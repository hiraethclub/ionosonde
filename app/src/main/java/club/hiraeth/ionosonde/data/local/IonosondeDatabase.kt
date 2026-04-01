package club.hiraeth.ionosonde.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import club.hiraeth.ionosonde.data.model.KIndexEntry
import club.hiraeth.ionosonde.data.model.KIndexForecastEntry
import club.hiraeth.ionosonde.data.model.SfiForecastEntry
import club.hiraeth.ionosonde.data.model.SolarData

@Database(
    entities = [
        SolarData::class,
        KIndexEntry::class,
        SfiForecastEntry::class,
        KIndexForecastEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IonosondeDatabase : RoomDatabase() {
    abstract fun ionosondeDao(): IonosondeDao

    companion object {
        @Volatile
        private var INSTANCE: IonosondeDatabase? = null

        fun getInstance(context: Context): IonosondeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    IonosondeDatabase::class.java,
                    "ionosonde.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
