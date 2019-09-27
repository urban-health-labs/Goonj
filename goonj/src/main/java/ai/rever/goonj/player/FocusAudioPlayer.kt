package ai.rever.goonj.player

import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.interfaces.AudioPlayer
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer

/**
 *     Call when ready for play: requestAudioFocus(focusLock)
 *     Call when paused: removeAudioFocus()
**/


abstract class FocusAudioPlayer: AudioPlayer , AudioManager.OnAudioFocusChangeListener {

    val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(appContext)
    }

    val focusLock = Any()

    private val audioManager : AudioManager? by lazy {
        appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    private var playbackAttributes: AudioAttributes? = null
    private var focusRequest: AudioFocusRequest? = null

    private var playbackDelayed = false
    private var resumeOnFocusGain = false

    fun requestAudioFocus(focusLock: Any) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes ?: return)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()


                val res = audioManager?.requestAudioFocus(focusRequest)
                this.focusRequest = focusRequest

                synchronized(focusLock) {
                    when (res) {
                        AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                            playbackDelayed = false
                            pause()
                        }
                        AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> playbackDelayed = false
                        AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> playbackDelayed = true
                    }
                }
            }

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val result = audioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                pause()
            }
        }
    }

    fun removeAudioFocus() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            audioManager?.abandonAudioFocus(this)
        } else {
            audioManager?.abandonAudioFocusRequest(focusRequest ?: return)
        }
    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if(playbackDelayed || resumeOnFocusGain){
                    synchronized(focusLock){
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    player.volume = 1f
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    playbackDelayed = false
                }
                pause()
                removeAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                player.volume = 0.1f
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(focusLock){
                    player.let {
                        resumeOnFocusGain = it.playWhenReady
                    }

                    playbackDelayed = false
                }
                player.volume = 0.1f
            }
        }
    }
}
