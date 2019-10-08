package ai.rever.goonj.interfaces

import ai.rever.goonj.Goonj
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.models.Track
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * This interface will be visible to User
 */
interface GoonjPlayer {

    fun startNewSession() = Goonj.startNewSession()

    fun resume() = Goonj.resume()

    fun pause() = Goonj.pause()

    fun finishTrack() = Goonj.finishTrack()

    fun seekTo(positionMS: Long) = Goonj.seekTo(positionMS)

    fun addTrack(track: Track, index: Int ?= null) = Goonj.addTrack(track, index)

    fun removeTrack(index : Int) = Goonj.removeTrack(index)

    fun moveTrack(currentIndex : Int, finalIndex : Int) = Goonj.moveTrack(currentIndex, finalIndex)

    fun skipToNext() = Goonj.skipToNext()

    fun skipToPrevious() = Goonj.skipToPrevious()

    fun customizeNotification(useNavigationAction: Boolean = true,
                              usePlayPauseAction: Boolean = true,
                              fastForwardIncrementMs: Long = 0L,
                              rewindIncrementMs: Long = 0L,
                              smallIcon : Int) =
        Goonj.customiseNotification(useNavigationAction,
            usePlayPauseAction,
            fastForwardIncrementMs,
            rewindIncrementMs,
            smallIcon)

    fun removeNotification() = Goonj.removeNotification()

    var autoplay: Boolean
        get() = Goonj.autoplay
        set(value) {
            Goonj.autoplay = value
        }

    val playerState: GoonjPlayerState? get() = Goonj.playerState

    val currentTrack: Track? get() = Goonj.currentTrack

    val trackList: List<Track> get() = Goonj.trackList

    val trackPosition: Long get() = Goonj.trackPosition

    val trackProgress: Double get() = Goonj.trackProgress

    val playerStateFlowable: Flowable<GoonjPlayerState> get() = Goonj.playerStateFlowable

    val currentTrackFlowable: Flowable<Track> get() = Goonj.currentTrackFlowable

    val autoplayFlowable: Flowable<Boolean> get() = Goonj.autoplayFlowable

    val trackCompletionObservable: Observable<Track> get() = Goonj.trackCompletionObservable
}