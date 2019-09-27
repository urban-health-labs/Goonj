package ai.rever.goonj.models

import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.R
import android.content.Context
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.mediarouter.media.MediaItemStatus
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.exoplayer2.offline.Download
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import kotlinx.android.parcel.Parcelize


val SAMPLES = arrayOf(
    Track(
        "https://storage.googleapis.com/automotive-media/Talkies.mp3",
        //"https://raw.githubusercontent.com/rever-ai/SampleMusic/master/David_H_Porter_-_Mozarts_Rondo_No_3_in_A_Minor_K_511.mp3",
        "audio_1",
        "Talkies",
        //"If it talks like a duck and walks like a duck.",
        "One",
        imageUrl = "https://img.discogs.com/Bss063QHQ7k0sRwejSQWTJ-iKGI=/fit-in/600x573/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-900642-1182343816.jpeg.jpg"
    ), Track(
        "https://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3",
        //"https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Menstruation_Sisters_-_14_-_top_gun.mp3",
        "audio_2",
        "Jazz in Paris",
        //"Jazz for the masses",
        "Two",
        imageUrl = "https://i.ebayimg.com/images/g/MMgAAOSwXi9b6BJ3/s-l640.jpg"
    ), Track(
        "https://storage.googleapis.com/automotive-media/The_Messenger.mp3",
        //"https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Manueljgrotesque_-_24_-_grotesque25.mp3",
        "audio_3",
        "The messenger",
        //"Hipster guide to London",
        "Three",
        imageUrl = "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
    ), Track(
        "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Hard_TON_-_07_-_Choc-ice_Dance.mp3",
        "audio_4",
        "Audio 4",
        //"Hipster guide to London",
        "Four",
        imageUrl = "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
    ), Track(
        "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Big_Blood_-_01_-_Bah-num.mp3",
        "audio_5",
        "Audio 5",
        //"Hipster guide to London",
        "Five",
        imageUrl = "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
    ), Track(
        "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Black_Ant_-_08_-_realest_year_9.mp3",
        "audio_6",
        "Audio 6",
        //"Hipster guide to London",
        "Six",
        imageUrl = "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
    )
)

@Entity(tableName = "download_table")
@Parcelize
data class Track (var url: String = "",
                  @PrimaryKey
                  var id: String = "",
                  var title: String = "",
                  var artistName: String = "",
                  var imageUrl: String? = null,
                  var downloadedState: Int = Download.STATE_QUEUED,

                  @Ignore
                  var extras: Bundle? = null,
                  @Ignore
                  val trackState: TrackState = TrackState()
): Parcelable {

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

    val mediaLoadRequestData: MediaLoadRequestData? get() {
        return MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo?: return null)
            .build()
    }

    val mediaDescription: MediaDescriptionCompat get() {
        val extras = Bundle()
        val mediaDescriptionBuilder =  MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setDescription(artistName)
            .setExtras(extras)

        val bitmap = appContext?.defaultBitmap
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)

        mediaDescriptionBuilder.setIconBitmap(bitmap)

        return mediaDescriptionBuilder.build()
    }
}


@Parcelize
data class TrackState(var state: GoonjPlayerState = GoonjPlayerState.IDLE,
                      var index: Int = 0,
                      var position: Long = 0,
                      var duration: Long = 0,
                      var remoteItemId: String? = null): Parcelable


val Context.defaultBitmap get() = ContextCompat.getDrawable(this, R.mipmap.ic_album_art)
    ?.toBitmap()


