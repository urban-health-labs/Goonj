package ai.rever.goonj.download.database

import ai.rever.goonj.models.Track
import android.app.Application
import androidx.lifecycle.LiveData

class DownloadRepository(application: Application) {
    lateinit var downloadDAO: DownloadDAO
    lateinit var allDownloadedTracks : LiveData<List<Track>>

    init {
        val db =
            DownloadDatabase.getDatabase(application)
        db?.let {
            downloadDAO = db.downloadDao()
            allDownloadedTracks = downloadDAO.getAllDownloadedTracks()
        }
    }


    fun insertDownload(track: Track){
//        suspend {
            downloadDAO.insert(track)
//        }

    }

    fun updateDownload(mediaId: String, downloadedState: Int){
//        suspend {
            downloadDAO.updateDownloadedState(mediaId, downloadedState)
//        }
    }
}