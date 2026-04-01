package club.hiraeth.ionosonde.ui.map

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Calculates solar terminator and subsolar point from UTC time.
 * Uses standard astronomical formulae for solar declination and hour angle.
 */
object SolarCalculator {

    data class SubsolarPoint(val latitude: Double, val longitude: Double)

    data class TerminatorPoint(val latitude: Double, val longitude: Double)

    private const val RAD = PI / 180.0
    private const val DEG = 180.0 / PI

    /**
     * Calculate the subsolar point for a given time in millis (UTC).
     */
    fun subsolarPoint(timeMillis: Long): SubsolarPoint {
        val jd = toJulianDate(timeMillis)
        val n = jd - 2451545.0 // days since J2000.0

        // Mean longitude of the Sun (degrees)
        val L = (280.460 + 0.9856474 * n) % 360.0
        // Mean anomaly of the Sun (degrees)
        val g = ((357.528 + 0.9856003 * n) % 360.0) * RAD
        // Ecliptic longitude (degrees)
        val lambda = L + 1.915 * sin(g) + 0.020 * sin(2 * g)
        // Obliquity of the ecliptic (degrees)
        val epsilon = 23.439 - 0.0000004 * n

        // Solar declination
        val declination = asin(sin(epsilon * RAD) * sin(lambda * RAD)) * DEG

        // Equation of time (minutes)
        val lambdaRad = lambda * RAD
        val epsilonRad = epsilon * RAD
        val y = kotlin.math.tan(epsilonRad / 2).let { it * it }
        val gMean = g
        val eot = 4 * DEG * (
            y * sin(2 * L * RAD) -
            2 * 0.01671 * sin(gMean) +
            4 * 0.01671 * y * sin(gMean) * cos(2 * L * RAD) -
            0.5 * y * y * sin(4 * L * RAD) -
            1.25 * 0.01671 * 0.01671 * sin(2 * gMean)
        )

        // Hour of day in UTC
        val utcHours = ((timeMillis % 86400000L) / 3600000.0)
        // Subsolar longitude
        val longitude = -(utcHours - 12.0 + eot / 60.0) * 15.0

        return SubsolarPoint(
            latitude = declination,
            longitude = ((longitude + 540) % 360) - 180
        )
    }

    /**
     * Calculate terminator points (the day/night boundary).
     * Returns a list of (lat, lon) points along the terminator.
     */
    fun terminatorPoints(timeMillis: Long, numPoints: Int = 360): List<TerminatorPoint> {
        val subsolar = subsolarPoint(timeMillis)
        val decRad = subsolar.latitude * RAD
        val lonSub = subsolar.longitude

        return (0 until numPoints).map { i ->
            val angle = (i * 360.0 / numPoints) * RAD
            val lat = asin(cos(angle) * cos(decRad)) * DEG
            val lon = lonSub + atan2(
                sin(angle),
                -cos(angle) * sin(decRad) / cos(lat * RAD)
            ) * DEG
            val normalizedLon = ((lon + 540) % 360) - 180
            TerminatorPoint(lat, normalizedLon)
        }
    }

    /**
     * Check if a point is on the night side.
     */
    fun isNightSide(lat: Double, lon: Double, subsolar: SubsolarPoint): Boolean {
        val latRad = lat * RAD
        val lonRad = lon * RAD
        val subLatRad = subsolar.latitude * RAD
        val subLonRad = subsolar.longitude * RAD

        // Angular distance from subsolar point
        val cosAngle = sin(latRad) * sin(subLatRad) +
                cos(latRad) * cos(subLatRad) * cos(lonRad - subLonRad)
        return cosAngle < 0
    }

    private fun toJulianDate(timeMillis: Long): Double {
        return (timeMillis / 86400000.0) + 2440587.5
    }
}
