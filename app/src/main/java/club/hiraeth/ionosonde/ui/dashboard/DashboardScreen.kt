package club.hiraeth.ionosonde.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import club.hiraeth.ionosonde.ui.components.BandConditionRow
import club.hiraeth.ionosonde.ui.components.IndexCard
import club.hiraeth.ionosonde.ui.components.LastUpdatedText
import club.hiraeth.ionosonde.ui.components.VhfConditionRow
import club.hiraeth.ionosonde.ui.theme.ConditionAmber
import club.hiraeth.ionosonde.ui.theme.kIndexColor
import club.hiraeth.ionosonde.ui.theme.sfiColor
import club.hiraeth.ionosonde.ui.theme.xRayColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val solarData by viewModel.solarData.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Ionosonde") },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = onNavigateToAbout) {
                    Icon(Icons.Default.Info, contentDescription = "About")
                }
            }
        )

        val data = solarData
        if (data == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Loading solar data...")
                    Text(
                        "Pull to refresh or wait for auto-update",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            // Top summary bar
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Solar Indices",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IndexCard("SFI", data.solarFluxIndex.toString(), sfiColor(data.solarFluxIndex))
                        IndexCard("SN", data.sunspotNumber.toString(), MaterialTheme.colorScheme.onSurface)
                        IndexCard("A-Index", data.aIndex.toString(), kIndexColor(data.aIndex / 4))
                        IndexCard("K-Index", data.kIndex.toString(), kIndexColor(data.kIndex))
                        IndexCard("X-Ray", data.xRayClass, xRayColor(data.xRayClass))
                    }
                }
            }

            // Secondary row
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Space Environment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IndexCard("Proton Flux", data.protonFlux, ConditionAmber)
                        IndexCard("Electron Flux", data.electronFlux, ConditionAmber)
                        IndexCard("Solar Wind", data.solarWindSpeed, ConditionAmber)
                        IndexCard("Bz", data.bzStatus, ConditionAmber)
                        IndexCard("Geo Mag", data.geomagneticField, ConditionAmber)
                        IndexCard("Signal Noise", data.signalNoiseLevel, ConditionAmber)
                        IndexCard("Aurora", data.auroraActivity, ConditionAmber)
                        IndexCard("Aurora Lat", data.auroraLatitude, ConditionAmber)
                    }
                }
            }

            // Band conditions
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "HF Band Conditions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Band", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        Text("Day", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.8f))
                        Text("Night", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.8f))
                    }
                    BandConditionRow("80m-40m", data.band80_40Day, data.band80_40Night)
                    BandConditionRow("30m-20m", data.band30_20Day, data.band30_20Night)
                    BandConditionRow("17m-15m", data.band17_15Day, data.band17_15Night)
                    BandConditionRow("12m-10m", data.band12_10Day, data.band12_10Night)
                }
            }

            // VHF Conditions
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "VHF Conditions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    VhfConditionRow("Aurora", data.vhfAurora)
                    VhfConditionRow("6m Es EU", data.vhf6mEsEU)
                    VhfConditionRow("4m Es EU", data.vhf4mEsEU)
                    VhfConditionRow("2m Es EU", data.vhf2mEsEU)
                    VhfConditionRow("2m Es NA", data.vhf2mEsNA)
                    VhfConditionRow("EME Deg", data.vhfEmeDeg)
                    VhfConditionRow("MUF", data.mufBar)
                }
            }

            // Solar flare probability
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    IndexCard(
                        "Flare Prob",
                        data.solarFlareProbability.ifEmpty { "N/A" },
                        ConditionAmber,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Last updated
            LastUpdatedText(
                timestamp = data.updated.ifEmpty {
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        .format(java.util.Date(data.fetchedAt))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}
