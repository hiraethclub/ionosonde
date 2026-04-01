package club.hiraeth.ionosonde.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import club.hiraeth.ionosonde.ui.MainActivity
import club.hiraeth.ionosonde.ui.theme.ConditionAmber
import club.hiraeth.ionosonde.ui.theme.ConditionGreen
import club.hiraeth.ionosonde.ui.theme.ConditionRed
import club.hiraeth.ionosonde.ui.theme.kIndexColor
import club.hiraeth.ionosonde.ui.theme.sfiColor
import club.hiraeth.ionosonde.ui.theme.xRayColor

class MediumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataProvider.getSolarData(context)
        val settings = WidgetDataProvider.getSettings(context)

        val bgAlpha = (settings.widgetBgAlpha * 255 / 100)
        val bgColor = Color(
            red = ((settings.widgetBgColor shr 16) and 0xFF).toInt(),
            green = ((settings.widgetBgColor shr 8) and 0xFF).toInt(),
            blue = (settings.widgetBgColor and 0xFF).toInt(),
            alpha = bgAlpha
        )
        val textColor = Color(settings.widgetTextColor)
        val useFixed = settings.useFixedConditionColors
        val accent = Color(settings.widgetAccentColor)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bgColor)
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(8.dp)
                ) {
                    if (data == null) {
                        Text("No data", style = TextStyle(color = ColorProvider(textColor), fontSize = 14.sp))
                        return@Column
                    }

                    // Top row: SFI, SN, A, K
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WidgetIndexCell("SFI", data.solarFluxIndex.toString(), if (useFixed) sfiColor(data.solarFluxIndex) else accent, textColor)
                        Spacer(GlanceModifier.width(8.dp))
                        WidgetIndexCell("SN", data.sunspotNumber.toString(), textColor, textColor)
                        Spacer(GlanceModifier.width(8.dp))
                        WidgetIndexCell("A", data.aIndex.toString(), textColor, textColor)
                        Spacer(GlanceModifier.width(8.dp))
                        WidgetIndexCell("K", data.kIndex.toString(), if (useFixed) kIndexColor(data.kIndex) else accent, textColor)
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Band conditions header
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text("Band", style = TextStyle(color = ColorProvider(textColor), fontSize = 9.sp), modifier = GlanceModifier.defaultWeight())
                        Text("Day", style = TextStyle(color = ColorProvider(textColor), fontSize = 9.sp), modifier = GlanceModifier.defaultWeight())
                        Text("Night", style = TextStyle(color = ColorProvider(textColor), fontSize = 9.sp), modifier = GlanceModifier.defaultWeight())
                    }

                    WidgetBandRow("80-40m", data.band80_40Day, data.band80_40Night, textColor, useFixed, accent)
                    WidgetBandRow("30-20m", data.band30_20Day, data.band30_20Night, textColor, useFixed, accent)
                    WidgetBandRow("17-15m", data.band17_15Day, data.band17_15Night, textColor, useFixed, accent)
                    WidgetBandRow("12-10m", data.band12_10Day, data.band12_10Night, textColor, useFixed, accent)

                    Spacer(GlanceModifier.height(2.dp))

                    // Last updated
                    Text(
                        text = WidgetDataProvider.formatLastUpdated(data),
                        style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.7f)), fontSize = 8.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetIndexCell(label: String, value: String, valueColor: Color, labelColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = TextStyle(color = ColorProvider(labelColor), fontSize = 9.sp))
        Text(value, style = TextStyle(color = ColorProvider(valueColor), fontSize = 16.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun WidgetBandRow(band: String, day: String, night: String, textColor: Color, useFixed: Boolean, accent: Color) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Text(band, style = TextStyle(color = ColorProvider(textColor), fontSize = 10.sp), modifier = GlanceModifier.defaultWeight())
        Text(day, style = TextStyle(color = ColorProvider(condColor(day, useFixed, accent)), fontSize = 10.sp), modifier = GlanceModifier.defaultWeight())
        Text(night, style = TextStyle(color = ColorProvider(condColor(night, useFixed, accent)), fontSize = 10.sp), modifier = GlanceModifier.defaultWeight())
    }
}

private fun condColor(status: String, useFixed: Boolean, accent: Color): Color {
    if (!useFixed) return accent
    return when (status.lowercase().trim()) {
        "good" -> ConditionGreen
        "fair" -> ConditionAmber
        "poor" -> ConditionRed
        else -> ConditionAmber
    }
}

class MediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumWidget()
}
