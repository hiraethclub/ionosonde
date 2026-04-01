package club.hiraeth.ionosonde.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    var showExplanation by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                if (IndexExplanations.explanations.containsKey(label)) {
                    showExplanation = true
                }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }

    if (showExplanation) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showExplanation = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = IndexExplanations.explanations[label] ?: "No description available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun BandConditionRow(
    band: String,
    dayCondition: String,
    nightCondition: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = band,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        ConditionChip(
            text = dayCondition,
            modifier = Modifier.weight(0.8f)
        )
        ConditionChip(
            text = nightCondition,
            modifier = Modifier.weight(0.8f)
        )
    }
}

@Composable
fun ConditionChip(text: String, modifier: Modifier = Modifier) {
    val color = conditionStatusColor(text)
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.ifEmpty { "N/A" },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VhfConditionRow(label: String, status: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        ConditionChip(
            text = status,
            modifier = Modifier.weight(0.8f)
        )
    }
}

@Composable
fun LastUpdatedText(timestamp: String, modifier: Modifier = Modifier) {
    Text(
        text = "Last updated: $timestamp UTC",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(4.dp)
    )
}

private fun conditionStatusColor(status: String): Color {
    val s = status.lowercase().trim()
    return when {
        s == "good" || s == "band open" || s == "high" || s == "active" -> club.hiraeth.ionosonde.ui.theme.ConditionGreen
        s == "fair" || s == "moderate" || s == "normal" -> club.hiraeth.ionosonde.ui.theme.ConditionAmber
        s == "poor" || s == "closed" || s == "band closed" || s == "low" -> club.hiraeth.ionosonde.ui.theme.ConditionRed
        else -> club.hiraeth.ionosonde.ui.theme.ConditionAmber
    }
}
