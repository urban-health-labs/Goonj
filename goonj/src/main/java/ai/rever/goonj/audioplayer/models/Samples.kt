package ai.rever.goonj.audioplayer.models

import ai.rever.goonj.R
import android.content.Context
import androidx.annotation.DrawableRes
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.mediarouter.media.MediaItemStatus
import java.io.Serializable



    val SAMPLES = arrayOf(
        Track(
            "https://storage.googleapis.com/automotive-media/Talkies.mp3",
            //"https://raw.githubusercontent.com/rever-ai/SampleMusic/master/David_H_Porter_-_Mozarts_Rondo_No_3_in_A_Minor_K_511.mp3",
            "audio_1",
            "Talkies",
            //"If it talks like a duck and walks like a duck.",
            "One",
            "https://img.discogs.com/Bss063QHQ7k0sRwejSQWTJ-iKGI=/fit-in/600x573/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-900642-1182343816.jpeg.jpg"
        ), Track(
            "https://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3",
            //"https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Menstruation_Sisters_-_14_-_top_gun.mp3",
            "audio_2",
            "Jazz in Paris",
            //"Jazz for the masses",
            "Two",
            "https://i.ebayimg.com/images/g/MMgAAOSwXi9b6BJ3/s-l640.jpg"
        ), Track(
            "https://storage.googleapis.com/automotive-media/The_Messenger.mp3",
            //"https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Manueljgrotesque_-_24_-_grotesque25.mp3",
            "audio_3",
            "The messenger",
            //"Hipster guide to London",
            "Three",
            "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
        ), Track(
            "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Hard_TON_-_07_-_Choc-ice_Dance.mp3",
            "audio_4",
            "Audio 4",
            //"Hipster guide to London",
            "Four",
            "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
        ), Track(
            "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Big_Blood_-_01_-_Bah-num.mp3",
            "audio_5",
            "Audio 5",
            //"Hipster guide to London",
            "Five",
            "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
        ),  Track(
            "https://raw.githubusercontent.com/rever-ai/SampleMusic/master/Black_Ant_-_08_-_realest_year_9.mp3",
            "audio_6",
            "Audio 6",
            //"Hipster guide to London",
            "Six",
            "https://www.mobygames.com/images/covers/l/507031-the-messenger-nintendo-switch-front-cover.jpg"
        )
    )

    class Track(
        val url: String, val mediaId: String, val title: String, val artist: String,
        val albumArtUrl : String? = ""
    ) : Serializable {
        var state = MediaItemStatus.PLAYBACK_STATE_PENDING
        var index : Int = 0
        var position: Long = 0
        var duration: Long = 0
        var timestamp: Long = 0
        var remoteItemId: String? = null
        var bitmapResource: Int = R.mipmap.ic_album_art
        var bitmap: Bitmap? = null

        override fun toString(): String {
            return "$title Description: $artist DURATION: $duration INDEX: $index state: $state"
        }
    }

    fun getMediaDescription(context: Context?, track: Track): MediaDescriptionCompat {
        val extras = Bundle()
        val mediaDescriptionBuilder =  MediaDescriptionCompat.Builder()
            .setMediaId(track.mediaId)
            .setTitle(track.title)
            .setDescription(track.artist)
            .setExtras(extras)

        context?.let {
            if(track.bitmap != null){
                mediaDescriptionBuilder.setIconBitmap(track.bitmap)
            } else {
                val bitmap = getBitmap(context, track.bitmapResource)
                extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)

                mediaDescriptionBuilder.setIconBitmap(bitmap)
            }
        }
        return mediaDescriptionBuilder.build()
    }

    fun getBitmap(context: Context?, @DrawableRes bitmapResource: Int): Bitmap? {
        return context?.let { ContextCompat.getDrawable(it, bitmapResource)?.toBitmap() }
    }

