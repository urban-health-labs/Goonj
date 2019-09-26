package ai.rever.goonj.player

import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.R
import ai.rever.goonj.analytics.*
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.net.toUri
import ai.rever.goonj.download.DownloadUtil
import ai.rever.goonj.models.Track
import ai.rever.goonj.manager.GoonjNotificationManager.playerNotificationManager
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.util.MEDIA_SESSION_TAG
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import java.util.concurrent.TimeUnit

class LocalAudioPlayer: FocusAudioPlayer() {

    private val trackPositionObservable
        get() = Observable.interval(500, TimeUnit.MILLISECONDS)
            .takeWhile { GoonjPlayerManager.isPlayingBehaviorSubject.value?: false}
            .observeOn(AndroidSchedulers.mainThread())
            .map { GoonjPlayerManager.trackPosition }

    private val compositeDisposable = CompositeDisposable()
    private var positionDisposable: Disposable? = null

    private val cacheDataSourceFactory: CacheDataSourceFactory by lazy {
        val dataSourceFactory = DefaultDataSourceFactory(appContext,
            Util.getUserAgent(appContext, appContext?.getString(R.string.app_name))
        )

        CacheDataSourceFactory(
            DownloadUtil.getCache(),
            dataSourceFactory,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )
    }

    private val mediaSessionConnector: MediaSessionConnector by lazy {
        MediaSessionConnector(MediaSessionCompat(appContext, MEDIA_SESSION_TAG))
    }

    private val concatenatingMediaSource by lazy { ConcatenatingMediaSource() }

    private var autoplay : Boolean = true
    private val playList get() = GoonjPlayerManager.trackList

    private var isFirstTimeAfterCreation = true
    private var isFirstTimeAfterNewSession = true


    private fun onStart() {
        compositeDisposable += GoonjPlayerManager.isPlayingBehaviorSubject
            .subscribe {
                if (it){
                    positionDisposable?.dispose()
                    positionDisposable = trackPositionObservable.subscribe(::onTrackPositionChange)
                }
            }

        val mediaSession = mediaSessionConnector.mediaSession
        mediaSession.isActive = true

        mediaSessionConnector.setPlayer(player)
        playerNotificationManager.setPlayer(player)

        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)

        mediaSessionConnector.setQueueNavigator(object: TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player, index: Int): MediaDescriptionCompat {
                return playList[index].mediaDescription
            }
        })

        addListeners()
    }

    private fun onTrackPositionChange(position: Long)  {
        position.let { _position ->
            GoonjPlayerManager.currentPlayingTrack.value?.let { track ->
                track.state.position = _position
                GoonjPlayerManager.currentPlayingTrack.onNext(track)
            }
        }
    }

    private fun addAudioToPlaylist(audio: Track, index: Int = playList.size) {
        val mediaSource =  ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(audio.url.toUri())

        concatenatingMediaSource.addMediaSource(index, mediaSource)

        if(!player.playWhenReady) {
            player.prepare(concatenatingMediaSource)
            player.playWhenReady = true
        }
    }

    private val eventListener = object: ExoPlayerEvenListenerImp() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)

            updateCurrentlyPlayingTrack()
            /** if player is ready to resume and is commanded to resume **/
            if (playWhenReady && playbackState == Player.STATE_READY) {
                requestAudioFocus(focusLock)
                GoonjPlayerManager.isPlayingBehaviorSubject.onNext(true)

            } else if (!playWhenReady) {
                removeAudioFocus()
                /** attach listener if player is playing for the first time or
                 * if its playing after a new session is played atleast once**/
                if (isFirstTimeAfterCreation || !isFirstTimeAfterNewSession) {
                    /** set false as the player is created**/
                    isFirstTimeAfterCreation = false
                    GoonjPlayerManager.isPlayingBehaviorSubject.onNext(false)
                } else {
                    /** if the player is playing first time after a new session
                     * do not attach the listener **/
                    isFirstTimeAfterNewSession = false
                }
            }
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?,
                                     trackSelections: TrackSelectionArray?) {
            super.onTracksChanged(trackGroups, trackSelections)
            if(!autoplay) {
                pause()
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            super.onPositionDiscontinuity(reason)
            updateCurrentlyPlayingTrack()
        }
    }

    private fun updateCurrentlyPlayingTrack() {
        player.let {
            val currentTrack = playList[it.currentWindowIndex]
            val lastKnownTrack = GoonjPlayerManager.currentPlayingTrack.value

            if (currentTrack.id != lastKnownTrack?.id) {
                GoonjPlayerManager.currentPlayingTrack.onNext(currentTrack)
                GoonjPlayerManager.onTrackComplete(lastKnownTrack?: return)
            }

            if (it.contentDuration > 0) {
                GoonjPlayerManager.currentPlayingTrack.value?.state?.duration = it.contentDuration
            }
        }
    }

    private fun addListeners(){
        player.addAnalyticsListener(ExoPlayerAnalyticsListenerImp)
        player.addListener(eventListener)
    }


    override fun startNewSession(){
        isFirstTimeAfterNewSession = true
        concatenatingMediaSource.clear()
    }

    override fun release() {
        positionDisposable?.dispose()
        compositeDisposable.dispose()

        GoonjPlayerManager.isPlayingBehaviorSubject.onNext(false)

        mediaSessionConnector.mediaSession.release()
        mediaSessionConnector.setPlayer(null)

        playerNotificationManager.setPlayer(null)
        player.removeAnalyticsListener(ExoPlayerAnalyticsListenerImp)
        player.removeListener(eventListener)
        player.release()
    }

    override fun seekTo(positionMs: Long) {
        player.let {
            it.seekTo(it.currentPosition + positionMs)
        }
    }

    override fun suspend() {
        pause()
    }

    override fun unsuspend() {
        resume()
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun resume() {
        player.playWhenReady = true
        playerNotificationManager.setPlayer(player)
    }

    override fun stop() {
        player.stop()
        release()
    }

    override fun enqueue(track: Track, index : Int) {
        addAudioToPlaylist(track, index)
    }

    override fun remove(index: Int){
        concatenatingMediaSource.removeMediaSource(index)
    }

    override fun moveTrack(currentIndex: Int, finalIndex: Int) {
        concatenatingMediaSource.moveMediaSource(currentIndex, finalIndex)
    }

    override fun skipToNext() {
        player.next()
    }

    override fun skipToPrevious() {
        player.previous()
    }

    override fun setPlaylist(playlist: List<Track>) {
        startNewSession()
        for(item in playlist){
            addAudioToPlaylist(item)
        }
    }

    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    override fun getTrackPosition() = player.currentPosition

    override fun setAutoplay(autoplay: Boolean) {
        this.autoplay = autoplay
    }

    override fun onRemoveNotification() {
        playerNotificationManager.setPlayer(null)
    }

    init {
        onStart()
    }
}