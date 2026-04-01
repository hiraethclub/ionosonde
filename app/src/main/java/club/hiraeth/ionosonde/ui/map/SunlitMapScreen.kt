package club.hiraeth.ionosonde.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.location.LocationManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import club.hiraeth.ionosonde.ui.components.LastUpdatedText
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

data class MufStation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val mufValue: String
)

data class UserLocation(val lat: Double, val lon: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunlitMapScreen(viewModel: SunlitMapViewModel = viewModel()) {
    val solarData by viewModel.solarData.collectAsState()
    val currentTime by viewModel.currentTimeMillis.collectAsState()
    val context = LocalContext.current

    var userLocation by remember { mutableStateOf<UserLocation?>(null) }

    // Request location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            userLocation = getLastKnownLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            userLocation = getLastKnownLocation(context)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Configure OSMDroid
    remember {
        Configuration.getInstance().apply {
            userAgentValue = "club.hiraeth.ionosonde"
            osmdroidTileCache = context.cacheDir
        }
    }

    val mufStations = remember(solarData) {
        val data = solarData
        if (data != null) {
            listOf(
                MufStation("Boulder, US", 40.0, -105.3, data.mufBoulder.ifEmpty { "NoRpt" }),
                MufStation("Alaska, US", 64.2, -153.0, data.mufAlaska.ifEmpty { "NoRpt" }),
                MufStation("Sondrestrom, GL", 66.9, -50.9, data.mufSondrestrom.ifEmpty { "NoRpt" }),
                MufStation("Tromsø, NO", 69.6, 18.9, data.mufTromso.ifEmpty { "NoRpt" }),
                MufStation("Athens, GR", 37.9, 23.7, data.mufAthens.ifEmpty { "NoRpt" }),
                MufStation("Ascension IS", -7.9, -14.4, data.mufAscension.ifEmpty { "NoRpt" }),
                MufStation("Hermanus, ZA", -34.4, 19.2, data.mufHermanus.ifEmpty { "NoRpt" }),
                MufStation("Darwin, AU", -12.4, 130.8, data.mufDarwin.ifEmpty { "NoRpt" }),
            )
        } else emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Sunlit World Map") })

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(3.0)
                        controller.setCenter(GeoPoint(20.0, 0.0))
                        minZoomLevel = 2.0
                        maxZoomLevel = 8.0
                        setUseDataConnection(true)
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    // Calculate terminator
                    val subsolar = SolarCalculator.subsolarPoint(currentTime)
                    val terminatorPts = SolarCalculator.terminatorPoints(currentTime, 720)

                    // Create night-side polygon
                    val nightPolygon = Polygon(mapView).apply {
                        fillPaint.color = AndroidColor.argb(100, 0, 0, 40)
                        outlinePaint.color = AndroidColor.argb(180, 255, 179, 71)
                        outlinePaint.strokeWidth = 2f
                    }

                    val sorted = terminatorPts.sortedBy { it.longitude }
                    val nightPoints = mutableListOf<GeoPoint>()
                    sorted.forEach { pt ->
                        nightPoints.add(GeoPoint(pt.latitude, pt.longitude))
                    }

                    val nightPole = if (subsolar.latitude >= 0) -90.0 else 90.0
                    if (sorted.isNotEmpty()) {
                        nightPoints.add(GeoPoint(nightPole, sorted.last().longitude))
                        nightPoints.add(GeoPoint(nightPole, sorted.first().longitude))
                        nightPoints.add(GeoPoint(sorted.first().latitude, sorted.first().longitude))
                    }

                    nightPolygon.points = nightPoints
                    mapView.overlays.add(nightPolygon)

                    // Subsolar point marker
                    val sunMarker = Marker(mapView).apply {
                        position = GeoPoint(subsolar.latitude, subsolar.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "☀ Subsolar Point"
                        snippet = "Lat: %.1f° Lon: %.1f°".format(subsolar.latitude, subsolar.longitude)
                    }
                    mapView.overlays.add(sunMarker)

                    // User location marker
                    userLocation?.let { loc ->
                        val isNight = SolarCalculator.isNightSide(loc.lat, loc.lon, subsolar)
                        val userMarker = Marker(mapView).apply {
                            position = GeoPoint(loc.lat, loc.lon)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = "📍 My Location"
                            snippet = "Lat: %.2f° Lon: %.2f° (%s)".format(
                                loc.lat, loc.lon,
                                if (isNight) "Night" else "Day"
                            )
                        }
                        mapView.overlays.add(userMarker)
                    }

                    // MUF station markers
                    mufStations.forEach { station ->
                        val mufText = if (station.mufValue == "NoRpt") "NoRpt" else "${station.mufValue} MHz"
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(station.lat, station.lon)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "${station.name} — MUF: $mufText"
                            snippet = "MUF: $mufText"
                        }
                        mapView.overlays.add(marker)
                    }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Attribution and last updated
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "© OpenStreetMap contributors (ODbL)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            solarData?.let { data ->
                LastUpdatedText(
                    timestamp = data.updated.ifEmpty {
                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                            .format(java.util.Date(data.fetchedAt))
                    }
                )
            }
        }
    }
}

@Suppress("MissingPermission")
private fun getLastKnownLocation(context: Context): UserLocation? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    for (provider in providers) {
        try {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                return UserLocation(location.latitude, location.longitude)
            }
        } catch (_: Exception) {
        }
    }
    return null
}
