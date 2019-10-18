package ai.rever.goonj.download

import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.R
import ai.rever.goonj.models.Track
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
    IDLE, DOWNLOAD_CHANGE, INITIALIZED, REQUIREMENT_STATE_CHANGED, DOWNLOAD_REMOVED
}

object GoonjDownloadManager {

    val maxCacheBytes: Long = 200 * 1024 * 1024

    internal val downloadStateBehaviorSubject = BehaviorSubject.createDefault<DownloadState>(DownloadState.INITIALIZED)

    internal val cache: Cache by lazy {
        SimpleCache(File(appContext?.getExternalFilesDir(DIRECTORY_MUSIC),
            "downloads"), GoonjLRUCacheEvictor(maxCacheBytes),
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

    fun addDownload(track: Track) {
        DownloadService.sendAddDownload(appContext,
            AudioDownloadService::class.java, DownloadRequest(
                track.url, DownloadRequest.TYPE_PROGRESSIVE,
                track.url.toUri(), Collections.emptyList(),
                null, track.toByteArray),
            false)
    }

    val allDownloads: DownloadCursor get() = downloadManager.downloadIndex.getDownloads(STATE_COMPLETED)

    fun isTrackDownloaded(url: String): Boolean {
        val download = downloadManager
            .downloadIndex.getDownload(url)
        return download?.state == STATE_COMPLETED
    }

    fun getMediaDownloadPercentage(url: String): Float? {
        val download = downloadManager
            .downloadIndex.getDownload(url)
        return download?.percentDownloaded
    }

    fun getDownloadState(state: Int) : String{
        return when(state){
            0 -> "STATE_QUEUED"
            1 -> "STATE_STOPPED"
            2 -> "STATE_DOWNLOADING"
            3 -> "STATE_COMPLETED"
            4 -> "STATE_FAILED"
            5 -> "STATE_REMOVING"
            7 -> "STATE_RESTARTING"
            else -> {
                "STATE_UNKNOWN"
            }
        }
    }
}
