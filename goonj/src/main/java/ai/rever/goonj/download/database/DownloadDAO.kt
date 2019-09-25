package ai.rever.goonj.download.database

import ai.rever.goonj.models.Track
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DownloadDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(track: Track)

    @Query("SELECT * from download_table")
    fun getAllDownloadedTracks() : LiveData<List<Track>>

    @Query("UPDATE download_table SET downloadedState = :downloadedState WHERE id = :id ")
    fun updateDownloadedState(id: String, downloadedState: Int)

}