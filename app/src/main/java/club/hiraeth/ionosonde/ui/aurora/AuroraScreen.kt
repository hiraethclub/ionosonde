package club.hiraeth.ionosonde.ui.aurora

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import club.hiraeth.ionosonde.ui.components.IndexCard
import club.hiraeth.ionosonde.ui.components.LastUpdatedText
import club.hiraeth.ionosonde.ui.theme.ConditionAmber
import club.hiraeth.ionosonde.ui.theme.ConditionGreen
import club.hiraeth.ionosonde.ui.theme.ConditionRed
import club.hiraeth.ionosonde.ui.theme.kIndexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuroraScreen(viewModel: AuroraViewModel = viewModel()) {
    val solarData by viewModel.solarData.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Aurora") },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            val data = solarData

            // Current conditions card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Current Conditions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (data != null) {
                        IndexCard("K-Index", data.kIndex.toString(), kIndexColor(data.kIndex), Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        IndexCard("Aurora Lat", data.auroraLatitude.ifEmpty { "N/A" }, ConditionAmber, Modifier.fillMaxWidth())

                        Spacer(Modifier.height(8.dp))
                        val auroraNote = getAuroraVisibilityNote(data.kIndex, data.auroraLatitude)
                        Text(
                            text = auroraNote,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (data.kIndex >= 4) ConditionAmber else MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text("No data available. Pull to refresh.")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 24h K-Index Forecast chart
            if (forecast.isNotEmpty()) {
                Text("24h K-Index Forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                val recentForecast = forecast.takeLast(8)
                val barWidth = 56.dp
                val chartWidth = barWidth * recentForecast.size
                val chartHeight = 240.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Canvas(
                        modifier = Modifier.width(chartWidth).height(chartHeight)
                    ) {
                        val maxK = 9f
                        val barWidthPx = size.width / recentForecast.size
                        val bottomPadding = 60f
                        val topPadding = 20f
                        val chartAreaHeight = size.height - bottomPadding - topPadding

                        for (k in 0..9 step 2) {
                            val y = topPadding + chartAreaHeight * (1f - k / maxK)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.3f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                        }

                        recentForecast.forEachIndexed { index, entry ->
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

                            drawContext.canvas.nativeCanvas.drawText(
                                String.format("%.1f", entry.kIndex),
                                x + barWidthPx * 0.2f, y.toFloat() - 4f,
                                android.graphics.Paint().apply {
                                    color = textColor.hashCode()
                                    textSize = 22f
                                }
                            )

                            val label = entry.timestamp.takeLast(14).take(11)
                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.rotate(-45f, x + barWidthPx * 0.5f, size.height - 5f)
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x + barWidthPx * 0.1f, size.height - 5f,
                                android.graphics.Paint().apply {
                                    color = textColor.hashCode()
                                    textSize = 18f
                                }
                            )
                            drawContext.canvas.nativeCanvas.restore()
                        }
                    }
                }
            }

            if (data != null) {
                Spacer(Modifier.height(8.dp))
                LastUpdatedText(
                    timestamp = data.updated.ifEmpty {
                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                            .format(java.util.Date(data.fetchedAt))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun getAuroraVisibilityNote(kIndex: Int, latString: String): String {
    val lat = latString.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
    return when {
        kIndex >= 7 -> "Major storm — aurora likely visible at mid-latitudes (45°N and above). HF propagation severely disrupted."
        kIndex >= 5 -> "Geomagnetic storm — aurora possible at ${lat?.let { "${it.toInt()}°N" } ?: "mid-latitudes (~55°N)"}. Significant HF degradation."
        kIndex >= 4 -> "Possible above ${lat?.let { "${it.toInt()}°N" } ?: "55°N"} at current K$kIndex. Marginal visibility at mid-latitudes."
        kIndex >= 3 -> "Aurora unlikely at mid-latitudes. May be visible above ${lat?.let { "${it.toInt()}°N" } ?: "60°N"} under dark skies."
        else -> "Quiet geomagnetic conditions. Aurora confined to high latitudes (65°N+)."
    }
}
