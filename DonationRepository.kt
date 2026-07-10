package com.ecopantry.app.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A single labelled bar. */
data class BarChartEntry(val label: String, val value: Float, val color: Color)

/**
 * Minimal, dependency-free bar chart used for the Weekly Food Tracker and
 * Impact dashboards (FR10, US 6.2) so the project doesn't need an extra
 * third-party charting library.
 */
@Composable
fun SimpleBarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    barHeight: androidx.compose.ui.unit.Dp = 140.dp
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = (entries.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = 8.dp)
        ) {
            if (entries.isEmpty()) return@Canvas
            val barSlotWidth = size.width / entries.size
            val barWidth = barSlotWidth * 0.5f

            entries.forEachIndexed { index, entry ->
                val barHeightPx = (entry.value / maxValue) * size.height
                val left = index * barSlotWidth + (barSlotWidth - barWidth) / 2f
                val top = size.height - barHeightPx

                drawRoundRect(
                    color = entry.color,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeightPx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )

                val valueText = entry.value.toInt().toString()
                val measured = textMeasurer.measure(
                    androidx.compose.ui.text.AnnotatedString(valueText),
                    style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = valueText,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        left + barWidth / 2f - measured.size.width / 2f,
                        (top - measured.size.height - 4).coerceAtLeast(0f)
                    ),
                    style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = entry.color)
                )
            }
        }
        BarChartLabelRow(entries)
    }
}

@Composable
private fun BarChartLabelRow(entries: List<BarChartEntry>) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
    ) {
        entries.forEach { entry ->
            Text(
                text = entry.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
