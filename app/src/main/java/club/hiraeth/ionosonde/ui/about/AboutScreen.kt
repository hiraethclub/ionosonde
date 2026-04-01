package club.hiraeth.ionosonde.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("About Ionosonde") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Ionosonde",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Solar & Radio Propagation Conditions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Ionosonde displays real-time solar and radio propagation conditions " +
                        "for amateur radio operators. View solar indices, HF and VHF band conditions, " +
                        "K-index history, SFI forecasts, aurora predictions, and a live solar terminator map — " +
                        "all at a glance.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            SectionCard("Data Sources & Attribution") {
                AttributionItem(
                    "N0NBH Solar Data Feed",
                    "Solar and band condition data provided by Paul L. Herrman, N0NBH, " +
                            "via the HamQSL.com XML data feed. This application uses only parsed " +
                            "data values and does not reproduce or embed any copyrighted imagery."
                )

                Spacer(Modifier.height(12.dp))

                AttributionItem(
                    "NOAA Space Weather Prediction Center",
                    "Planetary K-index history, 27-day SFI forecast, and aurora K-index forecast " +
                            "data sourced from the NOAA Space Weather Prediction Center (SWPC) " +
                            "public data feeds at services.swpc.noaa.gov."
                )

                Spacer(Modifier.height(12.dp))

                AttributionItem(
                    "OpenStreetMap",
                    "Map tiles provided by OpenStreetMap contributors, licensed under the " +
                            "Open Data Commons Open Database License (ODbL). " +
                            "\u00A9 OpenStreetMap contributors."
                )
            }

            Spacer(Modifier.height(16.dp))

            SectionCard("About the App") {
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Minimum Android version: 8.0 (API 26)\n" +
                            "Built with Kotlin, Jetpack Compose, Material 3\n" +
                            "Maps: OSMDroid 6.x\n" +
                            "Widgets: Jetpack Glance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            SectionCard("Developer") {
                Text(
                    "Aisling de Gr\u00E1s",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "aisling@hiraeth.club",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Proudly made in Wales\nCymru am byth!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            SectionCard("Licence") {
                Text(
                    "This application is provided as-is for amateur radio operators. " +
                            "Solar data is subject to the terms of the respective data providers. " +
                            "Map data \u00A9 OpenStreetMap contributors, available under the ODbL licence.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun AttributionItem(source: String, description: String) {
    Text(
        source,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(2.dp))
    Text(
        description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
