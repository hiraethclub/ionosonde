package club.hiraeth.ionosonde.data.repository

import android.content.Context
import club.hiraeth.ionosonde.data.local.IonosondeDatabase
import club.hiraeth.ionosonde.data.model.KIndexEntry
import club.hiraeth.ionosonde.data.model.KIndexForecastEntry
import club.hiraeth.ionosonde.data.model.SfiForecastEntry
import club.hiraeth.ionosonde.data.model.SolarData
import club.hiraeth.ionosonde.data.remote.NoaaParser
import club.hiraeth.ionosonde.data.remote.RetrofitClient
import club.hiraeth.ionosonde.data.remote.SolarXmlParser
import kotlinx.coroutines.flow.Flow

class IonosondeRepository(context: Context) {
    private val dao = IonosondeDatabase.getInstance(context).ionosondeDao()
    private val hamqslApi = RetrofitClient.hamqslApi
    private val noaaApi = RetrofitClient.noaaApi

    fun observeSolarData(): Flow<SolarData?> = dao.observeSolarData()
    fun observeKIndexHistory(): Flow<List<KIndexEntry>> = dao.observeKIndexHistory()
    fun observeSfiForecast(): Flow<List<SfiForecastEntry>> = dao.observeSfiForecast()
    fun observeKIndexForecast(): Flow<List<KIndexForecastEntry>> = dao.observeKIndexForecast()

    suspend fun getSolarData(): SolarData? = dao.getSolarData()
    suspend fun getKIndexHistory(): List<KIndexEntry> = dao.getKIndexHistory()

    suspend fun refreshSolarData(): SolarData {
        val xml = hamqslApi.getSolarXml()
        val data = SolarXmlParser.parse(xml)
        dao.insertSolarData(data)
        return data
    }

    suspend fun refreshKIndexHistory(): List<KIndexEntry> {
        val raw = noaaApi.getKIndexHistory()
        val entries = NoaaParser.parseKIndexHistory(raw)
        dao.clearKIndexHistory()
        dao.insertKIndexEntries(entries)
        return entries
    }

    suspend fun refreshSfiForecast(): List<SfiForecastEntry> {
        val raw = noaaApi.getSfiForecast()
        val entries = NoaaParser.parseSfiForecast(raw)
        dao.clearSfiForecast()
        dao.insertSfiForecast(entries)
        return entries
    }

    suspend fun refreshKIndexForecast(): List<KIndexForecastEntry> {
        val raw = noaaApi.getKIndexForecast()
        val entries = NoaaParser.parseKIndexForecast(raw)
        dao.clearKIndexForecast()
        dao.insertKIndexForecast(entries)
        return entries
    }

    suspend fun refreshAll() {
        refreshSolarData()
        refreshKIndexHistory()
        refreshSfiForecast()
        refreshKIndexForecast()
    }

    companion object {
        @Volatile
        private var INSTANCE: IonosondeRepository? = null

        fun getInstance(context: Context): IonosondeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: IonosondeRepository(context.applicationContext)
                    .also { INSTANCE = it }
            }
        }
    }
}
