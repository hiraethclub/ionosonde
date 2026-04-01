package club.hiraeth.ionosonde.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
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
import club.hiraeth.ionosonde.ui.map.SolarCalculator
import club.hiraeth.ionosonde.ui.theme.ConditionAmber
import club.hiraeth.ionosonde.ui.theme.ConditionGreen
import club.hiraeth.ionosonde.ui.theme.ConditionRed
import club.hiraeth.ionosonde.ui.theme.kIndexColor
import club.hiraeth.ionosonde.ui.theme.sfiColor
import club.hiraeth.ionosonde.ui.theme.xRayColor

class LargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataProvider.getSolarData(context)
        val settings = WidgetDataProvider.getSettings(context)

        val bgAlpha = (settings.widgetBgAlpha * 255 / 100).toLong()
        val bgRgb = settings.widgetBgColor and 0x00FFFFFF
        val bgColor = Color((bgAlpha shl 24) or bgRgb)
        val textColor = Color(settings.widgetTextColor)
        val useFixed = settings.useFixedConditionColors
        val accent = Color(settings.widgetAccentColor)

        // Generate terminator map bitmap
        val mapBitmap = generateMapBitmap(360, 180)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bgColor)
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(6.dp)
                ) {
                    if (data == null) {
                        Text("No data", style = TextStyle(color = ColorProvider(textColor)))
                        return@Column
                    }

                    // Row 1: Primary indices
                    Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        LargeIndexCell("SFI", data.solarFluxIndex.toString(), if (useFixed) sfiColor(data.solarFluxIndex) else accent, textColor)
                        Spacer(GlanceModifier.width(6.dp))
                        LargeIndexCell("SN", data.sunspotNumber.toString(), textColor, textColor)
                        Spacer(GlanceModifier.width(6.dp))
                        LargeIndexCell("A", data.aIndex.toString(), textColor, textColor)
                        Spacer(GlanceModifier.width(6.dp))
                        LargeIndexCell("K", data.kIndex.toString(), if (useFixed) kIndexColor(data.kIndex) else accent, textColor)
                    }

                    // Row 2: Secondary indices
                    Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        LargeIndexCell("X-Ray", data.xRayClass, if (useFixed) xRayColor(data.xRayClass) else accent, textColor)
                        Spacer(GlanceModifier.width(4.dp))
                        LargeIndexCell("Proton", data.protonFlux, textColor, textColor)
                        Spacer(GlanceModifier.width(4.dp))
                        LargeIndexCell("e-Flux", data.electronFlux, textColor, textColor)
                        Spacer(GlanceModifier.width(4.dp))
                        LargeIndexCell("Wind", data.solarWindSpeed, textColor, textColor)
                    }

                    Spacer(GlanceModifier.height(2.dp))

                    // Band conditions
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text("Band", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp), modifier = GlanceModifier.defaultWeight())
                        Text("Day", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp), modifier = GlanceModifier.defaultWeight())
                        Text("Night", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp), modifier = GlanceModifier.defaultWeight())
                    }
                    LargeBandRow("80-40m", data.band80_40Day, data.band80_40Night, textColor, useFixed, accent)
                    LargeBandRow("30-20m", data.band30_20Day, data.band30_20Night, textColor, useFixed, accent)
                    LargeBandRow("17-15m", data.band17_15Day, data.band17_15Night, textColor, useFixed, accent)
                    LargeBandRow("12-10m", data.band12_10Day, data.band12_10Night, textColor, useFixed, accent)

                    Spacer(GlanceModifier.height(2.dp))

                    // VHF section
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text("VHF:", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp))
                        Spacer(GlanceModifier.width(4.dp))
                        Text("6m:${data.vhf6mEsEU}", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp))
                        Spacer(GlanceModifier.width(4.dp))
                        Text("4m:${data.vhf4mEsEU}", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp))
                        Spacer(GlanceModifier.width(4.dp))
                        Text("2m:${data.vhf2mEsEU}", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp))
                    }

                    // Flare probability and aurora
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text("Flare: ${data.solarFlareProbability.ifEmpty { "N/A" }}", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp))
                        Spacer(GlanceModifier.width(8.dp))
                        Text("Aurora: ${data.auroraLatitude.ifEmpty { "N/A" }}", style = TextStyle(color = ColorProvider(textColor), fontSize = 8.sp))
                    }

                    Spacer(GlanceModifier.height(2.dp))

                    // Simplified sunlit map
                    Image(
                        provider = ImageProvider(mapBitmap),
                        contentDescription = "Solar terminator map",
                        modifier = GlanceModifier.fillMaxWidth().height(80.dp),
                        contentScale = ContentScale.FillBounds
                    )

                    Spacer(GlanceModifier.height(2.dp))

                    Text(
                        text = WidgetDataProvider.formatLastUpdated(data),
                        style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.7f)), fontSize = 7.sp)
                    )
                }
            }
        }
    }

    private fun generateMapBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background: ocean blue
        canvas.drawColor(android.graphics.Color.rgb(40, 70, 120))

        // Draw simplified continent outlines (rectangular approximations)
        val landPaint = Paint().apply {
            color = android.graphics.Color.rgb(60, 100, 60)
            style = Paint.Style.FILL
        }

        // Simplified continents (approximate bounding rectangles)
        // North America
        drawRegion(canvas, landPaint, width, height, -170.0, 15.0, -50.0, 72.0)
        // South America
        drawRegion(canvas, landPaint, width, height, -82.0, -56.0, -34.0, 13.0)
        // Europe
        drawRegion(canvas, landPaint, width, height, -10.0, 36.0, 40.0, 71.0)
        // Africa
        drawRegion(canvas, landPaint, width, height, -18.0, -35.0, 52.0, 37.0)
        // Asia
        drawRegion(canvas, landPaint, width, height, 40.0, 10.0, 150.0, 72.0)
        // Australia
        drawRegion(canvas, landPaint, width, height, 113.0, -44.0, 154.0, -10.0)

        // Draw terminator shading
        val timeMillis = System.currentTimeMillis()
        val subsolar = SolarCalculator.subsolarPoint(timeMillis)
        val nightPaint = Paint().apply {
            color = android.graphics.Color.argb(120, 0, 0, 30)
            style = Paint.Style.FILL
        }

        // Shade night side pixel by pixel (simplified for widget)
        for (px in 0 until width step 2) {
            for (py in 0 until height step 2) {
                val lon = (px.toDouble() / width) * 360.0 - 180.0
                val lat = 90.0 - (py.toDouble() / height) * 180.0
                if (SolarCalculator.isNightSide(lat, lon, subsolar)) {
                    canvas.drawRect(px.toFloat(), py.toFloat(), (px + 2).toFloat(), (py + 2).toFloat(), nightPaint)
                }
            }
        }

        // Mark subsolar point
        val sunPaint = Paint().apply {
            color = android.graphics.Color.rgb(255, 179, 71)
            style = Paint.Style.FILL
        }
        val sunX = ((subsolar.longitude + 180) / 360 * width).toFloat()
        val sunY = ((90 - subsolar.latitude) / 180 * height).toFloat()
        canvas.drawCircle(sunX, sunY, 4f, sunPaint)

        return bitmap
    }

    private fun drawRegion(canvas: Canvas, paint: Paint, w: Int, h: Int, lonMin: Double, latMin: Double, lonMax: Double, latMax: Double) {
        val x1 = ((lonMin + 180) / 360 * w).toFloat()
        val x2 = ((lonMax + 180) / 360 * w).toFloat()
        val y1 = ((90 - latMax) / 180 * h).toFloat()
        val y2 = ((90 - latMin) / 180 * h).toFloat()
        canvas.drawRect(x1, y1, x2, y2, paint)
    }
}

@androidx.compose.runtime.Composable
private fun LargeIndexCell(label: String, value: String, valueColor: Color, labelColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = TextStyle(color = ColorProvider(labelColor), fontSize = 8.sp))
        Text(value, style = TextStyle(color = ColorProvider(valueColor), fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}

@androidx.compose.runtime.Composable
private fun LargeBandRow(band: String, day: String, night: String, textColor: Color, useFixed: Boolean, accent: Color) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Text(band, style = TextStyle(color = ColorProvider(textColor), fontSize = 9.sp), modifier = GlanceModifier.defaultWeight())
        Text(day, style = TextStyle(color = ColorProvider(largeBandColor(day, useFixed, accent)), fontSize = 9.sp), modifier = GlanceModifier.defaultWeight())
        Text(night, style = TextStyle(color = ColorProvider(largeBandColor(night, useFixed, accent)), fontSize = 9.sp), modifier = GlanceModifier.defaultWeight())
    }
}

private fun largeBandColor(status: String, useFixed: Boolean, accent: Color): Color {
    if (!useFixed) return accent
    return when (status.lowercase().trim()) {
        "good" -> ConditionGreen
        "fair" -> ConditionAmber
        "poor" -> ConditionRed
        else -> ConditionAmber
    }
}

class LargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeWidget()
}
