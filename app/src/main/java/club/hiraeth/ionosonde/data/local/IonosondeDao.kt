package club.hiraeth.ionosonde.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.hiraeth.ionosonde.data.model.KIndexEntry
import club.hiraeth.ionosonde.data.model.KIndexForecastEntry
import club.hiraeth.ionosonde.data.model.SfiForecastEntry
import club.hiraeth.ionosonde.data.model.SolarData
import kotlinx.coroutines.flow.Flow

@Dao
interface IonosondeDao {
    // Solar Data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolarData(data: SolarData)

    @Query("SELECT * FROM solar_data WHERE id = 1")
    fun observeSolarData(): Flow<SolarData?>

    @Query("SELECT * FROM solar_data WHERE id = 1")
    suspend fun getSolarData(): SolarData?

    // K-Index History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKIndexEntries(entries: List<KIndexEntry>)

    @Query("DELETE FROM k_index_history")
    suspend fun clearKIndexHistory()

    @Query("SELECT * FROM k_index_history ORDER BY timestamp ASC")
    fun observeKIndexHistory(): Flow<List<KIndexEntry>>

    @Query("SELECT * FROM k_index_history ORDER BY timestamp ASC")
    suspend fun getKIndexHistory(): List<KIndexEntry>

    // SFI Forecast
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSfiForecast(entries: List<SfiForecastEntry>)

    @Query("DELETE FROM sfi_forecast")
    suspend fun clearSfiForecast()

    @Query("SELECT * FROM sfi_forecast ORDER BY date ASC")
    fun observeSfiForecast(): Flow<List<SfiForecastEntry>>

    // K-Index Forecast
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKIndexForecast(entries: List<KIndexForecastEntry>)

    @Query("DELETE FROM k_index_forecast")
    suspend fun clearKIndexForecast()

    @Query("SELECT * FROM k_index_forecast ORDER BY timestamp ASC")
    fun observeKIndexForecast(): Flow<List<KIndexForecastEntry>>
}
