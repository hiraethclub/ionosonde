package club.hiraeth.ionosonde.data.remote

import club.hiraeth.ionosonde.data.model.KIndexEntry
import club.hiraeth.ionosonde.data.model.KIndexForecastEntry
import club.hiraeth.ionosonde.data.model.SfiForecastEntry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object NoaaParser {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parses the NOAA planetary K-index JSON.
     * Format: array of arrays, first element is header row.
     * Each row: [time_tag, Kp, Kp_fraction, a_running, station_count]
     */
    fun parseKIndexHistory(raw: String): List<KIndexEntry> {
        val array = json.decodeFromString<JsonArray>(raw)
        return array.drop(1).mapNotNull { element ->
            val row = element.jsonArray
            val timestamp = row[0].jsonPrimitive.content
            val kp = row[1].jsonPrimitive.content.toDoubleOrNull() ?: return@mapNotNull null
            KIndexEntry(timestamp = timestamp, kIndex = kp)
        }
    }

    /**
     * Parses the 27-day SFI outlook text.
     * Lines with format: YYYY Mon DD  SFI  AP
     */
    fun parseSfiForecast(raw: String): List<SfiForecastEntry> {
        val entries = mutableListOf<SfiForecastEntry>()
        val lines = raw.lines()
        var inData = false
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            // Detect the data section after the header
            if (trimmed.startsWith(":'m")) {
                inData = true
                continue
            }
            if (trimmed.startsWith(":")) continue
            if (!inData) {
                // Check if this line looks like data: starts with 4 digits (year)
                if (trimmed.matches(Regex("^\\d{4}\\s+.*"))) {
                    inData = true
                } else {
                    continue
                }
            }
            // Parse data line: "2024 Jan 15   150   8"
            val parts = trimmed.split(Regex("\\s+"))
            if (parts.size >= 4) {
                val year = parts[0]
                val month = parts[1]
                val day = parts[2]
                val sfi = parts[3].toIntOrNull() ?: continue
                val date = "$year $month $day"
                entries.add(SfiForecastEntry(date = date, sfi = sfi))
            }
        }
        return entries
    }

    /**
     * Parses the K-index forecast JSON.
     * Format: array of arrays, first element is header row.
     * Each row: [time_tag, Kp, observed/estimated, noaa_scale]
     */
    fun parseKIndexForecast(raw: String): List<KIndexForecastEntry> {
        val array = json.decodeFromString<JsonArray>(raw)
        return array.drop(1).mapNotNull { element ->
            val row = element.jsonArray
            val timestamp = row[0].jsonPrimitive.content
            val kp = row[1].jsonPrimitive.content.toDoubleOrNull() ?: return@mapNotNull null
            KIndexForecastEntry(timestamp = timestamp, kIndex = kp)
        }
    }
}
