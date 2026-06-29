package moe.ouom.neriplayer.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

@Composable
fun MineradioParticleField(
    modifier: Modifier = Modifier,
    particleCount: Int = 220,
    intensity: Float = 1f,
    isPlaying: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "mineradio_wallpaper_particles")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 118_000 else 148_000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "mineradio_wallpaper_phase"
    )

    val auroraA = Color(0xFF85DCFF)
    val auroraB = Color(0xFFA58CFF)
    val accent = Color(0xFF00F5D4)
    val warm = Color(0xFFF4D28A)
    val pearl = Color(0xFFF4FBFF)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val minSide = minOf(w, h)
        val maxSide = max(w, h)
        val t = phase * 120f
        val play = if (isPlaying) 1f else 0f
        val baseEnergy = intensity.coerceIn(0f, 1.6f)
        val breath = smooth01(0.5f + 0.5f * sin(t * 0.115f))
        val shimmer = smooth01(0.5f + 0.5f * sin(t * 0.052f + 1.8f))
        val musicLift = play * (0.055f + breath * 0.045f)
        val energy = baseEnergy * (0.78f + musicLift)

        drawRect(
            brush = Brush.verticalGradient(
                0f to Color(0xFF02060A).copy(alpha = 0.78f),
                0.48f to Color(0xFF061014).copy(alpha = 0.58f),
                1f to Color.Black.copy(alpha = 0.82f)
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = 0.080f * energy),
                    auroraA.copy(alpha = 0.035f * energy),
                    Color.Transparent
                ),
                center = Offset(w * 0.22f, h * 0.16f),
                radius = maxSide * 0.72f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    auroraB.copy(alpha = 0.070f * energy),
                    warm.copy(alpha = 0.024f * energy),
                    Color.Transparent
                ),
                center = Offset(w * 0.76f, h * 0.72f),
                radius = maxSide * 0.78f
            )
        )

        val ribbonCount = 5
        val particlesPerRibbon = max(56, particleCount / ribbonCount)
        repeat(ribbonCount) { band ->
            val bandN = (band + 0.5f) / ribbonCount
            val bandColor = mixColor(auroraA, auroraB, bandN * 0.86f)
            repeat(particlesPerRibbon) { step ->
                val seed = particleSeed(band * 997 + step * 13 + 31)
                val seedB = particleSeed(band * 443 + step * 17 + 71)
                val seedC = particleSeed(band * 211 + step * 19 + 103)
                val lane = (step + seed * 0.82f) / particlesPerRibbon
                val flow = fract(lane + t * (0.0018f + bandN * 0.0013f + seed * 0.0009f))
                val arc = (flow - 0.5f) * PI.toFloat() * (1.30f + bandN * 0.62f + seed * 0.20f)
                val armCurve = sin(arc + bandN * 2.2f + seed * 5.3f)
                val ridgeCenter = 0.43f + (seed - 0.5f) * 0.16f
                val local = fract(flow * (2.6f + bandN * 1.2f) + seedB)
                val ridgeDistance = (local - ridgeCenter) / (0.26f + seed * 0.05f)
                val ridge = exp(-(ridgeDistance * ridgeDistance))
                val x = w * (0.50f + (flow - 0.5f) * (1.08f + bandN * 0.34f)) +
                    cos(arc * 0.72f + bandN * 0.92f + seed * 1.3f) * minSide * (0.17f + bandN * 0.08f)
                val y = h * (0.23f + bandN * 0.115f) +
                    armCurve * minSide * (0.070f + bandN * 0.035f) +
                    sin(flow * TWO_PI * (0.55f + bandN * 0.24f) + t * (0.010f + bandN * 0.006f) + seed * 5.7f) * minSide * 0.020f +
                    (seedB - 0.5f) * minSide * 0.050f
                val depth = 0.36f + bandN * 0.58f + ridge * 0.16f
                val twinkle = smooth01(0.5f + 0.5f * sin(t * (0.16f + seed * 0.20f) + seedC * 15f))
                val alpha = (0.020f + ridge * 0.105f + twinkle * 0.018f + musicLift * 0.10f) * energy
                val radius = (0.65f + ridge * 1.55f + seedC * 0.55f + musicLift * 0.55f) * depth
                val color = mixColor(
                    mixColor(bandColor, pearl, ridge * 0.20f + twinkle * 0.08f),
                    warm,
                    if (band == 4) 0.10f + ridge * 0.08f else 0f
                )

                drawCircle(
                    color = color.copy(alpha = alpha * 0.35f),
                    radius = radius * (5.8f + ridge * 5.2f),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )

                if (step % 7 == 0) {
                    val drift = minSide * (0.012f + ridge * 0.018f)
                    drawLine(
                        color = color.copy(alpha = alpha * 0.34f),
                        start = Offset(x - drift * (0.8f + seed), y - drift * 0.18f),
                        end = Offset(x + drift * (0.7f + seedB), y + drift * 0.16f),
                        strokeWidth = 0.72f + ridge * 0.80f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        val dustCount = max(70, particleCount / 3)
        repeat(dustCount) { index ->
            val seed = particleSeed(index + 3000)
            val seedB = particleSeed(index + 5000)
            val seedC = particleSeed(index + 7000)
            val drift = fract(seed + t * (0.00062f + seedB * 0.0019f))
            val x = (drift - 0.5f) * (w * (1.18f + seedB * 0.42f)) + w * 0.5f
            val y = h * (0.12f + seedC * 0.73f) +
                sin(t * (0.015f + seed * 0.022f) + seedB * 8f) * minSide * 0.036f
            val twinkle = smooth01(0.5f + 0.5f * sin(t * (0.20f + seed * 0.18f) + seedC * 18f))
            val sparse = smooth01((seedB - 0.16f) / 0.84f)
            val fineTwinkle = twinkle * twinkle * twinkle
            val alpha = sparse * (0.026f + fineTwinkle * 0.105f + musicLift * 0.030f) * energy
            val radius = 0.62f + seedC * 1.35f + twinkle * 0.55f
            val color = mixColor(pearl, auroraA, 0.24f + seed * 0.28f)
            drawCircle(
                color = color.copy(alpha = alpha * 0.28f),
                radius = radius * 5.6f,
                center = Offset(x, y)
            )
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }

        val floorY = h * 0.78f
        drawRect(
            brush = Brush.verticalGradient(
                0f to pearl.copy(alpha = 0.030f * energy),
                0.45f to accent.copy(alpha = 0.020f * energy),
                1f to Color.Transparent
            ),
            topLeft = Offset(0f, floorY),
            size = Size(width = w, height = h - floorY)
        )
        repeat(26) { index ->
            val seed = particleSeed(index + 9000)
            val seedB = particleSeed(index + 9100)
            val x = w * (0.10f + seed * 0.80f)
            val lineBreath = smooth01(0.5f + 0.5f * sin(t * (0.060f + seedB * 0.035f) + index * 0.7f))
            val height = minSide * (0.018f + lineBreath * (0.030f + musicLift * 0.022f))
            drawLine(
                color = mixColor(accent, pearl, seedB * 0.46f).copy(alpha = (0.028f + lineBreath * 0.050f) * energy),
                start = Offset(x, floorY + minSide * 0.018f),
                end = Offset(x + (seedB - 0.5f) * minSide * 0.030f, floorY + height),
                strokeWidth = 0.8f + seed * 1.0f,
                cap = StrokeCap.Round
            )
        }
    }
}

private const val TWO_PI = (PI * 2.0).toFloat()

private fun particleSeed(index: Int): Float {
    val value = sin((index + 1) * 12.9898f) * 43758.5453f
    return fract(value)
}

private fun fract(value: Float): Float = value - floor(value)

private fun smooth01(value: Float): Float {
    val clamped = value.coerceIn(0f, 1f)
    return clamped * clamped * (3f - 2f * clamped)
}

private fun mixColor(start: Color, end: Color, amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * t,
        green = start.green + (end.green - start.green) * t,
        blue = start.blue + (end.blue - start.blue) * t,
        alpha = start.alpha + (end.alpha - start.alpha) * t
    )
}
