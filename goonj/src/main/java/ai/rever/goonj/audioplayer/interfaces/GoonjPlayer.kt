package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples
import ai.rever.goonj.audioplayer.service.AudioPlayerService
import ai.rever.goonj.audioplayer.util.*
import android.content.Context
import android.content.Intent

interface GoonjPlayer {

    fun startNewSession(context: Context){
        performPlayerAction(context, ACTION_START_NEW_SESSION)
    }

    fun play(context: Context) {
        performPlayerAction(context, ACTION_RESUME_SESSION)
    }

    fun pause(context: Context) {
        performPlayerAction(context, ACTION_PAUSE_SESSION)
    }

    fun stop(context: Context){
        performPlayerAction(context, ACTION_STOP)
    }

    fun next(context: Context){
        performPlayerAction(context, ACTION_NEXT)
    }

    fun addAudioToPlaylist(context: Context, audioSample: Samples.Sample){
        val intent = Intent(context, AudioPlayerService::class.java)
        intent.action = ACTION_ADD_AUDIO_TO_PLAYLIST
        intent.putExtra(audioURLKey,audioSample)
        context.startService(intent)
    }

    private fun performPlayerAction(context: Context, action: String){
        val intent = Intent(context, AudioPlayerService::class.java)
        intent.action = action
        context.startService(intent)
    }

    //@JvmOverloads
    fun customizeNotification(context: Context, useNavigationAction: Boolean = true,
                              usePlayPauseAction: Boolean = true,
                              fastForwardIncrementMs: Long = 0L,
                              rewindIncrementMs: Long = 0L){
        val intent = Intent(context, AudioPlayerService::class.java)
        intent.action = ACTION_CUSTOMIZE_NOTIFICATION
        intent.putExtra(USE_NAV_ACTION,useNavigationAction)
        intent.putExtra(USE_PLAY_PAUSE,usePlayPauseAction)
        intent.putExtra(FAST_FORWARD_INC, fastForwardIncrementMs)
        intent.putExtra(REWIND_INC, rewindIncrementMs)

        context.startService(intent)
    }

}