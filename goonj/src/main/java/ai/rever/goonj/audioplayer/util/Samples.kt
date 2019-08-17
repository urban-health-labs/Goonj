package ai.rever.goonj.audioplayer.util

import ai.rever.goonj.R
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import android.graphics.Bitmap
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.os.Bundle
import androidx.mediarouter.media.MediaItemStatus
import java.io.Serializable


object Samples {

    val SAMPLES = arrayOf(
         Sample(
            "https://storage.googleapis.com/automotive-media/Talkies.mp3",
            "audio_3",
            "Talkies",
            "If it talks like a duck and walks like a duck.",
             R.mipmap.ic_album_art
        ), Sample(
            "https://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3",
            "audio_1",
            "Jazz in Paris",
            "Jazz for the masses",
            R.mipmap.ic_album_art
        ), Sample(
            "https://storage.googleapis.com/automotive-media/The_Messenger.mp3",
            "audio_2",
            "The messenger",
            "Hipster guide to London",
            R.mipmap.ic_album_art
        )
    )

    class Sample(
        val url: String, val mediaId: String, val title: String, val description: String, val bitmapResource: Int
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

    fun getMediaDescription(context: Context, sample: Sample): MediaDescriptionCompat {
        val extras = Bundle()
        val bitmap = getBitmap(context, sample.bitmapResource)
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
        return MediaDescriptionCompat.Builder()
            .setMediaId(sample.mediaId)
            .setIconBitmap(bitmap)
            .setTitle(sample.title)
            .setDescription(sample.description)
            .setExtras(extras)
            .build()
    }

    fun getBitmap(context: Context, @DrawableRes bitmapResource: Int): Bitmap {
        return (context.resources.getDrawable(bitmapResource) as BitmapDrawable).bitmap
    }

}