package moe.ouom.neriplayer.ui.component

/*
 * NeriPlayer - A unified Android player for streaming music and videos from multiple online platforms.
 * Copyright (C) 2025-2025 NeriPlayer developers
 * https://github.com/cwuom/NeriPlayer
 *
 * This software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.
 * If not, see <https://www.gnu.org/licenses/>.
 *
 * File: moe.ouom.neriplayer.ui.component/NeriMiniPlayer
 * Created: 2025/8/8
 */

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import moe.ouom.neriplayer.R
import moe.ouom.neriplayer.util.fastScrollableImageRequest
import moe.ouom.neriplayer.util.HapticIconButton

object NeriMiniPlayerDefaults {
    val Height = 76.dp
}

@Composable
fun NeriMiniPlayer(
    title: String,
    artist: String,
    coverUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    onExpand: () -> Unit,
    hazeState: HazeState,
    enableHaze: Boolean = true
) {
    val shape = RoundedCornerShape(28.dp)
    val supportsBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val glassAlpha = if (supportsBlur && enableHaze) 0.64f else 0.88f
    val cyan = Color(0xFF00F5D4)
    val champagne = Color(0xFFF4D28A)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    cyan.copy(alpha = 0.34f),
                    Color.White.copy(alpha = 0.10f),
                    champagne.copy(alpha = 0.18f)
                )
            )
        ),
        shape = shape,
        modifier = modifier
            .height(NeriMiniPlayerDefaults.Height)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = cyan.copy(alpha = 0.16f)
            )
            .clip(shape)
            .clickable { onExpand() }
            .then(
                if (supportsBlur && enableHaze) Modifier.hazeChild(state = hazeState, shape = shape)
                else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF35383A).copy(alpha = glassAlpha),
                            Color(0xFF12171A).copy(alpha = glassAlpha),
                            Color(0xFF030608).copy(alpha = glassAlpha)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = Color.Black.copy(alpha = 0.22f),
                        spotColor = cyan.copy(alpha = 0.12f)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                                cyan.copy(alpha = 0.20f),
                                Color.Black.copy(alpha = 0.12f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                if (coverUrl != null) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = fastScrollableImageRequest(
                            context = context,
                            data = coverUrl,
                            sizePx = 128,
                            crossfade = false
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    // 显示默认音乐图标
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = champagne
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.94f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.56f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HapticIconButton(
                onClick = { onPlayPause() },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(cyan.copy(alpha = 0.28f), champagne.copy(alpha = 0.16f))
                        ),
                        CircleShape
                    )
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    label = "mini_play_pause_icon",
                    transitionSpec = {
                        (scaleIn(
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            initialScale = 0.7f
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 150)
                        )) togetherWith (scaleOut(
                            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                            targetScale = 0.7f
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 100)
                        ))
                    }
                ) { currentlyPlaying ->
                    Icon(
                        imageVector = if (currentlyPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (currentlyPlaying) stringResource(R.string.lyrics_pause) else stringResource(R.string.lyrics_play),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
