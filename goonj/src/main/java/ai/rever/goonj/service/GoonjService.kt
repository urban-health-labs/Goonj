package ai.rever.goonj.service

import android.content.Intent
import android.os.IBinder
import android.content.Context
import androidx.mediarouter.media.*
import ai.rever.goonj.interfaces.AudioPlayer
import ai.rever.goonj.interfaces.AutoLoadListener
import ai.rever.goonj.interfaces.GoonjPlayerServiceInterface
import ai.rever.goonj.models.Track
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

open class GoonjService : LifecycleService(),
    GoonjPlayerServiceInterface {

    lateinit var context: Context

    private val trackPositionObservable
        get() = Observable.interval(500, TimeUnit.MILLISECONDS)
            .takeWhile { isPlaying.value?: false}
            .map { manager.trackPosition }

    private var mediaRouter: MediaRouter? = null
    private var mSelector: MediaRouteSelector? = null

    val manager = GoonjPlayerManager()
    private var player: AudioPlayer? = null

    internal val isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    internal val mCurrentPlayingTrack : MutableLiveData<Track> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()
    private var positionDisposable: Disposable? = null

    private var trackCompletion: ((Track) -> Unit)? = null
    private var removeNotification = true

    private val mediaRouterCallback = object : MediaRouter.Callback(){
        override fun onRouteSelected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            player = AudioPlayer.create(this@GoonjService, route)
            player?.let {
                manager.setPlayer(it)
            }
            manager.unsuspend()

            if(route?.isDefault == true || route?.isDefault == true) {
                manager.resume()
            }
        }

        override fun onRouteUnselected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            manager.suspend()
        }
    }

    var mAutoLoadListener: AutoLoadListener? = null

    inner class Binder : android.os.Binder() {
        val goonjPlayerServiceInterface: GoonjPlayerServiceInterface
            get() = this@GoonjService
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        mediaRouter = MediaRouter.getInstance(this)
        mSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
//            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        val player = AudioPlayer.create(this, mediaRouter?.selectedRoute)
        this.player = player
        manager.setPlayer(player)

        mSelector?.let {
            mediaRouter?.addCallback(it, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        }

        isPlayingLiveData.observe(this, Observer {
            if(it){
                positionDisposable?.dispose()
                positionDisposable = trackPositionObservable.subscribe(::onTrackPositionChange)
            }
        })

    }

    private fun onTrackPositionChange(position: Long)  {
        position.let { _position ->
            currentPlayingTrack.value?.let { track ->
                track.currentData.position = _position
                mCurrentPlayingTrack.postValue(track)
            }
        }
    }

    override fun onDestroy() {

        compositeDisposable.dispose()

        positionDisposable?.dispose()

        mediaRouter?.removeCallback(mediaRouterCallback)
        manager.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun play() {
        manager.resume()
    }

    override fun pause() {
        manager.pause()
        stopForeground(true)
    }

    override fun resume() {
        manager.resume()
    }

    override fun seekTo(position: Long) {
        manager.seek(position)
    }

    override fun addToPlaylist(track: Track, index: Int?) {
        manager.add(track, index)
    }

    override fun startNewSession() {
        manager.startNewSession()
    }

    override fun customiseNotification(
        useNavigationAction: Boolean,
        usePlayPauseAction: Boolean,
        fastForwardIncrementMs: Long,
        rewindIncrementMs: Long,
        smallIcon: Int
    ) {
        manager.customiseNotification(useNavigationAction, usePlayPauseAction,
            fastForwardIncrementMs, rewindIncrementMs, smallIcon)
    }

    override fun setAutoplay(autoplay: Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener) {

        mAutoLoadListener = autoLoadListener
        manager.setAutoplay(autoplay)

        mCurrentPlayingTrack.observe(this, Observer { currentTrack ->
            if(trackList.size - indexFromLast == currentTrack.currentData.index){
                mAutoLoadListener?.onLoadTracks()
            }
        })
    }

    override fun setPendingIntentForNotification(intent: Intent) {
        manager.setPendingIntentForNotification(intent)
    }

    override fun removeTrack(index: Int) {
        manager.removeTrack(index)
    }

    override fun moveTrack(currentIndex: Int, finalIndex: Int) {
        manager.moveTrack(currentIndex, finalIndex)
    }

    override fun skipToNext() {
        manager.skipToNext()
    }

    override fun skipToPrevious() {
        manager.skipToPrevious()
    }

    override fun removeNotification() {
        manager.removeNotification()
    }

    override fun setTrackComplete(removeNotification: Boolean, trackCompletion: (Track) -> Unit) {
        this.removeNotification = removeNotification
        this.trackCompletion = trackCompletion
    }

    override fun completeTrack(){
        pause()
        if (removeNotification) {
            stopForeground(true)
        }

        currentPlayingTrack.value?.let {
            trackCompletion?.invoke(it)
        }
    }

    override val isPlayingLiveData: LiveData<Boolean> get() = isPlaying

    override val currentPlayingTrack: LiveData<Track>
        get() = mCurrentPlayingTrack

    override val trackList: List<Track> get() = manager.trackList
}