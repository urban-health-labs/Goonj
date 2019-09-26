package ai.rever.goonj.download.database

import ai.rever.goonj.models.Track
import androidx.lifecycle.LiveData

class DownloadRepository {
    lateinit var downloadDAO: DownloadDAO
    lateinit var allDownloadedTracks : LiveData<List<Track>>

    init {
        DownloadDatabase.getDatabase()?.apply{
            downloadDAO = downloadDao()
            allDownloadedTracks = downloadDAO.getAllDownloadedTracks()
        }
    }

    fun insertDownload(track: Track){
            downloadDAO.insert(track)
    }

    fun updateDownload(mediaId: String, downloadedState: Int){
            downloadDAO.updateDownloadedState(mediaId, downloadedState)
    }
}