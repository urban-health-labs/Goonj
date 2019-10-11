package ai.rever.goonj.player

import ai.rever.goonj.Goonj
import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.Goonj.currentTrack
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.R
import ai.rever.goonj.analytics.ExoPlayerAnalyticsListenerImp
import ai.rever.goonj.analytics.ExoPlayerEvenListenerImp
import ai.rever.goonj.download.DownloadUtil
import ai.rever.goonj.interfaces.AudioPlayer
import ai.rever.goonj.manager.GoonjNotificationManager
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.manager.GoonjSessionMediaConnector
import ai.rever.goonj.models.Track
import androidx.core.net.toUri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
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
import java.util.*
import java.util.concurrent.TimeUnit

internal class LocalAudioPlayer: AudioPlayer {

    private var isSuspended = false

    private var player: SimpleExoPlayer? = null

    private val simpleExoPlayerGetter get() = run {
        val player = ExoPlayerFactory.newSimpleInstance(appContext)
        val attr = AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).build()
        player.setAudioAttributes(attr, true)
        player
    }

    private val timerObservable
        get() = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .takeWhile { !isSuspended &&
                    GoonjPlayerManager.playerStateSubject.value == GoonjPlayerState.PLAYING }
            .map { (GoonjPlayerManager.currentTrackSubject.value?.state?.position?: 0) + 1000 }

    private val compositeDisposable = CompositeDisposable()
    private var timerDisposable: Disposable? = null
    private val trackList get() = GoonjPlayerManager.trackList

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



    private val concatenatingMediaSource by lazy { ConcatenatingMediaSource() }

    private fun onCreate() {
        player = simpleExoPlayerGetter
        compositeDisposable += GoonjPlayerManager.playerStateSubject
            .subscribe {
                if (it == GoonjPlayerState.PLAYING && !isSuspended){
                    timerDisposable?.dispose()
                    onTrackPositionChange()
                    timerDisposable = timerObservable.subscribe(::onTrackPositionChange)
                }
            }


        addListeners()
        player?.let {
            GoonjSessionMediaConnector.onCreate(it)
        }
        GoonjNotificationManager.setPlayer(player)
    }


    private fun onTrackPositionChange(position: Long = getTrackPosition())  {
        GoonjPlayerManager.currentTrackSubject.value?.let { track ->
            track.state.position = position
            GoonjPlayerManager.currentTrackSubject.onNext(track)
        }
    }


    private val eventListener = object: ExoPlayerEvenListenerImp() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)

            updateCurrentlyPlayingTrack()

            GoonjPlayerManager.playerStateSubject.onNext(
                when (playbackState) {
                    Player.STATE_BUFFERING -> GoonjPlayerState.BUFFERING
                    Player.STATE_ENDED -> {
                        GoonjPlayerManager.currentTrackSubject.value?.let {
                            GoonjPlayerManager.onTrackComplete(it)
                        }
                        GoonjPlayerState.ENDED
                    }
                    Player.STATE_IDLE -> GoonjPlayerState.IDLE
                    else -> if (playWhenReady) {
                        GoonjPlayerState.PLAYING
                    } else {
                        Goonj.stopForeground(false)
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
        player?.apply {
            trackList[currentWindowIndex].let { exoTrack ->
                val lastKnownTrack = GoonjPlayerManager.currentTrackSubject.value

                if (contentDuration > 0) {
                    exoTrack.state.duration = contentDuration
                }
                val playerPosition = getTrackPosition()
                if (playerPosition > 0) {
                    exoTrack.state.position = playerPosition
                } else {
                    exoTrack.state.position = 0
                }

                GoonjPlayerManager.currentTrackSubject.onNext(exoTrack)

                if (exoTrack.id != lastKnownTrack?.id) {
                    exoTrack.state.playedAt = Date().time
                    GoonjPlayerManager.onTrackComplete(lastKnownTrack ?: return)
                    GoonjPlayerManager.autoplayTrackSubject.value?.let {
                        if (!it) {
                            pause()
                        }
                    }
                }
            }
        }
    }

    private fun addListeners(){
        player?.addAnalyticsListener(ExoPlayerAnalyticsListenerImp)
        player?.addListener(eventListener)
    }


    override fun startNewSession(){
        concatenatingMediaSource.clear()
        GoonjNotificationManager.setPlayer(player)
    }

    override fun release() {
        timerDisposable?.dispose()
        compositeDisposable.dispose()

        GoonjPlayerManager.removeNotification()

        GoonjSessionMediaConnector.release()

        player?.removeAnalyticsListener(ExoPlayerAnalyticsListenerImp)
        player?.removeListener(eventListener)
        player?.release()
        player = null
    }

    override fun seekTo(positionMs: Long) {
        if (positionMs < player?.duration?: 0) {
            player?.seekTo(positionMs)
        }
    }

    override fun seekTo(index: Int, positionMs: Long) {
        player?.seekTo(index, positionMs)
    }

    override fun suspend() {
        isSuspended = true
        pause()
    }

    override fun unsuspend() {
        isSuspended = false
        GoonjPlayerManager.currentTrackSubject.value?.state?.apply {
            seekTo(index, position)
            if (GoonjPlayerManager.playerStateSubject.value == GoonjPlayerState.PLAYING) {
                resume()
            }
        }
    }

    override fun pause() {
        player?.playWhenReady = false
    }

    override fun resume() {
        player?.playWhenReady = true
        GoonjNotificationManager.setPlayer(player)
    }

    override fun stop() {
        pause()
    }

    private var isNotPrepared = true
    override fun enqueue(track: Track, index : Int) {
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(track.url.toUri())

        concatenatingMediaSource.addMediaSource(index, mediaSource)

        if (isNotPrepared) {
            player?.prepare(concatenatingMediaSource)
            isNotPrepared = false
        }
    }

    override fun enqueue(trackList: List<Track>) {
        startNewSession()

        trackList.forEachIndexed { index, track ->
            enqueue(track, index)
        }
    }

    override fun remove(index: Int){
        concatenatingMediaSource.removeMediaSource(index)
    }

    override fun moveTrack(currentIndex: Int, finalIndex: Int) {
        concatenatingMediaSource.moveMediaSource(currentIndex, finalIndex)
    }

    override fun skipToNext() {
        player?.next()
    }

    override fun skipToPrevious() {
        player?.previous()
    }

    override fun setVolume(volume: Float) {
        player?.volume = volume
    }

    override fun getTrackPosition() = player?.currentPosition ?: 0

    override fun onRemoveNotification() {
        GoonjNotificationManager.setPlayer(null)
    }

    init {
        onCreate()
    }
}