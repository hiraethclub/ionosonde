package club.hiraeth.ionosonde

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import club.hiraeth.ionosonde.data.preferences.UserPreferences
import club.hiraeth.ionosonde.data.worker.SolarDataWorker
import club.hiraeth.ionosonde.notification.SpaceWeatherNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class IonosondeApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        SpaceWeatherNotifier.createChannel(this)

        // Schedule periodic data refresh
        scheduleDataRefresh()
    }

    private fun scheduleDataRefresh() {
        appScope.launch {
            val settings = UserPreferences.getInstance(this@IonosondeApp).observeSettings().first()
            val intervalMinutes = settings.refreshIntervalMinutes.toLong()

            if (intervalMinutes <= 0) {
                WorkManager.getInstance(this@IonosondeApp)
                    .cancelUniqueWork(SolarDataWorker.WORK_NAME)
                return@launch
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SolarDataWorker>(
                intervalMinutes.coerceAtLeast(15), TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this@IonosondeApp)
                .enqueueUniquePeriodicWork(
                    SolarDataWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}
