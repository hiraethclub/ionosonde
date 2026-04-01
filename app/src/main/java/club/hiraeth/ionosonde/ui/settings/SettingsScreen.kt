package club.hiraeth.ionosonde.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
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
            // Refresh interval
            SectionHeader("Data Refresh")
            DropdownSetting(
                label = "Refresh interval",
                currentValue = when (settings.refreshIntervalMinutes) {
                    15 -> "15 minutes"
                    30 -> "30 minutes"
                    60 -> "1 hour"
                    0 -> "Manual only"
                    else -> "${settings.refreshIntervalMinutes} min"
                },
                options = listOf(
                    "15 minutes" to 15,
                    "30 minutes" to 30,
                    "1 hour" to 60,
                    "Manual only" to 0
                ),
                onSelect = { viewModel.updateRefreshInterval(it) }
            )

            Spacer(Modifier.height(16.dp))

            // Notification thresholds
            SectionHeader("Notification Thresholds")

            DropdownSetting(
                label = "K-index alert level",
                currentValue = "K${settings.kIndexAlertLevel}",
                options = (2..8).map { "K$it" to it },
                onSelect = { viewModel.updateKIndexAlert(it) }
            )

            Spacer(Modifier.height(8.dp))

            DropdownSetting(
                label = "Flare class alert",
                currentValue = "${settings.flareClassAlert}-class",
                options = listOf("C-class" to "C", "M-class" to "M", "X-class" to "X"),
                onSelect = { viewModel.updateFlareAlert(it) }
            )

            Spacer(Modifier.height(8.dp))

            SwitchSetting(
                label = "Proton event alerts",
                checked = settings.protonEventAlert,
                onCheckedChange = { viewModel.updateProtonAlert(it) }
            )

            Spacer(Modifier.height(16.dp))

            // Widget appearance
            SectionHeader("Widget Appearance")

            Text("Background opacity", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = settings.widgetBgAlpha.toFloat(),
                onValueChange = { viewModel.updateWidgetBgAlpha(it.toInt()) },
                valueRange = 0f..100f,
                steps = 19
            )
            Text("${settings.widgetBgAlpha}%", style = MaterialTheme.typography.labelSmall)

            Spacer(Modifier.height(8.dp))

            // Color pickers (simplified as preset palettes)
            Text("Text colour", style = MaterialTheme.typography.bodyMedium)
            ColorPalette(
                selectedColor = Color(settings.widgetTextColor),
                onColorSelected = { viewModel.updateWidgetTextColor(it) }
            )

            Spacer(Modifier.height(8.dp))

            Text("Background colour", style = MaterialTheme.typography.bodyMedium)
            ColorPalette(
                selectedColor = Color(settings.widgetBgColor),
                onColorSelected = { viewModel.updateWidgetBgColor(it) },
                colors = listOf(
                    0xFF000000, 0xFF1A1A2E, 0xFF0D1B2A, 0xFF16213E,
                    0xFF1A1A1A, 0xFF2D2D2D, 0xFF0F3460, 0xFF533483
                )
            )

            Spacer(Modifier.height(8.dp))

            SwitchSetting(
                label = "Use fixed green/amber/red indicators",
                checked = settings.useFixedConditionColors,
                onCheckedChange = { viewModel.updateUseFixedColors(it) }
            )

            if (!settings.useFixedConditionColors) {
                Spacer(Modifier.height(4.dp))
                Text("Accent colour", style = MaterialTheme.typography.bodyMedium)
                ColorPalette(
                    selectedColor = Color(settings.widgetAccentColor),
                    onColorSelected = { viewModel.updateWidgetAccent(it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Time format
            SectionHeader("Display")

            SwitchSetting(
                label = "UTC only (disable local time)",
                checked = settings.timeFormatUtcOnly,
                onCheckedChange = { viewModel.updateTimeFormat(it) }
            )

            Spacer(Modifier.height(8.dp))

            // Theme
            DropdownSetting(
                label = "Theme",
                currentValue = when (settings.themeMode) {
                    "light" -> "Light"
                    "dark" -> "Dark"
                    else -> "System default"
                },
                options = listOf(
                    "Light" to "light",
                    "Dark" to "dark",
                    "System default" to "system"
                ),
                onSelect = { viewModel.updateTheme(it) }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun <T> DropdownSetting(
    label: String,
    currentValue: String,
    options: List<Pair<String, T>>,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                currentValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (text, value) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPalette(
    selectedColor: Color,
    onColorSelected: (Long) -> Unit,
    colors: List<Long> = listOf(
        0xFFFFFFFF, 0xFFE0E0E0, 0xFF90CAF9, 0xFF80CBC4,
        0xFFA5D6A7, 0xFFFFE082, 0xFFFFAB91, 0xFFEF9A9A,
        0xFFCE93D8, 0xFFB0BEC5, 0xFF4CAF50, 0xFFFF9800
    )
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        colors.forEach { colorLong ->
            val color = Color(colorLong)
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else Modifier.border(1.dp, Color.Gray, CircleShape)
                    )
                    .clickable { onColorSelected(colorLong) }
            )
        }
    }
}
