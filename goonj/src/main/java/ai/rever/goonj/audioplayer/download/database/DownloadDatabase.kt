package ai.rever.goonj.audioplayer.download.database

import ai.rever.goonj.audioplayer.models.Track
import android.app.Application
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Database(entities = [Track::class], version = 1)
abstract class DownloadDatabase : RoomDatabase(){
    abstract fun downloadDao(): DownloadDAO

    companion object{
        @Volatile
        private var INSTANCE: DownloadDatabase? = null

        fun getDatabase(context: Application): DownloadDatabase? {
            if (INSTANCE == null) {
                synchronized(DownloadDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            DownloadDatabase::class.java,
                            "download_database"
                        ).allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }

}