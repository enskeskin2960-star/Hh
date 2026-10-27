package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.VerdictType
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.NeonCyberCyan
import com.example.ui.theme.NeonDangerRed
import com.example.ui.theme.NeonMatrixGreen
import com.example.ui.theme.NeonWarningAmber
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun AnimatedRadarSweepHud(
    verdictType: VerdictType,
    ruinProbabilityPct: Double,
    serverLoadPct: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val primaryColor = when (verdictType) {
        VerdictType.ENTER_OPTIMAL -> NeonMatrixGreen
        VerdictType.ENTER_HIT_AND_RUN -> NeonWarningAmber
        VerdictType.DO_NOT_ENTER, VerdictType.RESET_REQUIRED -> NeonDangerRed
    }

    Box(
        modifier = modifier
            .size(92.dp)
            .clip(RoundedCornerShape(46.dp))
            .background(CyberObsidian)
            .border(1.5.dp, primaryColor.copy(alpha = 0.6f), RoundedCornerShape(46.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) / 2f - 4.dp.toPx()

            // Concentric radar rings
            for (i in 1..3) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.22f),
                    radius = maxRadius * (i / 3f),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Crosshair lines
            drawLine(
                color = primaryColor.copy(alpha = 0.18f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.18f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )

            // Sweep sector arc
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.42f), Color.Transparent),
                    center = center,
                    radius = maxRadius
                ),
                startAngle = sweepAngle - 55f,
                sweepAngle = 55f,
                useCenter = true,
                topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                size = Size(maxRadius * 2f, maxRadius * 2f)
            )

            // Sweep leading needle
            val rad = Math.toRadians(sweepAngle.toDouble())
            val endX = center.x + (maxRadius * cos(rad)).toFloat()
            val endY = center.y + (maxRadius * sin(rad)).toFloat()
            drawLine(
                color = primaryColor,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Blip dot representing target seed coordinate
            val blipAngleRad = Math.toRadians((serverLoadPct * 3.6) % 360.0)
            val blipDist = maxRadius * (0.35f + (ruinProbabilityPct.toFloat() / 200f))
            val blipCenter = Offset(
                center.x + (blipDist * cos(blipAngleRad)).toFloat(),
                center.y + (blipDist * sin(blipAngleRad)).toFloat()
            )
            drawCircle(
                color = primaryColor,
                radius = 3.5.dp.toPx(),
                center = blipCenter
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%${ruinProbabilityPct.roundToInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Text(
                text = "ERİME",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun EquityCurveChart(
    equityCurve: List<Float>,
    startingBankroll: Double,
    peakBankroll: Double,
    optimalExitSpin: Int,
    verdictType: VerdictType,
    modifier: Modifier = Modifier
) {
    val lineColor = when (verdictType) {
        VerdictType.ENTER_OPTIMAL -> NeonMatrixGreen
        VerdictType.ENTER_HIT_AND_RUN -> NeonWarningAmber
        VerdictType.DO_NOT_ENTER, VerdictType.RESET_REQUIRED -> NeonDangerRed
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberObsidian)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "500-SPİN KASA SİMÜLASYON EĞRİSİ",
                style = MaterialTheme.typography.labelLarge,
                color = NeonCyberCyan
            )
            Text(
                text = "Zirve: ₺${peakBankroll.roundToInt()} (${optimalExitSpin}. Spin)",
                style = MaterialTheme.typography.labelSmall,
                color = lineColor
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .padding(top = 10.dp, bottom = 4.dp)
        ) {
            if (equityCurve.size < 2) return@Canvas

            val maxVal = max(equityCurve.maxOrNull() ?: 1f, startingBankroll.toFloat() * 1.15f)
            val minVal = min(equityCurve.minOrNull() ?: 0f, startingBankroll.toFloat() * 0.5f).coerceAtLeast(0f)
            val range = max(1f, maxVal - minVal)

            val w = size.width
            val h = size.height

            // Draw horizontal grid lines
            for (g in 0..3) {
                val y = h * (g / 3f)
                drawLine(
                    color = CyberBorder.copy(alpha = 0.45f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Starting bankroll reference dashed line
            val startY = h - (((startingBankroll.toFloat() - minVal) / range) * h).coerceIn(0f, h)
            drawLine(
                color = NeonCyberCyan.copy(alpha = 0.65f),
                start = Offset(0f, startY),
                end = Offset(w, startY),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            )

            // Equity curve path + fill path
            val strokePath = Path()
            val fillPath = Path()
            val stepCount = equityCurve.size - 1

            equityCurve.forEachIndexed { idx, value ->
                val x = (idx.toFloat() / stepCount.toFloat()) * w
                val y = h - (((value - minVal) / range) * h).coerceIn(2f, h - 2f)
                if (idx == 0) {
                    strokePath.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    strokePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(w, h)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.30f), Color.Transparent),
                    startY = 0f,
                    endY = h
                )
            )

            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Highlight peak spin point
            if (optimalExitSpin in 1..stepCount) {
                val peakX = (optimalExitSpin.toFloat() / stepCount.toFloat()) * w
                val peakY = h - (((equityCurve[optimalExitSpin] - minVal) / range) * h).coerceIn(4f, h - 4f)
                drawCircle(
                    color = NeonCyberCyan,
                    radius = 4.5.dp.toPx(),
                    center = Offset(peakX, peakY)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0. Spin (Başlangıç: ₺${String.format(Locale.US, "%.0f", startingBankroll)})",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "250. Spin",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "500. Spin (Bitiş: ₺${String.format(Locale.US, "%.0f", equityCurve.lastOrNull() ?: 0f)})",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
