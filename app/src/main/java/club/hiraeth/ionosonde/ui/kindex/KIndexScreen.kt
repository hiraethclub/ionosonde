package club.hiraeth.ionosonde.ui.kindex

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import club.hiraeth.ionosonde.ui.theme.ConditionAmber
import club.hiraeth.ionosonde.ui.theme.ConditionGreen
import club.hiraeth.ionosonde.ui.theme.ConditionRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KIndexScreen(viewModel: KIndexViewModel = viewModel()) {
    val entries by viewModel.entries.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("K-Index History (48h)") },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }
        )

        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Loading K-index data...")
                }
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                "Planetary K-Index — Last 48 Hours",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            // Take last 48 hours worth of data (3-hour intervals = 16 entries)
            val recentEntries = entries.takeLast(16)
            val barWidth = 48.dp
            val chartWidth = barWidth * recentEntries.size
            val chartHeight = 280.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Canvas(
                    modifier = Modifier
                        .width(chartWidth)
                        .height(chartHeight)
                ) {
                    val maxK = 9f
                    val barWidthPx = size.width / recentEntries.size
                    val bottomPadding = 60f
                    val topPadding = 20f
                    val chartAreaHeight = size.height - bottomPadding - topPadding

                    // Draw grid lines
                    for (k in 0..9) {
                        val y = topPadding + chartAreaHeight * (1f - k / maxK)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            k.toString(),
                            2f, y - 4f,
                            android.graphics.Paint().apply {
                                color = textColor.hashCode()
                                textSize = 28f
                            }
                        )
                    }

                    // Draw bars
                    recentEntries.forEachIndexed { index, entry ->
                        val barHeight = (entry.kIndex / maxK) * chartAreaHeight
                        val x = index * barWidthPx
                        val y = topPadding + chartAreaHeight - barHeight

                        val barColor = when {
                            entry.kIndex <= 2 -> ConditionGreen
                            entry.kIndex <= 4 -> ConditionAmber
                            else -> ConditionRed
                        }

                        drawRect(
                            color = barColor,
                            topLeft = Offset(x + barWidthPx * 0.1f, y.toFloat()),
                            size = Size(barWidthPx * 0.8f, barHeight.toFloat())
                        )

                        // K value on top of bar
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format("%.0f", entry.kIndex),
                            x + barWidthPx * 0.35f, y.toFloat() - 4f,
                            android.graphics.Paint().apply {
                                color = textColor.hashCode()
                                textSize = 24f
                            }
                        )

                        // Timestamp label (rotated)
                        val label = entry.timestamp.takeLast(14).take(11)
                        drawContext.canvas.nativeCanvas.save()
                        drawContext.canvas.nativeCanvas.rotate(
                            -45f,
                            x + barWidthPx * 0.5f,
                            size.height - 5f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            x + barWidthPx * 0.1f,
                            size.height - 5f,
                            android.graphics.Paint().apply {
                                color = textColor.hashCode()
                                textSize = 20f
                            }
                        )
                        drawContext.canvas.nativeCanvas.restore()
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("X-axis: UTC time | Y-axis: Kp value (0-9)", style = MaterialTheme.typography.bodySmall)
            Text("Green: K0-2 (quiet) | Amber: K3-4 (unsettled) | Red: K5+ (storm)", style = MaterialTheme.typography.bodySmall)

            if (entries.isNotEmpty()) {
                val lastEntry = entries.last()
                Spacer(Modifier.height(8.dp))
                Text(
                    "Last data point: ${lastEntry.timestamp} UTC",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
