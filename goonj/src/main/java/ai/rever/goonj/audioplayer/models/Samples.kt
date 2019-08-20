package ai.rever.goonj.audioplayer.models

import ai.rever.goonj.R
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import android.graphics.Bitmap
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.mediarouter.media.MediaItemStatus
import java.io.Serializable


object Samples {

    val SAMPLES = arrayOf(
        Sample(
            //"https://storage.googleapis.com/automotive-media/Talkies.mp3",
            "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/David_H_Porter_-_Mozarts_Rondo_No_3_in_A_Minor_K_511.mp3",
            "audio_1",
            "Talkies",
            //"If it talks like a duck and walks like a duck.",
            "One",
            R.mipmap.ic_album_art,
            "https://img.discogs.com/Bss063QHQ7k0sRwejSQWTJ-iKGI=/fit-in/600x573/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-900642-1182343816.jpeg.jpg"
        ), Sample(
            //"https://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3",
            "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Menstruation_Sisters_-_14_-_top_gun.mp3",
            "audio_2",
            "Jazz in Paris",
            //"Jazz for the masses",
            "Two",
            R.mipmap.ic_album_art,
            "https://i.ebayimg.com/images/g/MMgAAOSwXi9b6BJ3/s-l640.jpg"
        ), Sample(
            //"https://storage.googleapis.com/automotive-media/The_Messenger.mp3",
            "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Manueljgrotesque_-_24_-_grotesque25.mp3",
            "audio_3",
            "The messenger",
            //"Hipster guide to London",
            "Three",
            R.mipmap.ic_album_art,
            "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
        ), Sample(
            //"https://storage.googleapis.com/automotive-media/The_Messenger.mp3",
            "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Hard_TON_-_07_-_Choc-ice_Dance.mp3",
            "audio_4",
            "The messenger",
            //"Hipster guide to London",
            "Four",
            R.mipmap.ic_album_art,
            "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
        )
    )

    class Sample(
        val url: String, val mediaId: String, val title: String, val description: String, val bitmapResource: Int,
        val albumArtUrl : String? = ""
    ) : Serializable {
        var state = MediaItemStatus.PLAYBACK_STATE_PENDING
        var position: Long = 0
        var duration: Long = 0
        var timestamp: Long = 0
        var remoteItemId: String? = null

        override fun toString(): String {
            return "$title Description: $description URL: $url"
        }
    }

    fun getMediaDescription(context: Context?, sample: Sample): MediaDescriptionCompat {
        val extras = Bundle()
        val mediaDescriptionBuilder =  MediaDescriptionCompat.Builder()
            .setMediaId(sample.mediaId)
            .setTitle(sample.title)
            .setDescription(sample.description)
            .setExtras(extras)

        context?.let {
            val bitmap = getBitmap(context, sample.bitmapResource)
            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)

            mediaDescriptionBuilder.setIconBitmap(bitmap)
        }
        return mediaDescriptionBuilder.build()
    }

    fun getBitmap(context: Context?, @DrawableRes bitmapResource: Int): Bitmap? {
        return context?.let { ContextCompat.getDrawable(it, bitmapResource)?.toBitmap() }
    }

}