package ai.rever.goonj.audioplayer.service

import android.content.Intent
import android.os.IBinder
import android.content.Context
import android.util.Log
import androidx.mediarouter.media.*
import ai.rever.goonj.audioplayer.SessionManager
import ai.rever.goonj.audioplayer.interfaces.AudioPlayer
import ai.rever.goonj.audioplayer.interfaces.PlaybackInterface
import ai.rever.goonj.audioplayer.models.Samples
import android.app.Service
import io.reactivex.Observable

class AudioPlayerService : Service(), PlaybackInterface {

    val TAG = "AUDIO_PLAYER_SERVICE"
    lateinit var context: Context

    lateinit var playbackObservable: Observable<Long>
    lateinit var playbackObserver: io.reactivex.Observer<Long>

    private var mediaRouter: MediaRouter? = null
    private var mSelector: MediaRouteSelector? = null

    val mSessionManager = SessionManager("app")
    private var mPlayer: AudioPlayer? = null

    private val mediaRouterCallback = object : MediaRouter.Callback(){
        override fun onRouteSelected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            Log.d(TAG, "onRouteSelected: route=$route")
            Log.d(TAG,"Playlist Size: ${mSessionManager.playlist.size}")

            mPlayer = AudioPlayer.create(this@AudioPlayerService, route)
            mPlayer?.let {
                mSessionManager.setPlayer(it)
            }
            mSessionManager.unsuspend()

            Log.d(TAG,mPlayer.toString())
            if(route?.isDefault == true || route?.isDefault == true) {
                mSessionManager.setRemotePlayerSelected(false)
                mSessionManager.resume()
            } else {
                Log.d(TAG,"Playlist Size: ${mSessionManager.playlist.size}")
                mSessionManager.setRemotePlayerSelected(true)
            }
        }

        override fun onRouteUnselected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            Log.d(TAG, "onRouteUnselected: route=$route")

            if(route?.isDefault == true || route?.isBluetooth == true ) {
                mSessionManager.setRemotePlayerSelected(true)
            } else {
                mSessionManager.setRemotePlayerSelected(false)
            }
            mSessionManager.suspend()
        }
    }


    inner class Binder : android.os.Binder() {
        val service: PlaybackInterface
            get() = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"oncreated")
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

        //                TODO: cHECK REQUIREMENT
        mSessionManager.setCallback(object : SessionManager.Callback {
            override fun onStatusChanged() {
                //updateUi()
                Log.d(TAG,"SessionManager: onStatusChanged")
            }

            override fun onItemChanged(item: Samples.Track) {
                Log.d(TAG,"SessionManager: onItemChanged")
            }
        })

    }

    override fun onDestroy() {
        removeObserver()
        mediaRouter?.removeCallback(mediaRouterCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
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
        mSessionManager.pause()
        stopForeground(true)
    }

    override fun seekTo(position: Long) {
        mSessionManager.seek(position)
    }

    override fun addToPlaylist(track: Samples.Track) {
        mSessionManager.add(track)
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


}