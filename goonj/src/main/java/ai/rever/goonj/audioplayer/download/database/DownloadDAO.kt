package ai.rever.goonj.audioplayer.download.database

import ai.rever.goonj.audioplayer.models.Track
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

    @Query("UPDATE download_table SET downloadedState = :downloadedState WHERE mediaId = :mediaId ")
    fun updateDownloadedState(mediaId: String,downloadedState: Int)

}