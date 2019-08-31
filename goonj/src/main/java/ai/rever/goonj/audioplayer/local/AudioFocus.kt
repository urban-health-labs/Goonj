package ai.rever.goonj.audioplayer.local

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

fun LocalPlayer.requestAudioFocus() {
    audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        playbackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)
                .build()

            val res = audioManager?.requestAudioFocus(focusRequest)

            synchronized(mFocusLock){
                if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED){
                    mPlaybackDelayed = false
                    pause()
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                    mPlaybackDelayed = false

                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                    mPlaybackDelayed = true
                }
            }
        }

    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        val result = audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

        if(result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            pause()
        }
    }
}

fun LocalPlayer.removeAudioFocus() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        audioManager?.abandonAudioFocus(this)
    } else {
        audioManager?.abandonAudioFocusRequest(focusRequest)
    }
}
