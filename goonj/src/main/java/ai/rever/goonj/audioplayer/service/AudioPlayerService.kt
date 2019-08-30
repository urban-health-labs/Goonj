package ai.rever.goonj.audioplayer.service

import android.content.Intent
import android.os.IBinder
import android.content.Context
import androidx.mediarouter.media.*
import ai.rever.goonj.audioplayer.SessionManager
import ai.rever.goonj.audioplayer.interfaces.AudioPlayer
import ai.rever.goonj.audioplayer.interfaces.AutoLoadListener
import ai.rever.goonj.audioplayer.interfaces.PlaybackInterface
import ai.rever.goonj.audioplayer.models.Samples
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.Observable

class AudioPlayerService : LifecycleService(), PlaybackInterface{

    lateinit var context: Context

    lateinit var playbackObservable: Observable<Long>
    lateinit var playbackObserver: io.reactivex.Observer<Long>

    private var mediaRouter: MediaRouter? = null
    private var mSelector: MediaRouteSelector? = null

    val mSessionManager = SessionManager("app")
    private var mPlayer: AudioPlayer? = null

    internal val mIsPlaying: MutableLiveData<Boolean> = MutableLiveData()
    internal val mCurrentPlayingTrack : MutableLiveData<Samples.Track> = MutableLiveData()

    private val mediaRouterCallback = object : MediaRouter.Callback(){
        override fun onRouteSelected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {

            mPlayer = AudioPlayer.create(this@AudioPlayerService, route)
            mPlayer?.let {
                mSessionManager.setPlayer(it)
            }
            mSessionManager.unsuspend()

            if(route?.isDefault == true || route?.isDefault == true) {
                mSessionManager.setRemotePlayerSelected(false)
                mSessionManager.resume()
            } else {
                mSessionManager.setRemotePlayerSelected(true)
            }
        }

        override fun onRouteUnselected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            if(route?.isDefault == true || route?.isBluetooth == true ) {
                mSessionManager.setRemotePlayerSelected(true)
            } else {
                mSessionManager.setRemotePlayerSelected(false)
            }
            mSessionManager.suspend()
        }
    }

    lateinit var mAutoLoadListener: AutoLoadListener


    inner class Binder : android.os.Binder() {
        val service: PlaybackInterface
            get() = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        setupProgressObserver()

        mediaRouter = MediaRouter.getInstance(this)
        mSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        mPlayer = AudioPlayer.create(this, mediaRouter?.selectedRoute)
        mSessionManager.setPlayer(mPlayer!!)

        mSelector?.also { selector ->
            mediaRouter?.addCallback(selector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        }

        isPlayingLiveData.observe(this, Observer {
            if(it){
                addProgressObserver()
            } else {
                removeProgressObserver()
            }
        })

    }

    override fun onDestroy() {
        removeProgressObserver()
        mediaRouter?.removeCallback(mediaRouterCallback)
        mSessionManager.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return Binder()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mSelector?.also { selector ->
            mediaRouter?.addCallback(selector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        }
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun play() {
        mSessionManager.resume()
    }

    override fun pause() {
        mSessionManager.pause()
        stopForeground(true)
    }

    override fun resume() {
        mSessionManager.resume()
    }

    override fun stop() {
        mSessionManager.stop()
        stopForeground(true)
    }

    override fun seekTo(position: Long) {
        mSessionManager.seek(position)
    }

    override fun addToPlaylist(track: Samples.Track, index: Int?) {
        mSessionManager.add(track,index)
    }

    override fun startNewSession() {
        mSessionManager.startNewSession()
    }

    override fun customiseNotification(
        useNavigationAction: Boolean,
        usePlayPauseAction: Boolean,
        fastForwardIncrementMs: Long,
        rewindIncrementMs: Long
    ) {
        mSessionManager.customiseNotification(useNavigationAction,
            usePlayPauseAction,fastForwardIncrementMs,rewindIncrementMs)
    }

    override fun setAutoplay(autoplay: Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener) {

        mAutoLoadListener = autoLoadListener
        mSessionManager.setAutoplay(autoplay)

        mCurrentPlayingTrack.observe(this, Observer { currentTrack ->
            if(getSession.size - indexFromLast == currentTrack.index){
                mAutoLoadListener.onLoadTracks()
            }
        })
    }

    override fun setPendingActivityForNotification(intent: Intent) {
        mSessionManager.setPendingActivityForNotification(intent)
    }

    override fun removeTrack(index: Int) {
        mSessionManager.removeTrack(index)
    }

    override fun moveTrack(currentIndex: Int, finalIndex: Int) {
        mSessionManager.moveTrack(currentIndex, finalIndex)
    }

    override fun skipToNext() {
        mSessionManager.skipToNext()
    }

    override fun skipToPrevious() {
        mSessionManager.skipToPrevious()
    }

    override val isPlayingLiveData: LiveData<Boolean> get() = mIsPlaying

    override val currentPlayingTrack: LiveData<Samples.Track>
        get() = mCurrentPlayingTrack

    override val getSession: List<Samples.Track> get() = mSessionManager.getSession
}