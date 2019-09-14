package ai.rever.goonj.audioplayer.download

import ai.rever.goonj.R
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.scheduler.Requirements
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.util.*

object DownloadUtil {

    private var cache: Cache? = null
    private var downloadManager: DownloadManager? = null

    @Synchronized
    fun getCache(context: Context): Cache {
        if (cache == null) {
            val cacheDirectory = File(context.getExternalFilesDir(null), "downloads")
            cache = SimpleCache(cacheDirectory, NoOpCacheEvictor(), ExoDatabaseProvider(context))
        }
        return cache as Cache
    }

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager {
        if (downloadManager == null) {
            downloadManager = DownloadManager(context,
                ExoDatabaseProvider(context),
                getCache(context),
                DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name))))
        }
        return downloadManager as DownloadManager
    }

    fun addDownload(context: Context,id: String, url: String){
        DownloadService.sendAddDownload(context,
            AudioDownloadService::class.java, DownloadRequest(id, DownloadRequest.TYPE_PROGRESSIVE,
                url.toUri(), Collections.emptyList(),null,null),
            false
        )
    }
    val TAG = "DOWNLOAD UTIL"
    fun getAllDownloads(context: Context) {


        var downloadCursor = getDownloadManager(context).downloadIndex.getDownloads()
        Log.d(TAG,"Count: ${downloadCursor?.count} ")

        if(downloadCursor.count == 0){
            return
        }

        try {

            downloadCursor.moveToFirst()
            var download = downloadCursor?.download
            Log.d(TAG,"Percent: ${download?.percentDownloaded} State: ${download?.state} Length:${download?.contentLength}")

            while (downloadCursor.moveToNext()){
                var download = downloadCursor?.download
                Log.d(TAG,"Percent: ${download?.percentDownloaded} State: ${download?.state} Length:${download?.contentLength}")
            }

        } finally {
            downloadCursor.close()
        }
    }

    fun isMediaDownloaded(context: Context, mediaId: String): Boolean {
        var download = getDownloadManager(context).downloadIndex.getDownload(mediaId)
        Log.d(TAG,"zD: ${download?.percentDownloaded}")

        return download?.state == Download.STATE_COMPLETED
    }

    fun getMediaDownloadPercentage(context: Context, mediaId: String) : Float? {
        var download = getDownloadManager(context).downloadIndex.getDownload(mediaId)
        return download?.percentDownloaded
    }

}
