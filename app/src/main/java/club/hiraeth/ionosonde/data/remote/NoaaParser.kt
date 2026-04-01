package club.hiraeth.ionosonde.data.remote

import club.hiraeth.ionosonde.data.model.KIndexEntry
import club.hiraeth.ionosonde.data.model.KIndexForecastEntry
import club.hiraeth.ionosonde.data.model.SfiForecastEntry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object NoaaParser {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parses the NOAA planetary K-index JSON.
     * Format: array of JSON objects with fields: time_tag, Kp, a_running, station_count
     */
    fun parseKIndexHistory(raw: String): List<KIndexEntry> {
        val array = json.decodeFromString<JsonArray>(raw)
        return array.mapNotNull { element ->
            val obj = element.jsonObject
            val timestamp = obj["time_tag"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val kp = obj["Kp"]?.jsonPrimitive?.double ?: return@mapNotNull null
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
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            // Skip comment and metadata lines
            if (trimmed.startsWith(":") || trimmed.startsWith("#")) continue
            // Only parse lines that start with a year (4 digits)
            if (!trimmed.matches(Regex("^\\d{4}\\s+.*"))) continue
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
     * Format: array of JSON objects with fields: time_tag, kp (lowercase), observed, noaa_scale
     */
    fun parseKIndexForecast(raw: String): List<KIndexForecastEntry> {
        val array = json.decodeFromString<JsonArray>(raw)
        return array.mapNotNull { element ->
            val obj = element.jsonObject
            val timestamp = obj["time_tag"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val kp = obj["kp"]?.jsonPrimitive?.double ?: return@mapNotNull null
            KIndexForecastEntry(timestamp = timestamp, kIndex = kp)
        }
    }
}
