package club.hiraeth.ionosonde.data.remote

import club.hiraeth.ionosonde.data.model.SolarData
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object SolarXmlParser {

    fun parse(xml: String): SolarData {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var updated = ""
        var sfi = 0; var sn = 0; var aIndex = 0; var kIndex = 0
        var xRay = ""; var protonFlux = ""; var electronFlux = ""
        var solarWind = ""; var bz = ""; var geoMag = ""
        var signalNoise = ""; var auroraAct = ""; var auroraLat = ""
        var flareProbability = ""

        var b80_40Day = ""; var b30_20Day = ""; var b17_15Day = ""; var b12_10Day = ""
        var b80_40Night = ""; var b30_20Night = ""; var b17_15Night = ""; var b12_10Night = ""

        var vhfAurora = ""; var vhf6mEsEU = ""; var vhf4mEsEU = ""
        var vhf2mEsEU = ""; var vhf2mEsNA = ""; var vhfEmeDeg = ""; var mufBar = ""

        var mufBoulder = ""; var mufAlaska = ""; var mufSondrestrom = ""
        var mufTromso = ""; var mufAthens = ""; var mufAscension = ""
        var mufHermanus = ""; var mufDarwin = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "updated" -> updated = parser.nextText()
                    "solarflux" -> sfi = parser.nextText().trim().toIntOrNull() ?: 0
                    "sunspots" -> sn = parser.nextText().trim().toIntOrNull() ?: 0
                    "aindex" -> aIndex = parser.nextText().trim().toIntOrNull() ?: 0
                    "kindex" -> kIndex = parser.nextText().trim().toIntOrNull() ?: 0
                    "xray" -> xRay = parser.nextText().trim()
                    "protonflux" -> protonFlux = parser.nextText().trim()
                    "electonflux", "electronflux" -> electronFlux = parser.nextText().trim()
                    "solarwind" -> solarWind = parser.nextText().trim()
                    "magneticfield", "bz" -> bz = parser.nextText().trim()
                    "geomagfield" -> geoMag = parser.nextText().trim()
                    "signalnoise" -> signalNoise = parser.nextText().trim()
                    "aurora" -> auroraAct = parser.nextText().trim()
                    "latdegree" -> auroraLat = parser.nextText().trim()
                    "solarflareprobability", "flareprobability" -> flareProbability = parser.nextText().trim()
                    "calculatedconditions" -> {
                        // parse band conditions from attributes
                    }
                    "band" -> {
                        val name = parser.getAttributeValue(null, "name") ?: ""
                        val time = parser.getAttributeValue(null, "time") ?: ""
                        val condition = parser.nextText().trim()
                        when {
                            name.contains("80m-40m") && time == "day" -> b80_40Day = condition
                            name.contains("80m-40m") && time == "night" -> b80_40Night = condition
                            name.contains("30m-20m") && time == "day" -> b30_20Day = condition
                            name.contains("30m-20m") && time == "night" -> b30_20Night = condition
                            name.contains("17m-15m") && time == "day" -> b17_15Day = condition
                            name.contains("17m-15m") && time == "night" -> b17_15Night = condition
                            name.contains("12m-10m") && time == "day" -> b12_10Day = condition
                            name.contains("12m-10m") && time == "night" -> b12_10Night = condition
                        }
                    }
                    "phenomenon" -> {
                        val name = parser.getAttributeValue(null, "name") ?: ""
                        val location = parser.getAttributeValue(null, "location") ?: ""
                        val condition = parser.nextText().trim()
                        when {
                            name.contains("Aurora", ignoreCase = true) -> vhfAurora = condition
                            name.contains("6m", ignoreCase = true) && location.contains("eu", ignoreCase = true) -> vhf6mEsEU = condition
                            name.contains("4m", ignoreCase = true) && location.contains("eu", ignoreCase = true) -> vhf4mEsEU = condition
                            name.contains("2m", ignoreCase = true) && location.contains("eu", ignoreCase = true) -> vhf2mEsEU = condition
                            name.contains("2m", ignoreCase = true) && location.contains("na", ignoreCase = true) -> vhf2mEsNA = condition
                            name.contains("EME", ignoreCase = true) -> vhfEmeDeg = condition
                        }
                    }
                    "muf" -> {
                        val station = parser.getAttributeValue(null, "station") ?: ""
                        val value = parser.nextText().trim()
                        when {
                            station.contains("Boulder", ignoreCase = true) -> mufBoulder = value
                            station.contains("Alaska", ignoreCase = true) -> mufAlaska = value
                            station.contains("Sondrestrom", ignoreCase = true) -> mufSondrestrom = value
                            station.contains("Tromso", ignoreCase = true) || station.contains("Tromsø", ignoreCase = true) -> mufTromso = value
                            station.contains("Athens", ignoreCase = true) -> mufAthens = value
                            station.contains("Ascension", ignoreCase = true) -> mufAscension = value
                            station.contains("Hermanus", ignoreCase = true) -> mufHermanus = value
                            station.contains("Darwin", ignoreCase = true) -> mufDarwin = value
                        }
                    }
                    "mufbar" -> mufBar = parser.nextText().trim()
                }
            }
            eventType = parser.next()
        }

        return SolarData(
            updated = updated,
            solarFluxIndex = sfi,
            sunspotNumber = sn,
            aIndex = aIndex,
            kIndex = kIndex,
            xRayClass = xRay,
            protonFlux = protonFlux,
            electronFlux = electronFlux,
            solarWindSpeed = solarWind,
            bzStatus = bz,
            geomagneticField = geoMag,
            signalNoiseLevel = signalNoise,
            auroraActivity = auroraAct,
            auroraLatitude = auroraLat,
            solarFlareProbability = flareProbability,
            band80_40Day = b80_40Day,
            band30_20Day = b30_20Day,
            band17_15Day = b17_15Day,
            band12_10Day = b12_10Day,
            band80_40Night = b80_40Night,
            band30_20Night = b30_20Night,
            band17_15Night = b17_15Night,
            band12_10Night = b12_10Night,
            vhfAurora = vhfAurora,
            vhf6mEsEU = vhf6mEsEU,
            vhf4mEsEU = vhf4mEsEU,
            vhf2mEsEU = vhf2mEsEU,
            vhf2mEsNA = vhf2mEsNA,
            vhfEmeDeg = vhfEmeDeg,
            mufBar = mufBar,
            mufBoulder = mufBoulder,
            mufAlaska = mufAlaska,
            mufSondrestrom = mufSondrestrom,
            mufTromso = mufTromso,
            mufAthens = mufAthens,
            mufAscension = mufAscension,
            mufHermanus = mufHermanus,
            mufDarwin = mufDarwin
        )
    }
}
