package club.hiraeth.ionosonde.widget

import android.content.Context
import club.hiraeth.ionosonde.data.model.SolarData
import club.hiraeth.ionosonde.data.preferences.AppSettings
import club.hiraeth.ionosonde.data.preferences.UserPreferences
import club.hiraeth.ionosonde.data.repository.IonosondeRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object WidgetDataProvider {

    suspend fun getSolarData(context: Context): SolarData? {
        return IonosondeRepository.getInstance(context).getSolarData()
    }

    suspend fun getSettings(context: Context): AppSettings {
        return UserPreferences.getInstance(context).getSettings()
    }

    fun formatLastUpdated(data: SolarData): String {
        return data.updated.ifEmpty {
            SimpleDateFormat("HH:mm", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(Date(data.fetchedAt)) + " UTC"
        }
    }
}
