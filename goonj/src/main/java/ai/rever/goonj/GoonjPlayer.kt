package ai.rever.goonj

import ai.rever.goonj.interfaces.AutoLoadListener
import ai.rever.goonj.interfaces.GoonjPlayerServiceInterface
import ai.rever.goonj.models.Track
import ai.rever.goonj.service.GoonjService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import java.lang.ref.WeakReference


object GoonjPlayer {

    private var weakContext: WeakReference<Context>? = null
    private val applicationContext get() = weakContext?.get()

    private var holderGoonjPlayerServiceInterface : GoonjPlayerServiceInterface? = null
    private var goonjPlayerServiceInterface
        get() = holderGoonjPlayerServiceInterface
        set(value) {
            holderGoonjPlayerServiceInterface = value
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
            goonjPlayerServiceInterface?.apply(run)
        } else {
            runs.add(run)
        }
    }


    fun initialize(context: Context): GoonjPlayer {
        initialize(
            context,
            GoonjService::class.java
        )
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
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    (service as? GoonjService.Binder)?.goonjPlayerServiceInterface?.let {
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
            applicationContext?.bindService(
                Intent(applicationContext, audioServiceClass),
                it, Context.BIND_AUTO_CREATE
            )
        }
    }

    fun unregister(){
        mServiceConnection?.let {
            applicationContext?.unbindService(it)
        }
        goonjPlayerServiceInterface = null
    }

    fun play() = run { play() }

    fun pause() = run { pause() }

    fun seekTo(position : Long) = run { seekTo(position) }

    fun startNewSession() = run { startNewSession() }

    fun addTrack(track : Track, index: Int? = null) =
        run {
            addToPlaylist(track, index)
        }

    fun setAutoplay(autoplay : Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener) =
        run {
            setAutoplay(
                autoplay,
                indexFromLast,
                autoLoadListener
            )
        }

    fun customiseNotification(useNavigationAction: Boolean,
                              usePlayPauseAction: Boolean,
                              fastForwardIncrementMs: Long ,
                              rewindIncrementMs: Long, smallIcon: Int) =
        run {
            customiseNotification(
                useNavigationAction, usePlayPauseAction,
                fastForwardIncrementMs, rewindIncrementMs, smallIcon
            )
        }


    fun removeTrack(index : Int) = run { removeTrack(index) }

    fun moveTrack(currentIndex : Int, finalIndex : Int) =
        run {
            moveTrack(currentIndex, finalIndex)
        }

    fun skipToNext() = run { skipToNext() }

    fun skipToPrevious()  = run { skipToPrevious() }

    fun removeNotification() = run { removeNotification() }

    fun completeTrack() = run { completeTrack() }

    fun setPendingIntentForNotification(intent: Intent): GoonjPlayer {
        run {
            setPendingIntentForNotification(intent)
        }
        return this
    }

    fun setTrackComplete(removeNotification: Boolean = true, trackCompletion: (Track) -> Unit): GoonjPlayer {
        run { setTrackComplete(removeNotification, trackCompletion) }
        return this
    }

    val isPlayingLiveData get() = goonjPlayerServiceInterface?.isPlayingLiveData

    val currentPlayingTrack get() = goonjPlayerServiceInterface?.currentPlayingTrack

    val currentTrack get() = goonjPlayerServiceInterface?.trackList

}
