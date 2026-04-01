package club.hiraeth.ionosonde.ui.sfi

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import club.hiraeth.ionosonde.ui.theme.ConditionAmber
import club.hiraeth.ionosonde.ui.theme.ConditionGreen
import club.hiraeth.ionosonde.ui.theme.PrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SfiScreen(viewModel: SfiViewModel = viewModel()) {
    val entries by viewModel.entries.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("27-Day SFI Forecast") },
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
                    Text("Loading SFI forecast data...")
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
                "Solar Flux Index — 27-Day Forecast",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            val pointSpacing = 40.dp
            val chartWidth = pointSpacing * entries.size
            val chartHeight = 300.dp

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
                    val minSfi = (entries.minOfOrNull { it.sfi } ?: 60) - 20
                    val maxSfi = (entries.maxOfOrNull { it.sfi } ?: 200) + 20
                    val range = (maxSfi - minSfi).toFloat()

                    val bottomPadding = 60f
                    val topPadding = 20f
                    val leftPadding = 40f
                    val chartAreaHeight = size.height - bottomPadding - topPadding
                    val chartAreaWidth = size.width - leftPadding

                    fun sfiToY(sfi: Int): Float =
                        topPadding + chartAreaHeight * (1f - (sfi - minSfi) / range)

                    fun indexToX(index: Int): Float =
                        leftPadding + (index.toFloat() / (entries.size - 1).coerceAtLeast(1)) * chartAreaWidth

                    // Horizontal reference lines at SFI 100 and 150
                    listOf(100, 150).forEach { ref ->
                        if (ref in minSfi..maxSfi) {
                            val y = sfiToY(ref)
                            drawLine(
                                color = if (ref == 100) ConditionGreen.copy(alpha = 0.5f) else ConditionAmber.copy(alpha = 0.5f),
                                start = Offset(leftPadding, y),
                                end = Offset(size.width, y),
                                strokeWidth = 2f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 10f)
                                )
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "SFI $ref",
                                leftPadding + 4f, y - 4f,
                                android.graphics.Paint().apply {
                                    color = textColor.hashCode()
                                    textSize = 24f
                                }
                            )
                        }
                    }

                    // Draw Y-axis labels
                    val step = ((maxSfi - minSfi) / 5).coerceAtLeast(10)
                    var label = minSfi - (minSfi % step) + step
                    while (label < maxSfi) {
                        val y = sfiToY(label)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(leftPadding, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            label.toString(),
                            2f, y + 6f,
                            android.graphics.Paint().apply {
                                color = textColor.hashCode()
                                textSize = 22f
                            }
                        )
                        label += step
                    }

                    // Draw line graph
                    if (entries.size >= 2) {
                        val path = Path()
                        entries.forEachIndexed { index, entry ->
                            val x = indexToX(index)
                            val y = sfiToY(entry.sfi)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, PrimaryLight, style = Stroke(width = 3f))

                        // Draw data points
                        entries.forEachIndexed { index, entry ->
                            val x = indexToX(index)
                            val y = sfiToY(entry.sfi)
                            drawCircle(PrimaryLight, radius = 4f, center = Offset(x, y))
                        }
                    }

                    // X-axis date labels (every few entries)
                    val labelEvery = (entries.size / 7).coerceAtLeast(1)
                    entries.forEachIndexed { index, entry ->
                        if (index % labelEvery == 0 || index == entries.lastIndex) {
                            val x = indexToX(index)
                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.rotate(-45f, x, size.height - 5f)
                            drawContext.canvas.nativeCanvas.drawText(
                                entry.date.takeLast(6),
                                x - 10f, size.height - 5f,
                                android.graphics.Paint().apply {
                                    color = textColor.hashCode()
                                    textSize = 20f
                                }
                            )
                            drawContext.canvas.nativeCanvas.restore()
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Dashed green line: SFI 100 | Dashed amber line: SFI 150", style = MaterialTheme.typography.bodySmall)
            Text("SFI above 100 generally indicates good HF propagation on higher bands.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
