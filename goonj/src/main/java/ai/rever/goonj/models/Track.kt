package ai.rever.goonj.models

import ai.rever.goonj.Goonj
import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.Goonj.downloadStateFlowable
import ai.rever.goonj.R
import ai.rever.goonj.download.DownloadState
import ai.rever.goonj.download.GoonjDownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.os.Bundle
import android.os.Parcelable
import androidx.core.net.toUri
import com.google.android.exoplayer2.offline.Download
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import kotlinx.android.parcel.Parcelize
import java.util.*
import android.os.Parcel
import io.reactivex.Flowable
import kotlinx.android.parcel.IgnoredOnParcel
import java.io.IOException

//private val ByteArray.track: Track? get() = run {
//    val parcel = Parcel.obtain()
//    parcel.unmarshall(this, 0, size)
//    parcel.setDataPosition(0) // This is extremely important!
//
//    val result = Track.CREATOR.createFromParcel(parcel)
//    parcel.recycle()
//    result
//}

//val Download.track get() = request.data.track

internal val Track.Companion.CREATOR get() = TrackCreator.getCreator()

@Parcelize
class Track (var id: String = "",
             var url: String = "",
             var title: String = "",
             var artistName: String = "",
             var imageUrl: String? = null,
             val extras: Bundle = Bundle(),
             var bitmap: Bitmap? = appContext?.defaultBitmap,
             val state: TrackState = TrackState()
): Parcelable {

    companion object{}

    @IgnoredOnParcel
    private val mediaInfo: MediaInfo? get() {
        if (url.isEmpty()) return null
        val musicMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)

        musicMetadata.putString(MediaMetadata.KEY_TITLE, title)
        musicMetadata.putString(MediaMetadata.KEY_ARTIST, artistName)

        imageUrl?.let {
            musicMetadata.addImage(WebImage(it.toUri()))
        }

        return MediaInfo.Builder(url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("audio/*")
            .setMetadata(musicMetadata)
            .build()
    }

    @IgnoredOnParcel
    val mediaLoadRequestData: MediaLoadRequestData? get() {
        return MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo?: return null)
            .build()
    }

    @IgnoredOnParcel
    val mediaDescription: MediaDescriptionCompat get() {
        val extras = Bundle()


        val mediaDescriptionBuilder =  MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setDescription(artistName)
            .setExtras(extras)

        if (bitmap != null) {
            mediaDescriptionBuilder.setIconBitmap(bitmap)

            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, appContext?.defaultBitmap)

        } else {
            val bitmap = appContext?.defaultBitmap

            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)

            mediaDescriptionBuilder.setIconBitmap(bitmap)
        }

        return mediaDescriptionBuilder.build()
    }

    fun load(callback: (Bitmap?) -> Unit) {
        if (bitmap != null) {
            callback(bitmap)
        }

        val imageLoader = Goonj.imageLoader
        if (imageLoader == null) {
            bitmap = appContext?.defaultBitmap
            callback(bitmap)
            return
        }

        imageLoader(this) {
            bitmap = it
            callback(it)
        }
    }

//    @IgnoredOnParcel
//    val toByteArray: ByteArray get() = run {
//        val parcel = Parcel.obtain()
//        writeToParcel(parcel, 0)
//        val bytes = parcel.marshall()
//        parcel.recycle()
//        return bytes
//    }

    @IgnoredOnParcel
    val download: Download? get() = try {
        GoonjDownloadManager.downloadManager.downloadIndex.getDownload(id)
    } catch (e: IOException) {
        null
    }

    @IgnoredOnParcel
    val isDownloadActiveFlowable: Flowable<Boolean?> get() = downloadStateFlowable.map { download != null }

    fun requestDownload() = GoonjDownloadManager.addDownload(id, url.toUri())

    fun removeDownload() = GoonjDownloadManager.removeDownload(id)
}


@Parcelize
data class TrackState(var index: Int = 0,
                      var position: Long = 0,
                      var duration: Long = 1, // divide safe
                      var addedAt: Date? = Date(),
                      var playedAt: Date? = Date(),
                      var completedAt: Date? = Date(),
                      var remoteItemId: String? = null): Parcelable {
    val progress: Double get() = position.toDouble() / duration.toDouble()
}


internal val Context.defaultBitmap: Bitmap? get() = BitmapFactory.decodeResource(resources, R.drawable.ic_album)


