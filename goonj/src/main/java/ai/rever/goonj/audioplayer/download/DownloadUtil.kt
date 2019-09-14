package ai.rever.goonj.audioplayer.download

import ai.rever.goonj.R
import ai.rever.goonj.audioplayer.download.database.DownloadRepository
import ai.rever.goonj.audioplayer.models.Track
import android.app.Application
import android.content.Context
import android.util.Log
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
        lateinit var application: Application
        lateinit var downloadRepository: DownloadRepository

        @Synchronized
        fun getCache(context: Context): Cache {
            if (cache == null) {
                val cacheDirectory = File(context.getExternalFilesDir(null), "downloads")
                cache =
                    SimpleCache(cacheDirectory, NoOpCacheEvictor(), ExoDatabaseProvider(context))
            }
            return cache as Cache
        }

        @Synchronized
        fun getDownloadManager(context: Context): DownloadManager {
            if (downloadManager == null) {
                downloadManager = DownloadManager(
                    context,
                    ExoDatabaseProvider(context),
                    getCache(context),
                    DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(context, context.getString(R.string.app_name))
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
                    track.mediaId, DownloadRequest.TYPE_PROGRESSIVE,
                    url.toUri(), Collections.emptyList(), null, null
                ),
                false
            )

            
            downloadRepository.insertDownload(track)
        }

        fun getAllDownloads(context: Application): LiveData<List<Track>> {
            application = context
            downloadRepository = DownloadRepository(context)
            return downloadRepository.allDownloadedTracks
        }

        fun isMediaDownloaded(context: Application, mediaId: String): Boolean {
            application = context
            downloadRepository = DownloadRepository(application)
            var download = getDownloadManager(context).downloadIndex.getDownload(mediaId)
            return download?.state == Download.STATE_COMPLETED
        }

        fun getMediaDownloadPercentage(context: Application, mediaId: String): Float? {
            downloadRepository = DownloadRepository(application)
            var download = getDownloadManager(context).downloadIndex.getDownload(mediaId)
            return download?.percentDownloaded
        }

        fun getDownloadState(state: Int) : String{
            when(state){
                0 -> return "STATE_QUEUED"
                1 -> return "STATE_STOPPED"
                2 -> return "STATE_DOWNLOADING"
                3 -> return "STATE_COMPLETED"
                4 -> return "STATE_FAILED"
                5 -> return "STATE_REMOVING"
                7 -> return "STATE_RESTARTING"
                else -> {
                    return "STATE_UNKNOWN"
                }
            }
        }

    }

}
