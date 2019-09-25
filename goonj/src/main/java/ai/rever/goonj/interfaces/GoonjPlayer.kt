package ai.rever.goonj.interfaces

import ai.rever.goonj.GoonjPlayer
import ai.rever.goonj.models.Track

/**
 * This interface will be visible to User
 */
interface GoonjPlayer {

    fun startNewSession() {
        GoonjPlayer.startNewSession()
    }

    fun play() {
        GoonjPlayer.play()
    }

    fun pause() {
        GoonjPlayer.pause()
    }

    fun seek(positionMS: Long){
        GoonjPlayer.seekTo(positionMS)
    }

    fun setAutoplay(autoplay : Boolean, indexFromLast: Int,
                            autoLoadListener: AutoLoadListener
    ) {
        GoonjPlayer.setAutoplay(autoplay, indexFromLast, autoLoadListener)
    }

    fun addTrack(track: Track, index: Int ?= null) {
        GoonjPlayer.addTrack(track, index)
    }

    fun customizeNotification(useNavigationAction: Boolean = true,
                              usePlayPauseAction: Boolean = true,
                              fastForwardIncrementMs: Long = 0L,
                              rewindIncrementMs: Long = 0L,
                              smallIcon : Int){

        GoonjPlayer.customiseNotification(useNavigationAction,
            usePlayPauseAction,
            fastForwardIncrementMs,
            rewindIncrementMs,
            smallIcon)
    }

    val currentTrack get() = GoonjPlayer.currentTrack

    fun removeTrack(index : Int){
        GoonjPlayer.removeTrack(index)
    }

    fun moveTrack(currentIndex : Int, finalIndex : Int){
        GoonjPlayer.moveTrack(currentIndex, finalIndex)
    }

    fun skipToNext() = GoonjPlayer.skipToNext()

    fun skipToPrevious() = GoonjPlayer.skipToPrevious()

    fun removeNotification() = GoonjPlayer.removeNotification()

    val isPlayingLiveData get() = GoonjPlayer.isPlayingLiveData

    val currentPlayingTrack get() = GoonjPlayer.currentPlayingTrack
}