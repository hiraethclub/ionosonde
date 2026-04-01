package club.hiraeth.ionosonde.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "k_index_forecast")
data class KIndexForecastEntry(
    @PrimaryKey val timestamp: String,
    val kIndex: Double,
    val fetchedAt: Long = System.currentTimeMillis()
)
