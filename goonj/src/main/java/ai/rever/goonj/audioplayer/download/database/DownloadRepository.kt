package ai.rever.goonj.audioplayer.download.database

import ai.rever.goonj.audioplayer.models.Track
import android.app.Application
import androidx.lifecycle.LiveData

class DownloadRepository(application: Application) {
    lateinit var downloadDAO: DownloadDAO
    lateinit var allDownloadedTracks : LiveData<List<Track>>

    init {
        var db = DownloadDatabase.getDatabase(application)
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