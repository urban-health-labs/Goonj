package ai.rever.goonj.download.database

import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.models.Track
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Database(entities = [Track::class], version = 1)
abstract class DownloadDatabase : RoomDatabase(){
    abstract fun downloadDao(): DownloadDAO

    companion object{
        @Volatile
        private var INSTANCE: DownloadDatabase? = null

        fun getDatabase(): DownloadDatabase? {
            if (INSTANCE == null) {
                synchronized(DownloadDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(appContext?: return null,
                            DownloadDatabase::class.java,
                            "download_database"
                        ).allowMainThreadQueries().build()
                    }
                }
            }
            return INSTANCE
        }
    }

}

