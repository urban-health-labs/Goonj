package ai.rever.goonj.audioplayer.service

import ai.rever.goonj.audioplayer.util.*
import android.content.Intent
import android.os.IBinder
import com.google.android.exoplayer2.SimpleExoPlayer
import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.mediarouter.media.*
import ai.rever.goonj.audioplayer.SessionManager
import ai.rever.goonj.audioplayer.interfaces.AudioPlayer
import io.reactivex.Observable

class AudioPlayerService : LifecycleService(), AudioManager.OnAudioFocusChangeListener {

    val TAG = "AUDIO_PLAYER_SERvICE"
    lateinit var player: SimpleExoPlayer
    lateinit var audioManager: AudioManager

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

            mPlayer = AudioPlayer.create(this@AudioPlayerService,route)
            mSessionManager.setPlayer(mPlayer!!)
            mSessionManager.unsuspend()

            Log.d(TAG,mPlayer.toString())
            if(route?.isDefault == true) {
                mSessionManager.setRemotePlayerSelected(false)
                mSessionManager.resume()
            } else {
                Log.d(TAG,"Playlist Size: ${mSessionManager.playlist.size}")
                mSessionManager.setRemotePlayerSelected(true)
            }
        }

        override fun onRouteUnselected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            Log.d(TAG, "onRouteUnselected: route=$route")

            mSessionManager.suspend()
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        requestAudioFocus()
        setupProgressObserver()

        isPlaying.observe(this, Observer { isPlaying ->
            if (isPlaying) {
                requestAudioFocus()
                addProgressObserver()
            } else {
                removeAudioFocus()
            }
        })

        // Get the media router service.
        mediaRouter = MediaRouter.getInstance(this)
        mSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        mPlayer = AudioPlayer.create(this, mediaRouter?.selectedRoute)
        mSessionManager.setPlayer(mPlayer!!)
        mSessionManager.setCallback(object : SessionManager.Callback {
            override fun onStatusChanged() {
                //updateUi()
                Log.d(TAG,"SessionManager: onStatusChanged")
            }

            override fun onItemChanged(item: Samples.Sample) {
                Log.d(TAG,"SessionManager: onItemChanged")
            }
        })

    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        mSelector?.also { selector ->
            mediaRouter?.addCallback(selector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        }
    }

    override fun onDestroy() {
        removeAudioFocus()
        mediaRouter?.removeCallback(mediaRouterCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!(intent == null || intent.action == null)) {
            when (intent.action) {
                ACTION_RESUME_SESSION -> {
                    mSessionManager.resume()
                }
                ACTION_PAUSE_SESSION -> {
                    mSessionManager.pause()
                    if(!mSessionManager.isRemote) {
                        stopForeground(true)
                    }
                }
                ACTION_STOP -> {
                    mSessionManager.pause()
                    stopForeground(true)

                }
                ACTION_NEXT -> {
                    // NEXT action
                }
                ACTION_ADD_AUDIO_TO_PLAYLIST -> {
                    val audio = intent.getSerializableExtra(audioURLKey) as Samples.Sample
                    mSessionManager.add(audio)
                }
                ACTION_START_NEW_SESSION ->{
                    mSessionManager.startNewSession()
                }
            }
        }
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                Log.d(TAG, "AUDIOFOCUS_REQUEST_GRANTED")
                mSessionManager.setVolume(1f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(TAG, "AUDIOFOCUS_LOSS")
                mSessionManager.pause()
                removeAudioFocus()

            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT")
                mSessionManager.setVolume(0.1f)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
                mSessionManager.setVolume(0.1f)
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        //Could not gain focus
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this)
    }

}