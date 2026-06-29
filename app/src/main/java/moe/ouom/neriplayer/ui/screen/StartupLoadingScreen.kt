package moe.ouom.neriplayer.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.ouom.neriplayer.ui.component.MineradioParticleField
import kotlin.math.sin

@Composable
fun StartupLoadingScreen(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "startup_loading")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "startup_pulse"
    )
    val wave by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "startup_wave"
    )

    val deep = Color(0xFF020607)
    val cyan = Color(0xFF00F5D4)
    val blue = Color(0xFF73A7FF)
    val champagne = Color(0xFFF4D28A)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF061116),
                    0.46f to deep,
                    1f to Color.Black
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        MineradioParticleField(
            modifier = Modifier.matchParentSize(),
            particleCount = 190,
            intensity = 1.08f,
            isPlaying = true
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .blur(44.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            cyan.copy(alpha = 0.22f + pulse * 0.10f),
                            blue.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(42.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color(0xFF0B1518).copy(alpha = 0.70f),
                                Color.Black.copy(alpha = 0.28f)
                            )
                        )
                    )
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    cyan.copy(alpha = 0.72f),
                                    champagne.copy(alpha = 0.42f),
                                    Color.White.copy(alpha = 0.18f)
                                )
                            ),
                            style = Stroke(width = strokeWidth)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension * (0.33f + pulse * 0.035f)
                    drawCircle(
                        color = cyan.copy(alpha = 0.22f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.2.dp.toPx())
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(cyan, blue, champagne, cyan),
                            center = center
                        ),
                        startAngle = -90f + wave * 360f,
                        sweepAngle = 110f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(
                text = "李烜's radio",
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
            )
            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(18) { index ->
                    val phase = (wave * 6.28318f) + index * 0.44f
                    val height = 8.dp + (sin(phase) * 0.5f + 0.5f).dp * 26f
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = height)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(champagne, cyan.copy(alpha = 0.74f))
                                )
                            )
                    )
                }
            }
        }

        Text(
            text = "Tuning your soundscape",
            color = Color.White.copy(alpha = 0.45f),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
        )
    }
}
