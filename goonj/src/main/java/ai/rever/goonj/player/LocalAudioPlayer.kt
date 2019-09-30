package ai.rever.goonj.player

import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.R
import ai.rever.goonj.analytics.ExoPlayerAnalyticsListenerImp
import ai.rever.goonj.analytics.ExoPlayerEvenListenerImp
import ai.rever.goonj.download.DownloadUtil
import ai.rever.goonj.interfaces.AudioPlayer
import ai.rever.goonj.manager.GoonjNotificationManager.playerNotificationManager
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.models.Track
import ai.rever.goonj.util.MEDIA_SESSION_TAG
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.net.toUri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LocalAudioPlayer: AudioPlayer {

    private var isSuspended = false

    private val player: SimpleExoPlayer by lazy {
        val player = ExoPlayerFactory.newSimpleInstance(appContext)
        val attr = AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).build()
        player.setAudioAttributes(attr, true)
        player
    }

    private val timerObservable
        get() = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .takeWhile { !isSuspended &&
                    GoonjPlayerManager.playerStateBehaviorSubject.value == GoonjPlayerState.PLAYING }
            .map { (GoonjPlayerManager.currentPlayingTrack.value?.trackState?.position?: 0) + 1000 }

    private val compositeDisposable = CompositeDisposable()
    private var timerDisposable: Disposable? = null

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
    private val trackList get() = GoonjPlayerManager.trackList

    private fun onStart() {
        compositeDisposable += GoonjPlayerManager.playerStateBehaviorSubject
            .subscribe {
                if (it == GoonjPlayerState.PLAYING && !isSuspended){
                    timerDisposable?.dispose()
                    onTrackPositionChange()
                    timerDisposable = timerObservable.subscribe(::onTrackPositionChange)
                }
            }

        val mediaSession = mediaSessionConnector.mediaSession
        mediaSession.isActive = true

        mediaSessionConnector.setPlayer(player)
        playerNotificationManager.setPlayer(player)

        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)

        mediaSessionConnector.setQueueNavigator(object: TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player, index: Int): MediaDescriptionCompat {
                return trackList[index].mediaDescription
            }
        })

        addListeners()
    }


    private fun onTrackPositionChange(position: Long = getTrackPosition())  {
        GoonjPlayerManager.currentPlayingTrack.value?.let { track ->
            track.trackState.position = position
            GoonjPlayerManager.currentPlayingTrack.onNext(track)
        }
    }


    private val eventListener = object: ExoPlayerEvenListenerImp() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)

            updateCurrentlyPlayingTrack()

            GoonjPlayerManager.playerStateBehaviorSubject.onNext(
                when (playbackState) {
                    Player.STATE_BUFFERING -> GoonjPlayerState.BUFFERING
                    Player.STATE_ENDED -> GoonjPlayerState.ENDED
                    Player.STATE_IDLE -> GoonjPlayerState.IDLE
                    else -> if (playWhenReady) {
                        GoonjPlayerState.PLAYING
                    } else {
                        GoonjPlayerState.PAUSED
                    }
                })
        }

        override fun onPositionDiscontinuity(reason: Int) {
            super.onPositionDiscontinuity(reason)
            updateCurrentlyPlayingTrack()

        }
    }

    private fun updateCurrentlyPlayingTrack() {
        if (trackList.isEmpty()) return
        val currentTrack =  trackList[player.currentWindowIndex]
        val lastKnownTrack = GoonjPlayerManager.currentPlayingTrack.value

        if (player.contentDuration > 0) {
            currentTrack.trackState.duration = player.contentDuration
        }
        val playerPosition = getTrackPosition()
        if (playerPosition > 0) {
            currentTrack.trackState.position = playerPosition
        } else {
            currentTrack.trackState.position = 0
        }

        GoonjPlayerManager.currentPlayingTrack.onNext(currentTrack)

        if (currentTrack.id != lastKnownTrack?.id) {
            GoonjPlayerManager.onTrackComplete(lastKnownTrack?: return)
            if(!autoplay) {
                pause()
            }
        }
    }

    private fun addListeners(){
        player.addAnalyticsListener(ExoPlayerAnalyticsListenerImp)
        player.addListener(eventListener)
    }


    override fun startNewSession(){
        concatenatingMediaSource.clear()
    }

    override fun release() {
        timerDisposable?.dispose()
        compositeDisposable.dispose()

        mediaSessionConnector.mediaSession.release()
        mediaSessionConnector.setPlayer(null)

        playerNotificationManager.setPlayer(null)

        player.removeAnalyticsListener(ExoPlayerAnalyticsListenerImp)
        player.removeListener(eventListener)
        player.release()
    }

    override fun seekTo(positionMs: Long) {
        Log.e("==========>", "$positionMs")
        player.seekTo(positionMs)
    }

    override fun seekTo(index: Int, positionMs: Long) {
        player.seekTo(index, positionMs)
    }

    override fun suspend() {
        isSuspended = true
        pause()
    }

    override fun unsuspend() {
        isSuspended = false
        GoonjPlayerManager.currentPlayingTrack.value?.trackState?.apply {
            seekTo(index, position)
            if (state == GoonjPlayerState.PLAYING) {
                resume()
            }
        }
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
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(track.url.toUri())

        concatenatingMediaSource.addMediaSource(index, mediaSource)

        player.prepare(concatenatingMediaSource)

    }

    override fun enqueue(trackList: List<Track>) {
        startNewSession()
        for(item in trackList){
            enqueue(item, this.trackList.size)
        }
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