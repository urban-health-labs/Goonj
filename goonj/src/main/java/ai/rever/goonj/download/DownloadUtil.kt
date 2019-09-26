package ai.rever.goonj.download

import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.R
import ai.rever.goonj.download.database.DownloadRepository
import ai.rever.goonj.models.Track
import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.util.*

class DownloadUtil {

    companion object {

        private var cache: Cache? = null
        private var downloadManager: DownloadManager? = null
        lateinit var downloadRepository: DownloadRepository

        @Synchronized
        fun getCache(): Cache {
            if (cache == null) {
                val cacheDirectory = File(appContext?.getExternalFilesDir(null), "downloads")
                cache = SimpleCache(cacheDirectory, NoOpCacheEvictor(), ExoDatabaseProvider(appContext))
            }
            return cache as Cache
        }

        @Synchronized
        fun getDownloadManager(): DownloadManager {
            if (downloadManager == null) {
                downloadManager = DownloadManager(
                    appContext,
                    ExoDatabaseProvider(appContext),
                    getCache(),
                    DefaultDataSourceFactory(
                        appContext,
                        Util.getUserAgent(appContext, appContext?.getString(R.string.app_name))
                    )
                )
            }
            return downloadManager as DownloadManager
        }

        fun addDownload(context: Application, track: Track) {
            val url = track.url
            DownloadService.sendAddDownload(
                context,
                AudioDownloadService::class.java, DownloadRequest(
                    track.id, DownloadRequest.TYPE_PROGRESSIVE,
                    url.toUri(), Collections.emptyList(), null, null
                ),
                false
            )
            downloadRepository.insertDownload(track)
        }

        fun getAllDownloads(): LiveData<List<Track>> {
            downloadRepository = DownloadRepository()
            return downloadRepository.allDownloadedTracks
        }

        fun isMediaDownloaded(mediaId: String): Boolean {
            downloadRepository = DownloadRepository()
            val download = getDownloadManager()
                .downloadIndex.getDownload(mediaId)
            return download?.state == Download.STATE_COMPLETED
        }

        fun getMediaDownloadPercentage(mediaId: String): Float? {
            downloadRepository = DownloadRepository()
            val download = getDownloadManager()
                .downloadIndex.getDownload(mediaId)
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

}
