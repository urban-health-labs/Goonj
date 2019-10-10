package ai.rever.goonj.interfaces

import ai.rever.goonj.Goonj
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.models.Track
import android.content.Intent
import android.graphics.Bitmap
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * This interface will be visible to User
 */
interface GoonjPlayer {

    fun startNewSession() = Goonj::startNewSession

    fun resume() = Goonj::resume

    fun pause() = Goonj::pause

    fun finishTrack() = Goonj::finishTrack

    fun seekTo(positionMS: Long) = Goonj::seekTo

    fun addTrack(track: Track, index: Int ?= null) = Goonj::addTrack

    fun removeTrack(index : Int) = Goonj::removeTrack

    fun moveTrack(currentIndex : Int, finalIndex : Int) = Goonj::moveTrack

    fun skipToNext() = Goonj::skipToNext

    fun skipToPrevious() = Goonj::skipToPrevious

    fun customizeNotification(useNavigationAction: Boolean = true,
                              usePlayPauseAction: Boolean = true,
                              fastForwardIncrementMs: Long = 0L,
                              rewindIncrementMs: Long = 0L,
                              smallIcon : Int) = Goonj::customiseNotification

    fun changeActivityIntentForNotification(intent: Intent) = Goonj::changeActivityIntentForNotification

    fun removeNotification() = Goonj::removeNotification

    var imageLoader
        get() = Goonj.imageLoader
        set(value) {
            Goonj.imageLoader = value
        }

    var trackFetcher
        get() = Goonj.trackFetcher
        set(value) {
            Goonj.trackFetcher = value
        }

    var prefetchDistanceWithAutoplay
        get() = Goonj.prefetchDistanceWithAutoplay
        set(value){
            Goonj.prefetchDistanceWithAutoplay = value
        }


    var prefetchDistanceWithoutAutoplay
        get() = Goonj.prefetchDistanceWithoutAutoplay
        set(value){
            Goonj.prefetchDistanceWithoutAutoplay = value
        }

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