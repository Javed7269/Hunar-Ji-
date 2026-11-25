package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerCard(
    videoUrl: String,
    title: String = "",
    subtitle: String = "",
    thumbnailUrl: String = "",
    youtubeId: String = "",
    height: Dp = 190.dp,
    modifier: Modifier = Modifier,
    onPlayTracked: () -> Unit = {}
) {
    val context = LocalContext.current
    val resolvedYoutubeUrl = when {
        youtubeId.isNotBlank() -> "https://www.youtube.com/watch?v=$youtubeId"
        videoUrl.contains("youtube.com", true) || videoUrl.contains("youtu.be", true) -> videoUrl
        else -> ""
    }

    if (resolvedYoutubeUrl.isNotBlank()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    onPlayTracked()
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resolvedYoutubeUrl)))
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(BrandBlueDark, Color(0xFF0F172A))))
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PlayCircleFilled,
                            contentDescription = "Play YouTube video",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (title.isNotBlank()) {
                            Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (subtitle.isNotBlank()) {
                            Text(subtitle, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Icon(Icons.Default.OpenInNew, contentDescription = "Open video", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        return
    }

    if (videoUrl.isBlank()) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            shape = RoundedCornerShape(16.dp),
            color = BrandBlueLight
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(title.ifBlank { "Video unavailable" }, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }
        return
    }

    var isPlaying by remember(videoUrl) { mutableStateOf(false) }
    var isMuted by remember(videoUrl) { mutableStateOf(true) }
    var trackedOnce by remember(videoUrl) { mutableStateOf(false) }

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFF0F172A))
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { view ->
                        view.player = exoPlayer
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = CircleShape
                    ) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                exoPlayer.volume = if (isMuted) 0f else 1f
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isMuted) "Unmute video" else "Mute video",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Surface(
                        color = BrandBlue,
                        shape = CircleShape
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    exoPlayer.pause()
                                    isPlaying = false
                                } else {
                                    exoPlayer.play()
                                    isPlaying = true
                                    if (!trackedOnce) {
                                        trackedOnce = true
                                        onPlayTracked()
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause video" else "Play video",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            if (title.isNotBlank() || subtitle.isNotBlank()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
