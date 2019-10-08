package ai.rever.goonj.interfaces

import ai.rever.goonj.Goonj
import ai.rever.goonj.models.Track
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * This interface will be visible to User
 */
interface GoonjPlayer {

    fun startNewSession() {
        Goonj.startNewSession()
    }

    fun play() {
        Goonj.resume()
    }

    fun pause() {
        Goonj.pause()
    }

    fun seekTo(positionMS: Long){
        Goonj.seekTo(positionMS)
    }

    fun setAutoplay(autoplay : Boolean) {
        Goonj.setAutoplay(autoplay)
    }

    fun addTrack(track: Track, index: Int ?= null) {
        Goonj.addTrack(track, index)
    }

    fun customizeNotification(useNavigationAction: Boolean = true,
                              usePlayPauseAction: Boolean = true,
                              fastForwardIncrementMs: Long = 0L,
                              rewindIncrementMs: Long = 0L,
                              smallIcon : Int){

        Goonj.customiseNotification(useNavigationAction,
            usePlayPauseAction,
            fastForwardIncrementMs,
            rewindIncrementMs,
            smallIcon)
    }


    val trackList get() = Goonj.trackList

    fun removeTrack(index : Int){
        Goonj.removeTrack(index)
    }

    fun moveTrack(currentIndex : Int, finalIndex : Int){
        Goonj.moveTrack(currentIndex, finalIndex)
    }

    fun skipToNext() = Goonj.skipToNext()

    fun skipToPrevious() = Goonj.skipToPrevious()

    fun removeNotification() = Goonj.removeNotification()

    fun finishTrack() = Goonj.finishTrack()

    val trackPosition get() = Goonj.trackPosition

    val playerStateObservable get() = Goonj.playerStateObservable

    val currentPlayingTrackObservable get() = Goonj.currentPlayingTrack
}