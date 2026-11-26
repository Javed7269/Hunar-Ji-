package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.firebase.HunarJiRepository
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.signOutUser
import com.example.data.model.AppWorkspace
import com.example.data.model.KycApplication
import com.example.data.model.KycDocumentRecord
import com.example.ui.components.AuthScreen
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.components.WorkspaceSwitcherDialog
import com.example.ui.screens.admin.AdminWorkspace
import com.example.ui.screens.auth.AdminLoginScreen
import com.example.ui.screens.auth.RegistrationAndKycWizard
import com.example.ui.screens.customer.CustomerWorkspace
import com.example.ui.screens.delivery.DeliveryWorkspace
import com.example.ui.screens.mechanic.MechanicWorkspace
import com.example.ui.screens.seller.SellerWorkspace
import com.example.ui.theme.HunarJiTheme
import com.example.ui.viewmodel.AdminClaimState
import com.example.ui.viewmodel.HunarJiViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private data class PendingCredentialLogin(
    val identifier: String,
    val password: String
)

private data class PendingPasswordReset(
    val identifier: String,
    val newPassword: String
)

private data class PendingRegistrationPayload(
    val application: KycApplication,
    val rawPassword: String,
    val docs: List<KycDocumentRecord>,
    val termsAccepted: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep cold start independent from optional SDKs. Ads are initialized by
        // their feature when needed, not while the launcher activity is starting.
        try {
            enableEdgeToEdge()
        } catch (_: Exception) {
            // Continue with the platform window defaults on older/modified devices.
        }

        // Initialize the default Firebase app explicitly and fail closed to the
        // setup screen instead of allowing an initialization exception to crash startup.
        val firebaseConfigured = try {
            FirebaseApp.getInstance()
            true
        } catch (_: IllegalStateException) {
            try {
                FirebaseApp.initializeApp(this) != null
            } catch (_: Exception) {
                false
            }
        } catch (_: Exception) {
            false
        }
        setContent {
            HunarJiTheme {
                var startupComplete by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(1100)
                    startupComplete = true
                }
                if (!startupComplete) {
                    HunarJiStartupScreen()
                } else if (firebaseConfigured) {
                    HunarJiRoot()
                } else {
                    FirebaseSetupRequiredScreen()
                }
            }
        }
    }
}

@Composable
private fun HunarJiRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var pendingLogin by remember { mutableStateOf<PendingCredentialLogin?>(null) }
    var pendingReset by remember { mutableStateOf<PendingPasswordReset?>(null) }
    var pendingRegistration by remember { mutableStateOf<PendingRegistrationPayload?>(null) }
    var pendingAdminLogin by remember { mutableStateOf(false) }
    var authScreenError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {},
            onPreparePasswordLogin = { id, pwd ->
                val mobileDigits = id.filter(Char::isDigit).takeLast(10)
                if (mobileDigits.length != 10) {
                    authScreenError = "Enter your registered 10-digit mobile number."
                } else {
                    authScreenError = null
                    Firebase.auth.signInWithEmailAndPassword("$mobileDigits@mobile.hunarji.in", pwd)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                pendingLogin = PendingCredentialLogin(mobileDigits, pwd)
                                pendingReset = null
                                pendingRegistration = null
                                pendingAdminLogin = false
                            } else {
                                authScreenError = when (task.exception) {
                                    is com.google.firebase.auth.FirebaseAuthInvalidUserException,
                                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                                        "Mobile number or password is incorrect. Please register or check your details."
                                    else -> task.exception?.localizedMessage ?: "Login failed. Please try again."
                                }
                            }
                        }
                }
            },
            onPrepareRegistration = { app, rawPwd, docs, terms ->
                pendingRegistration = PendingRegistrationPayload(app, rawPwd, docs, terms)
                pendingLogin = null
                pendingReset = null
                pendingAdminLogin = false
            },
            onPrepareAdminLogin = {
                pendingAdminLogin = true
                pendingLogin = null
                pendingReset = null
                pendingRegistration = null
            },
            externalError = authScreenError,
            onClearExternalError = { authScreenError = null }
        )
    } else {
        key(user.uid) {
            HunarJiAuthenticatedShell(
                user = user,
                pendingLogin = pendingLogin,
                pendingReset = pendingReset,
                pendingRegistration = pendingRegistration,
                pendingAdminLogin = pendingAdminLogin,
                onConsumePendingLogin = { pendingLogin = null },
                onConsumePendingReset = { pendingReset = null },
                onConsumePendingRegistration = { pendingRegistration = null },
                onConsumePendingAdminLogin = { pendingAdminLogin = false },
                onAuthRejected = { reason ->
                    authScreenError = reason
                    pendingLogin = null
                    pendingAdminLogin = false
                    signOutUser(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                },
                onSignOut = {
                    pendingLogin = null
                    pendingReset = null
                    pendingRegistration = null
                    pendingAdminLogin = false
                    signOutUser(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            )
        }
    }
}

@Composable
private fun HunarJiAuthenticatedShell(
    user: FirebaseUser,
    pendingLogin: PendingCredentialLogin?,
    pendingReset: PendingPasswordReset?,
    pendingRegistration: PendingRegistrationPayload?,
    pendingAdminLogin: Boolean,
    onConsumePendingLogin: () -> Unit,
    onConsumePendingReset: () -> Unit,
    onConsumePendingRegistration: () -> Unit,
    onConsumePendingAdminLogin: () -> Unit,
    onAuthRejected: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: HunarJiViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                val dbId = appContext.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                val repo = HunarJiRepository(db = db, auth = Firebase.auth)
                HunarJiViewModel(
                    repository = repo,
                    currentUserId = user.uid,
                    currentUserEmail = user.email ?: "",
                    currentUserName = user.displayName ?: (user.email?.substringBefore("@") ?: "Hunar Ji User")
                )
            }
        }
    )

    val activeWorkspace by viewModel.activeWorkspace.collectAsStateWithLifecycle()
    val adminClaimState by viewModel.adminClaimState.collectAsStateWithLifecycle()
    val isAdminVerified by viewModel.isAdminVerified.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = (profileState as? UiState.Success)?.data
    val myKyc by viewModel.myKycApplication.collectAsStateWithLifecycle()
    val myDocs by viewModel.myKycDocuments.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showWorkspaceSwitcher by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var registrationWizardRole by remember { mutableStateOf<String?>(null) }
    var registeredSuccessApp by remember { mutableStateOf<KycApplication?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(pendingAdminLogin) {
        if (pendingAdminLogin) {
            viewModel.switchWorkspace(AppWorkspace.ADMIN)
            onConsumePendingAdminLogin()
        }
    }

    LaunchedEffect(pendingLogin) {
        if (pendingLogin != null) {
            viewModel.verifyPasswordLogin(
                identifier = pendingLogin.identifier,
                password = pendingLogin.password,
                onSuccess = { onConsumePendingLogin() },
                onError = { err -> onAuthRejected(err) }
            )
        }
    }

    LaunchedEffect(pendingReset) {
        if (pendingReset != null) {
            viewModel.resetPasswordWithOtp(
                identifier = pendingReset.identifier,
                newPassword = pendingReset.newPassword,
                onSuccess = { onConsumePendingReset() },
                onError = { err ->
                    onConsumePendingReset()
                    onAuthRejected(err)
                }
            )
        }
    }

    LaunchedEffect(pendingRegistration) {
        if (pendingRegistration != null) {
            viewModel.submitRegistration(
                application = pendingRegistration.application,
                rawPassword = pendingRegistration.rawPassword,
                docRecords = pendingRegistration.docs,
                termsAccepted = pendingRegistration.termsAccepted,
                onSuccess = { saved ->
                    onConsumePendingRegistration()
                    registeredSuccessApp = saved
                },
                onError = { _ ->
                    onConsumePendingRegistration()
                }
            )
        }
    }

    val wizRole = registrationWizardRole
    if (wizRole != null) {
        RegistrationAndKycWizard(
            initialRole = wizRole,
            existingKyc = myKyc,
            existingDocs = myDocs,
            defaultName = profile?.displayName?.ifBlank { viewModel.currentUserName } ?: viewModel.currentUserName,
            defaultEmail = profile?.email?.ifBlank { viewModel.currentUserEmail } ?: viewModel.currentUserEmail,
            defaultPhone = profile?.phone ?: "",
            onSubmitRegistration = { app, rawPwd, docs, terms, onSuccess, onError ->
                viewModel.submitRegistration(app, rawPwd, docs, terms, onSuccess, onError)
            },
            onCompletedClose = { registrationWizardRole = null },
            onCancel = { registrationWizardRole = null }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (activeWorkspace) {
            AppWorkspace.CUSTOMER -> CustomerWorkspace(
                viewModel = viewModel,
                onOpenWorkspaceSwitcher = { showWorkspaceSwitcher = true },
                onOpenLanguagePicker = { showLanguagePicker = true },
                onOpenRegistrationWizard = { role -> registrationWizardRole = role },
                onSignOut = onSignOut
            )

            AppWorkspace.MECHANIC -> MechanicWorkspace(
                viewModel = viewModel,
                onOpenWorkspaceSwitcher = { showWorkspaceSwitcher = true },
                onOpenKycWizard = { role -> registrationWizardRole = role },
                onSignOut = onSignOut
            )

            AppWorkspace.SELLER -> SellerWorkspace(
                viewModel = viewModel,
                onOpenWorkspaceSwitcher = { showWorkspaceSwitcher = true },
                onOpenKycWizard = { role -> registrationWizardRole = role },
                onSignOut = onSignOut
            )

            AppWorkspace.DELIVERY -> DeliveryWorkspace(
                viewModel = viewModel,
                onOpenWorkspaceSwitcher = { showWorkspaceSwitcher = true },
                onOpenKycWizard = { role -> registrationWizardRole = role },
                onSignOut = onSignOut
            )

            AppWorkspace.ADMIN -> {
                if (adminClaimState is AdminClaimState.Verified) {
                    AdminWorkspace(
                        viewModel = viewModel,
                        onOpenWorkspaceSwitcher = { showWorkspaceSwitcher = true },
                        onSignOut = onSignOut
                    )
                } else {
                    AdminLoginScreen(
                        isSignedIn = true,
                        signedInEmail = user.email.orEmpty().ifBlank { viewModel.currentUserEmail },
                        claimState = adminClaimState,
                        isLoading = adminClaimState is AdminClaimState.Checking,
                        onSignInWithGoogleForAdmin = {},
                        onRetryClaimVerification = {
                            viewModel.verifyAdminClaimAndRedirect(forceRefresh = true)
                        },
                        onSwitchAdminAccount = onSignOut,
                        onBack = {
                            viewModel.switchWorkspace(AppWorkspace.CUSTOMER)
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 72.dp)
        )
    }

    registeredSuccessApp?.let { saved ->
        AlertDialog(
            onDismissRequest = { registeredSuccessApp = null },
            title = { Text("Registration Submitted!") },
            text = {
                Text(
                    "Your unique Hunar Ji User ID is ${saved.hunarUserId} (${saved.role}). KYC Status: ${saved.kycStatus}."
                )
            },
            confirmButton = {
                Button(onClick = { registeredSuccessApp = null }) {
                    Text("Continue")
                }
            }
        )
    }

    if (showWorkspaceSwitcher) {
        WorkspaceSwitcherDialog(
            current = activeWorkspace,
            isAdminUser = isAdminVerified,
            onSelect = { viewModel.switchWorkspace(it) },
            onDismiss = { showWorkspaceSwitcher = false }
        )
    }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            current = language,
            onSelect = { viewModel.setLanguage(it) },
            onDismiss = { showLanguagePicker = false }
        )
    }
}


@Composable
private fun FirebaseSetupRequiredScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hunar Ji setup required")
        Text(
            "Firebase is not configured in this APK. Add the Firebase Android google-services.json " +
                "for application ID com.aistudio.hunarji.mrxkpq, then rebuild the APK. " +
                "Until then, sign-in, cloud data and role dashboards cannot work."
        )
    }
}


@Composable
private fun HunarJiStartupScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F1FF), Color(0xFFF8FBFF), Color.White)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_hunarji_logo_fg),
                contentDescription = "Hunar Ji app logo",
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(220.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.img_hunarji_wordmark),
                contentDescription = "Hunar Ji",
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(58.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SHOP  •  BOOK  •  EARN  •  GROW",
                color = Color(0xFF31527C),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(30.dp))
            androidx.compose.material3.CircularProgressIndicator(
                color = Color(0xFF1769C2),
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
