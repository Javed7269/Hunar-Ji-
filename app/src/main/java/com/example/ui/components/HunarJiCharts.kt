package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SalesOverviewLineChart(
    thisMonthPoints: List<Float>,
    lastMonthPoints: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendDot(color = BrandBlue, label = "This Month")
            LegendDot(color = AccentOrange, label = "Last Month")
        }
        Spacer(Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val maxVal = (thisMonthPoints + lastMonthPoints).maxOrNull()?.coerceAtLeast(1f) ?: 1f
            val leftPad = 14.dp.toPx()
            val rightPad = 14.dp.toPx()
            val topPad = 10.dp.toPx()
            val bottomPad = 10.dp.toPx()
            val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

            // Horizontal grid lines
            for (i in 0..4) {
                val y = topPad + (chartH / 4f) * i
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            fun buildOffsets(points: List<Float>): List<Offset> {
                if (points.isEmpty()) return emptyList()
                val step = if (points.size > 1) chartW / (points.size - 1) else chartW
                return points.mapIndexed { idx, v ->
                    val x = leftPad + idx * step
                    val y = topPad + chartH - (v / maxVal) * chartH
                    Offset(x, y)
                }
            }

            fun drawSeries(pts: List<Offset>, lineColor: Color) {
                if (pts.size < 2) return
                val linePath = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x, pts[i].y)
                    }
                }
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(pts.last().x, topPad + chartH)
                    lineTo(pts.first().x, topPad + chartH)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(lineColor.copy(alpha = 0.20f), Color.Transparent)
                    )
                )
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                )
                pts.forEach { p ->
                    drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = p)
                    drawCircle(color = lineColor, radius = 3.dp.toPx(), center = p)
                }
            }

            drawSeries(buildOffsets(lastMonthPoints), AccentOrange)
            drawSeries(buildOffsets(thisMonthPoints), BrandBlue)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { lbl ->
                Text(lbl, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun SalesByAppDonutChart(
    segments: List<Triple<String, Float, Color>>,
    centerText: String,
    modifier: Modifier = Modifier
) {
    val total = segments.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 20.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2f, stroke / 2f)
                var startAngle = -90f
                segments.forEach { (_, value, color) ->
                    val sweep = (value / total) * 360f
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
            Text(
                text = centerText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEach { (label, pct, color) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = color, shape = CircleShape, modifier = Modifier.size(9.dp)) {}
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${pct.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryRouteCanvasMap(
    progress: Float = 0.55f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Light map grid background
        drawRect(color = Color(0xFFEFF4FA))
        val roadColor = Color.White
        for (i in 1..4) {
            val y = size.height * (i / 5f)
            drawLine(roadColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 8.dp.toPx())
            val x = size.width * (i / 5f)
            drawLine(roadColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 8.dp.toPx())
        }
        // Green park patch
        drawRoundRect(
            color = Color(0xFFD1FAE5),
            topLeft = Offset(size.width * 0.55f, size.height * 0.58f),
            size = Size(size.width * 0.28f, size.height * 0.25f)
        )

        val p0 = Offset(size.width * 0.16f, size.height * 0.72f)
        val p1 = Offset(size.width * 0.44f, size.height * 0.36f)
        val p2 = Offset(size.width * 0.62f, size.height * 0.44f)
        val p3 = Offset(size.width * 0.84f, size.height * 0.24f)

        val route = Path().apply {
            moveTo(p0.x, p0.y)
            lineTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
        }
        drawPath(
            path = route,
            color = BrandBlue,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
            )
        )

        // Rider dot interpolated along route
        val riderPos = Offset(
            x = p0.x + (p3.x - p0.x) * progress.coerceIn(0f, 1f),
            y = p0.y + (p3.y - p0.y) * progress.coerceIn(0f, 1f)
        )
        drawCircle(color = BrandBlue.copy(alpha = 0.22f), radius = 16.dp.toPx(), center = riderPos)
        drawCircle(color = BrandBlue, radius = 9.dp.toPx(), center = riderPos)
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = riderPos)

        // Pickup pin (Green) & Drop pin (Red)
        drawCircle(color = AccentGreen, radius = 8.dp.toPx(), center = p0)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = p0)
        drawCircle(color = AccentRed, radius = 9.dp.toPx(), center = p3)
        drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = p3)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}
