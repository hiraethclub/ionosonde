package club.hiraeth.ionosonde.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import club.hiraeth.ionosonde.data.repository.IonosondeRepository
import club.hiraeth.ionosonde.notification.SpaceWeatherNotifier

class SolarDataWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = IonosondeRepository.getInstance(applicationContext)
        return try {
            val previousData = repo.getSolarData()
            repo.refreshAll()
            val newData = repo.getSolarData()
            if (newData != null) {
                SpaceWeatherNotifier.checkAndNotify(applicationContext, previousData, newData)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SolarDataWorker", "Failed to refresh data", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "solar_data_refresh"
    }
}
