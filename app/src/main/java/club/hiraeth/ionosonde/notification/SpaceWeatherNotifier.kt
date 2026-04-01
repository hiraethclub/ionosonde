package club.hiraeth.ionosonde.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import club.hiraeth.ionosonde.R
import club.hiraeth.ionosonde.data.model.SolarData
import club.hiraeth.ionosonde.ui.MainActivity
import club.hiraeth.ionosonde.data.preferences.UserPreferences

object SpaceWeatherNotifier {
    private const val CHANNEL_ID = "space_weather_alerts"
    private const val NOTIFICATION_K_INDEX = 1001
    private const val NOTIFICATION_FLARE = 1002
    private const val NOTIFICATION_PROTON = 1003

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Space Weather Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerts for significant space weather events affecting radio propagation"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    suspend fun checkAndNotify(context: Context, previous: SolarData?, current: SolarData) {
        val prefs = UserPreferences.getInstance(context)
        val settings = prefs.getSettings()

        if (!hasNotificationPermission(context)) return

        // K-index alert
        if (current.kIndex >= settings.kIndexAlertLevel) {
            if (previous == null || previous.kIndex < settings.kIndexAlertLevel) {
                sendNotification(
                    context,
                    NOTIFICATION_K_INDEX,
                    "K-Index Alert: K${current.kIndex}",
                    getKIndexExplanation(current.kIndex)
                )
            }
        }

        // Flare class alert
        val flareLevel = xRayClassLevel(current.xRayClass)
        val thresholdLevel = xRayClassLevel(settings.flareClassAlert)
        if (flareLevel >= thresholdLevel && flareLevel >= 3) {
            if (previous == null || xRayClassLevel(previous.xRayClass) < thresholdLevel) {
                sendNotification(
                    context,
                    NOTIFICATION_FLARE,
                    "Solar Flare Alert: ${current.xRayClass}",
                    getFlareExplanation(current.xRayClass)
                )
            }
        }

        // Proton event alert
        if (settings.protonEventAlert) {
            val protonValue = current.protonFlux.toDoubleOrNull() ?: 0.0
            if (protonValue >= 10.0) {
                val previousProton = previous?.protonFlux?.toDoubleOrNull() ?: 0.0
                if (previousProton < 10.0) {
                    sendNotification(
                        context,
                        NOTIFICATION_PROTON,
                        "Proton Event Alert: ${current.protonFlux} pfu",
                        "Elevated proton flux may cause polar cap absorption, degrading HF propagation on polar paths."
                    )
                }
            }
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun sendNotification(context: Context, id: Int, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun xRayClassLevel(xRayClass: String): Int {
        val c = xRayClass.uppercase().firstOrNull() ?: return 0
        return when (c) {
            'A' -> 1
            'B' -> 2
            'C' -> 3
            'M' -> 4
            'X' -> 5
            else -> 0
        }
    }

    private fun getKIndexExplanation(k: Int): String = when {
        k >= 7 -> "Severe geomagnetic storm (K$k). HF propagation severely disrupted. Expect blackouts on most bands. Aurora may be visible at low latitudes."
        k >= 5 -> "Geomagnetic storm (K$k). HF propagation significantly disturbed, especially on higher bands and polar paths. Aurora possible at mid-latitudes."
        k >= 4 -> "Unsettled geomagnetic conditions (K$k). Some degradation of HF propagation possible, particularly on polar and high-latitude paths."
        else -> "K-index at $k."
    }

    private fun getFlareExplanation(xRayClass: String): String {
        val c = xRayClass.uppercase().firstOrNull() ?: return "Solar flare detected."
        return when (c) {
            'X' -> "Major X-class flare ($xRayClass). Expect HF radio blackouts on the sunlit side of Earth. Shortwave fadeout likely."
            'M' -> "Moderate M-class flare ($xRayClass). Possible brief HF radio blackouts, especially on lower frequencies on the sunlit side."
            else -> "Solar flare detected: $xRayClass."
        }
    }
}
