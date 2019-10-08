package ai.rever.goonj.interfaces

import ai.rever.goonj.Goonj
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.models.Track

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


    var autoplay: Boolean
        get() = Goonj.autoplay
        set(value) {
            Goonj.autoplay = value
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

    val playerState: GoonjPlayerState? get() = Goonj.playerState

    val currentTrack: Track? get() = Goonj.currentTrack

    val playerStateObservable get() = Goonj.playerStateObservable

    val currentTrackObservable get() = Goonj.currentTrackObservable

    val trackList get() = Goonj.trackList

    val trackPosition get() = Goonj.trackPosition

}