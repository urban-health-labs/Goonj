package ai.rever.goonj

import ai.rever.goonj.interfaces.GoonjPlayerServiceInterface
import ai.rever.goonj.models.Track
import ai.rever.goonj.manager.GoonjNotificationManager
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.service.GoonjService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.lang.ref.WeakReference

//fun log(message: String) {
//    e("=============>", message)
//}

//fun threadLog() {
//    log("thread ${Thread.currentThread().name}")
//}


object Goonj {

    internal val appContext get() = weakContext?.get()
    private var weakContext: WeakReference<Context>? = null
    internal var imageLoader: ((Track, (Bitmap?) -> Unit) -> Unit)? = null
    private var holderGoonjPlayerServiceInterface:
            WeakReference<GoonjPlayerServiceInterface?>? = null

    private var goonjPlayerServiceInterface
        get() = holderGoonjPlayerServiceInterface?.get()
        set(value) {
            holderGoonjPlayerServiceInterface = WeakReference(value)
            runOnSet()
        }


    private var mServiceConnection: ServiceConnection? = null

    private var runs = ArrayList<GoonjPlayerServiceInterface.() -> Unit>()

    private fun runOnSet() {
        val currentRuns = ArrayList(runs)
        runs.clear()
        run {
            currentRuns.forEach { it() }
        }
    }

    private fun run(run: GoonjPlayerServiceInterface.() -> Unit) {
        if (goonjPlayerServiceInterface != null) {
            goonjPlayerServiceInterface?.apply { run() }
        } else {
            runs.add(run)
        }
    }

    fun initialize(context: Context): Goonj {
        initialize(context, GoonjService::class.java)
        return this
    }

    fun setImageLoader(imageLoader: (Track, (Bitmap?) -> Unit) -> Unit): Goonj {
        this.imageLoader = imageLoader
        return this
    }

    fun setPendingIntentForNotification(intent: Intent): Goonj {
        run {
            GoonjNotificationManager.pendingIntent = intent
        }
        return this
    }

    fun addOnTrackComplete(trackCompletion: (Track) -> Unit): Goonj {
        run {
            GoonjPlayerManager.addOnTrackComplete(trackCompletion)
        }
        return this
    }

    fun <S: GoonjService> initialize(context: Context, audioServiceClass: Class<S>) {
        weakContext = if (context.applicationContext == null) {
            WeakReference(context)
        } else {
            WeakReference(context.applicationContext)
        }
        register(audioServiceClass)
    }

    private fun <T: GoonjService> register(audioServiceClass: Class<T>) {
        if(mServiceConnection == null){
            mServiceConnection = object : ServiceConnection{
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    (binder as? GoonjService.Binder)?.goonjPlayerServiceInterface?.let {
                        goonjPlayerServiceInterface = it
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    goonjPlayerServiceInterface = null
                }

                override fun onBindingDied(name: ComponentName?) {
                    unregister()
                    register(audioServiceClass)
                }
            }
        }

        mServiceConnection?.let {
            appContext?.bindService(
                Intent(appContext, audioServiceClass),
                it, Context.BIND_AUTO_CREATE
            )
        }
    }

    fun unregister() {
        mServiceConnection?.let {
            appContext?.unbindService(it)
        }
        goonjPlayerServiceInterface = null
    }

    fun resume() = run { GoonjPlayerManager.resume() }

    fun pause() = run { GoonjPlayerManager.pause() }

    fun seekTo(position : Long) = run { GoonjPlayerManager.seekTo(position) }

    fun addTrack(track : Track, index: Int? = null) = run {
        index?.let {
            GoonjPlayerManager.addTrack(track, it)
        } ?: GoonjPlayerManager.addTrack(track)
    }

    fun startNewSession() = run { GoonjPlayerManager.startNewSession() }


    fun customiseNotification(useNavigationAction: Boolean,
                              usePlayPauseAction: Boolean,
                              fastForwardIncrementMs: Long ,
                              rewindIncrementMs: Long, smallIcon: Int) = run {
        GoonjNotificationManager.customiseNotification(
            useNavigationAction, usePlayPauseAction,
            fastForwardIncrementMs, rewindIncrementMs, smallIcon
        )
    }

    fun setAutoplay(autoplay : Boolean) = run { GoonjPlayerManager.setAutoplay(autoplay) }

    fun removeTrack(index : Int) = run { GoonjPlayerManager.removeTrack(index) }

    fun moveTrack(currentIndex : Int, finalIndex : Int) = run {
        GoonjPlayerManager.moveTrack(currentIndex, finalIndex)
    }

    fun skipToNext() = run { GoonjPlayerManager.skipToNext() }

    fun skipToPrevious()  = run { GoonjPlayerManager.skipToPrevious() }

    fun removeNotification() = run { GoonjPlayerManager.removeNotification() }

    fun finishTrack() = run { GoonjPlayerManager.finishTrack() }

    val playerStateObservable: Observable<GoonjPlayerState>? get() = GoonjPlayerManager.playerStateBehaviorSubject.observeOn(AndroidSchedulers.mainThread())

    val currentPlayingTrack: Observable<Track>? get() = GoonjPlayerManager.currentPlayingTrack.observeOn(AndroidSchedulers.mainThread())

    val trackList get() = GoonjPlayerManager.trackList

    val trackPosition get() = GoonjPlayerManager.trackPosition

}

enum class GoonjPlayerState {
    IDLE, BUFFERING, PLAYING, PAUSED, ENDED, CANCELED, ERROR, INVALIDATE
}
