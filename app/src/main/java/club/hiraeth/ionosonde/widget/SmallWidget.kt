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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import club.hiraeth.ionosonde.ui.MainActivity
import club.hiraeth.ionosonde.ui.theme.kIndexColor

class SmallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataProvider.getSolarData(context)
        val settings = WidgetDataProvider.getSettings(context)

        val kIndex = data?.kIndex ?: 0
        val geoMag = data?.geomagneticField ?: "N/A"

        val bgAlpha = (settings.widgetBgAlpha * 255 / 100).toLong()
        val bgRgb = settings.widgetBgColor and 0x00FFFFFF
        val bgColor = Color((bgAlpha shl 24) or bgRgb)

        val kColor = if (settings.useFixedConditionColors) {
            kIndexColor(kIndex)
        } else {
            Color(settings.widgetAccentColor)
        }

        val textColor = Color(settings.widgetTextColor)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bgColor)
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "K$kIndex",
                        style = TextStyle(
                            color = ColorProvider(kColor),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = geoMag.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(textColor),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

class SmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWidget()
}
