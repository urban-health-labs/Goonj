package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples
import android.content.Context

interface GoonjPlayer {
    fun startNewSession(context: Context){
        PlaybackManager.getInstance(context).startNewSession()
    }

    fun play(context: Context) {
        PlaybackManager.getInstance(context).play()
    }

    fun pause(context: Context) {
        PlaybackManager.getInstance(context).pause()
    }

    fun stop(context: Context){
        PlaybackManager.getInstance(context).stop()
    }

    fun next(context: Context){}

    fun seek(context: Context, positionMs: Long?){
        PlaybackManager.getInstance(context).seekTo(positionMs!!)
    }

    fun addAudioToPlaylist(context: Context, audioTrack: Samples.Track){
        PlaybackManager.getInstance(context).addAudioToPlaylist(audioTrack)
    }

    fun customizeNotification(context: Context, useNavigationAction: Boolean = true,
                              usePlayPauseAction: Boolean = true,
                              fastForwardIncrementMs: Long = 0L,
                              rewindIncrementMs: Long = 0L){

        PlaybackManager.getInstance(context).customiseNotification(useNavigationAction,usePlayPauseAction,fastForwardIncrementMs,rewindIncrementMs)
    }

    fun isPlayingLiveData(context: Context) = PlaybackManager.getInstance(context).isPlayingLiveData

    fun currentPlayingTrack(context: Context) = PlaybackManager.getInstance(context).currentPlayingTrack

}