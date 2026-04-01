package club.hiraeth.ionosonde.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sfi_forecast")
data class SfiForecastEntry(
    @PrimaryKey val date: String,
    val sfi: Int,
    val fetchedAt: Long = System.currentTimeMillis()
)
