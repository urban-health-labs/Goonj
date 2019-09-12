package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples
import android.content.Context

/**
 * This interface will be visible to User
 */
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

    fun seek(context: Context, positionMs: Long?){
        PlaybackManager.getInstance(context).seekTo(positionMs!!)
    }

    fun setAutoplay(context: Context, autoplay : Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener) {
        PlaybackManager.getInstance(context).setAutoplay(autoplay, indexFromLast, autoLoadListener)
    }

    fun addAudioToPlaylist(context: Context, audioTrack: Samples.Track, index: Int ?= -1){
        PlaybackManager.getInstance(context).addAudioToPlaylist(audioTrack,index)
    }

    fun customizeNotification(context: Context, useNavigationAction: Boolean = true,
                              usePlayPauseAction: Boolean = true,
                              fastForwardIncrementMs: Long = 0L,
                              rewindIncrementMs: Long = 0L,
                              smallIcon : Int){

        PlaybackManager.getInstance(context).customiseNotification(useNavigationAction,usePlayPauseAction
            ,fastForwardIncrementMs,rewindIncrementMs,smallIcon)
    }

    fun session(context: Context) : List<Samples.Track>{
        return PlaybackManager.getInstance(context).currentSession
    }

    fun removeTrack(context: Context,index : Int){
        PlaybackManager.getInstance(context).removeTrack(index)
    }

    fun moveTrack(context: Context, currentIndex : Int, finalIndex : Int){
        PlaybackManager.getInstance(context).moveTrack(currentIndex, finalIndex)
    }

    fun skipToNext(context: Context){
        PlaybackManager.getInstance(context).skipToNext()
    }

    fun skipToPrevious(context: Context){
        PlaybackManager.getInstance(context).skipToPrevious()
    }

    fun removeNotification(context: Context){
        PlaybackManager.getInstance(context).removeNotification()
    }

    fun isPlayingLiveData(context: Context) = PlaybackManager.getInstance(context).isPlayingLiveData

    fun currentPlayingTrack(context: Context) = PlaybackManager.getInstance(context).currentPlayingTrack

}