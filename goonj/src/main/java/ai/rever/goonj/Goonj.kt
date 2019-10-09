package ai.rever.goonj

import ai.rever.goonj.interfaces.GoonjPlayerServiceInterface
import ai.rever.goonj.models.Track
import ai.rever.goonj.manager.GoonjNotificationManager
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.service.GoonjService
import android.app.Activity
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
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

    /**
     * Shortest way to register
     */
    inline fun<reified T: Activity>register(context: Context) {
        val ctx = if (context.applicationContext == null) context else context.applicationContext
        register(context, Intent(ctx, T::class.java))
    }

    /**
     * Extra parameter could be attached with activity intent
     * e.g.:
     *   val activityIntent = Intent(applicationContext, Activity)
     *   activityIntent.putExtras(KEY, VALUE)
     */
    fun register(context: Context, activityIntent: Intent) = register(context, activityIntent, GoonjService::class.java)

    /**
     * Custom GoonjService could be passed
     *
     * Note: GoonjService must added to app manifest
     */
    fun <S: GoonjService> register(context: Context, activityIntent: Intent, audioServiceClass: Class<S>) {
        if (appContext == null) {
            val ctx = if (context.applicationContext == null) context else context.applicationContext
            weakContext = WeakReference(ctx)
            register(audioServiceClass)
        }

        changeActivityIntentForNotification(activityIntent)
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
        imageLoader = null
        weakContext = null
        goonjPlayerServiceInterface = null
    }

    fun startNewSession() = run { GoonjPlayerManager.startNewSession() }

    fun resume() = run { GoonjPlayerManager.resume() }

    fun pause() = run { GoonjPlayerManager.pause() }

    fun finishTrack() = run { GoonjPlayerManager.finishTrack() }

    fun seekTo(position : Long) = run { GoonjPlayerManager.seekTo(position) }

    fun addTrack(track : Track, index: Int? = null) = run {
        index?.let {
            GoonjPlayerManager.addTrack(track, it)
        } ?: GoonjPlayerManager.addTrack(track)
    }

    fun removeTrack(index : Int) = run { GoonjPlayerManager.removeTrack(index) }

    fun moveTrack(currentIndex : Int, finalIndex : Int) = run {
        GoonjPlayerManager.moveTrack(currentIndex, finalIndex)
    }

    fun skipToNext() = run { GoonjPlayerManager.skipToNext() }

    fun skipToPrevious()  = run { GoonjPlayerManager.skipToPrevious() }

    fun customiseNotification(useNavigationAction: Boolean,
                              usePlayPauseAction: Boolean,
                              fastForwardIncrementMs: Long ,
                              rewindIncrementMs: Long,
                              smallIcon: Int) = run {
        GoonjNotificationManager.customiseNotification(useNavigationAction,
            usePlayPauseAction, fastForwardIncrementMs, rewindIncrementMs, smallIcon)
    }

    fun changeActivityIntentForNotification(intent: Intent) {
        run {
            GoonjNotificationManager.activityIntent = intent
        }
    }

    fun removeNotification() = run { GoonjPlayerManager.removeNotification() }

    var imageLoader: ((Track, (Bitmap?) -> Unit) -> Unit)? = null

    var autoplay: Boolean
        get() = GoonjPlayerManager.autoplayTrackSubject.value?: false
        set(value) = run {
            GoonjPlayerManager.autoplayTrackSubject.onNext(value)
        }

    val trackProgress: Double get() {
        currentTrack?.apply {
            return trackState.position.toDouble() / trackState.duration.toDouble()
        }
        return 0.toDouble()
    }

    val trackList get() = GoonjPlayerManager.trackList

    val playerState: GoonjPlayerState? get() = GoonjPlayerManager.playerStateBehaviorSubject.value

    val currentTrack: Track? get() = GoonjPlayerManager.currentTrackSubject.value

    val trackPosition: Long get() = GoonjPlayerManager.trackPosition

    val playerStateFlowable: Flowable<GoonjPlayerState> get() = GoonjPlayerManager.playerStateBehaviorSubject.toFlowable(BackpressureStrategy.LATEST)

    val currentTrackFlowable: Flowable<Track> get() = GoonjPlayerManager.currentTrackSubject.toFlowable(BackpressureStrategy.LATEST)

    val autoplayFlowable: Flowable<Boolean> get() = GoonjPlayerManager.autoplayTrackSubject.toFlowable(BackpressureStrategy.LATEST)

    val trackCompletionObservable: Observable<Track> get() = GoonjPlayerManager.trackCompleteSubject

    // internal method
    internal fun startForeground(notificationId: Int, notification: Notification?) = run {
        startForeground(notificationId, notification)
    }

    internal fun stopSelf() = run {
        stopSelf()
    }
}

enum class GoonjPlayerState {
    IDLE, BUFFERING, PLAYING, PAUSED, ENDED, CANCELED, ERROR, INVALIDATE
}
