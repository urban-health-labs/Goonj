package ai.rever.goonj.download

import ai.rever.goonj.Goonj
import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.R
import ai.rever.goonj.models.Track
import android.net.Uri
import android.os.Environment.DIRECTORY_MUSIC
import androidx.core.net.toUri
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.offline.Download.*
import com.google.android.exoplayer2.scheduler.Requirements
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import java.util.*

enum class DownloadState{
    IDLE, DOWNLOAD_CHANGE, DOWNLOADING, INITIALIZED, REQUIREMENT_STATE_CHANGED, DOWNLOAD_REMOVED, DOWNLOADED
}

object GoonjDownloadManager {

    internal val downloadStateBehaviorSubject = BehaviorSubject.createDefault<DownloadState>(DownloadState.INITIALIZED)

    internal val cache: Cache by lazy {
        SimpleCache(File(appContext?.getExternalFilesDir(DIRECTORY_MUSIC),
            "downloads"), GoonjLRUCacheEvictor(Goonj.maxCacheBytes),
            ExoDatabaseProvider(appContext))
    }

    internal val downloadManager: DownloadManager by lazy {
        DownloadManager(appContext, ExoDatabaseProvider(appContext),
            cache, DefaultDataSourceFactory(appContext,
                Util.getUserAgent(appContext, appContext?.getString(R.string.app_name)))
        )
    }

    private val listener = object: DownloadManager.Listener {
        override fun onIdle(downloadManager: DownloadManager?) {
            downloadStateBehaviorSubject.onNext(DownloadState.IDLE)
        }

        override fun onDownloadChanged(downloadManager: DownloadManager?, download: Download?) {
            downloadStateBehaviorSubject.onNext(DownloadState.DOWNLOAD_CHANGE)
        }

        override fun onInitialized(downloadManager: DownloadManager?) {
            downloadStateBehaviorSubject.onNext(DownloadState.INITIALIZED)
        }

        override fun onRequirementsStateChanged(
            downloadManager: DownloadManager?,
            requirements: Requirements?,
            notMetRequirements: Int
        ) {
            downloadStateBehaviorSubject.onNext(DownloadState.REQUIREMENT_STATE_CHANGED)
        }

        override fun onDownloadRemoved(downloadManager: DownloadManager?, download: Download?) {
            downloadStateBehaviorSubject.onNext(DownloadState.DOWNLOAD_REMOVED)
        }
    }

    val subscribe get() = object: Disposable {

        init {
            downloadManager.addListener(listener)
        }

        var isDispose = false

        override fun isDisposed(): Boolean {
            return isDispose
        }

        override fun dispose() {
            isDispose = true
            downloadManager.removeListener(listener)
        }
    }

    fun addDownload(trackId: String, uri: Uri) {
        DownloadService.sendAddDownload(appContext,
            AudioDownloadService::class.java, DownloadRequest(
                trackId, DownloadRequest.TYPE_PROGRESSIVE,
                uri, Collections.emptyList(),
                trackId, null), false)                 //, track.toByteArray))
    }

    fun removeDownload(trackId: String) {
        DownloadService.sendRemoveDownload(appContext,
                AudioDownloadService::class.java,
                trackId, false)
    }

    fun isDownloaded(trackId: String) = downloadManager.downloadIndex
            .getDownload(trackId)?.state == STATE_COMPLETED

    fun getAllDownloads(): List<String> {
        val downloadCursor = downloadManager.downloadIndex.getDownloads(STATE_COMPLETED)
        val downloadedTrackList = mutableListOf<String>()
        if(downloadCursor.count == 0) {
            return downloadedTrackList
        }
        downloadCursor.moveToFirst()
        do {
            downloadedTrackList.add(downloadCursor.download.request.id)
        } while (downloadCursor.moveToNext())

        return downloadedTrackList
    }
}
