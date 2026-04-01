package club.hiraeth.ionosonde.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solar_data")
data class SolarData(
    @PrimaryKey val id: Int = 1,
    val updated: String = "",
    val solarFluxIndex: Int = 0,
    val sunspotNumber: Int = 0,
    val aIndex: Int = 0,
    val kIndex: Int = 0,
    val xRayClass: String = "",
    val protonFlux: String = "",
    val electronFlux: String = "",
    val solarWindSpeed: String = "",
    val bzStatus: String = "",
    val geomagneticField: String = "",
    val signalNoiseLevel: String = "",
    val auroraActivity: String = "",
    val auroraLatitude: String = "",
    val solarFlareProbability: String = "",
    // HF Band conditions - Day
    val band80_40Day: String = "",
    val band30_20Day: String = "",
    val band17_15Day: String = "",
    val band12_10Day: String = "",
    // HF Band conditions - Night
    val band80_40Night: String = "",
    val band30_20Night: String = "",
    val band17_15Night: String = "",
    val band12_10Night: String = "",
    // VHF conditions
    val vhfAurora: String = "",
    val vhf6mEsEU: String = "",
    val vhf4mEsEU: String = "",
    val vhf2mEsEU: String = "",
    val vhf2mEsNA: String = "",
    val vhfEmeDeg: String = "",
    val mufBar: String = "",
    // MUF station values
    val mufBoulder: String = "",
    val mufAlaska: String = "",
    val mufSondrestrom: String = "",
    val mufTromso: String = "",
    val mufAthens: String = "",
    val mufAscension: String = "",
    val mufHermanus: String = "",
    val mufDarwin: String = "",
    val fetchedAt: Long = System.currentTimeMillis()
)
