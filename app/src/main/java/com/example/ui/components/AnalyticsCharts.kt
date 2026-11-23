package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.MintPulse
import com.example.ui.theme.SunsetEmber

@Composable
fun ViewsTrendChartCard(
    title: String,
    points: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val lineColor = CrimsonCoral
    val fillBrush = Brush.verticalGradient(
        colors = listOf(CrimsonCoral.copy(alpha = 0.36f), Color.Transparent)
    )
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
                val stepX = if (points.size > 1) size.width / (points.size - 1) else size.width

                // Subtle horizontal guide lines
                for (i in 0..3) {
                    val y = size.height * (i / 3f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.size >= 2) {
                    val linePath = Path()
                    val areaPath = Path()
                    points.forEachIndexed { index, v ->
                        val x = index * stepX
                        val y = size.height - (v / maxVal) * (size.height * 0.85f) - size.height * 0.06f
                        if (index == 0) {
                            linePath.moveTo(x, y)
                            areaPath.moveTo(x, size.height)
                            areaPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            areaPath.lineTo(x, y)
                        }
                    }
                    areaPath.lineTo((points.size - 1) * stepX, size.height)
                    areaPath.close()

                    drawPath(path = areaPath, brush = fillBrush)
                    drawPath(
                        path = linePath,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    points.forEachIndexed { index, v ->
                        val x = index * stepX
                        val y = size.height - (v / maxVal) * (size.height * 0.85f) - size.height * 0.06f
                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = SunsetEmber, radius = 3.dp.toPx(), center = Offset(x, y))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EngagementBarChartCard(
    title: String,
    likesSeries: List<Float>,
    followerSeries: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("● Likes", style = MaterialTheme.typography.labelSmall, color = ElectricAmber)
                    Text("● Followers", style = MaterialTheme.typography.labelSmall, color = MintPulse)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val count = minOf(likesSeries.size, followerSeries.size).coerceAtLeast(1)
                val maxVal = maxOf(
                    likesSeries.maxOrNull() ?: 1f,
                    followerSeries.maxOrNull() ?: 1f,
                    1f
                )
                val groupWidth = size.width / count
                val barW = (groupWidth * 0.28f).coerceAtLeast(6.dp.toPx())

                for (i in 0 until count) {
                    val centerX = i * groupWidth + groupWidth / 2f
                    val h1 = (likesSeries[i] / maxVal) * (size.height * 0.88f)
                    val h2 = (followerSeries[i] / maxVal) * (size.height * 0.88f)

                    drawRoundRect(
                        color = ElectricAmber,
                        topLeft = Offset(centerX - barW - 2.dp.toPx(), size.height - h1),
                        size = Size(barW, h1),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    drawRoundRect(
                        color = MintPulse,
                        topLeft = Offset(centerX + 2.dp.toPx(), size.height - h2),
                        size = Size(barW, h2),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
