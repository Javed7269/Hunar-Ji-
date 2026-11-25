package com.example.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.registrationDataStore by preferencesDataStore(name = "hunarji_reg_drafts")

data class RememberedLoginPrefs(
    val rememberMe: Boolean = false,
    val savedIdentifier: String = ""
)

object RegistrationDraftStore {

    private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
    private val KEY_REMEMBERED_ID = stringPreferencesKey("remembered_login_id")

    private fun draftKey(role: String) = stringPreferencesKey("draft_${role.uppercase()}")

    fun observeRememberedLogin(context: Context): Flow<RememberedLoginPrefs> =
        context.registrationDataStore.data.map { prefs ->
            RememberedLoginPrefs(
                rememberMe = prefs[KEY_REMEMBER_ME] ?: false,
                savedIdentifier = prefs[KEY_REMEMBERED_ID] ?: ""
            )
        }

    suspend fun saveRememberedLogin(context: Context, rememberMe: Boolean, identifier: String) {
        context.registrationDataStore.edit { prefs ->
            prefs[KEY_REMEMBER_ME] = rememberMe
            prefs[KEY_REMEMBERED_ID] = if (rememberMe) identifier.trim() else ""
        }
    }

    suspend fun saveRoleDraft(context: Context, role: String, values: Map<String, String>) {
        val json = JSONObject()
        values.forEach { (k, v) -> json.put(k, v) }
        context.registrationDataStore.edit { prefs ->
            prefs[draftKey(role)] = json.toString()
        }
    }

    suspend fun loadRoleDraft(context: Context, role: String): Map<String, String> {
        val raw = context.registrationDataStore.data.first()[draftKey(role)] ?: return emptyMap()
        return try {
            val json = JSONObject(raw)
            buildMap {
                json.keys().forEach { key ->
                    put(key, json.optString(key, ""))
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun clearRoleDraft(context: Context, role: String) {
        context.registrationDataStore.edit { prefs ->
            prefs.remove(draftKey(role))
        }
    }
}
