package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples
import ai.rever.goonj.audioplayer.service.AudioPlayerService
import ai.rever.goonj.audioplayer.util.SingletonHolder
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import java.lang.Exception

class PlaybackManager (private val mContext : Context){

    lateinit var playbackInterface : PlaybackInterface
    private var mServiceBound = false
    private var mServiceConnection: ServiceConnection? = null

    fun register(intent: Intent) {
        if(mServiceConnection == null){
            mServiceConnection = object : ServiceConnection{
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    (service as? AudioPlayerService.Binder)?.service?.let {
                        playbackInterface = it
                        setPendingActivityForNotification(intent)
                    }
                    mServiceBound = true

                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    mServiceBound = false
                }
            }
        }

        try{
            mContext.applicationContext.bindService(Intent(
                mContext.applicationContext, AudioPlayerService::class.java),
                mServiceConnection!!, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregister(){
        try{
            if(this.mServiceBound) {
                this.mServiceConnection?.let {
                    mContext.applicationContext.unbindService(it)
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(){
        playbackInterface.play()
    }

    fun pause(){
        playbackInterface.pause()
    }

    fun stop(){
        playbackInterface.stop()
    }

    fun seekTo(position : Long){
        playbackInterface.seekTo(position)
    }

    fun startNewSession(){
        playbackInterface.startNewSession()
    }

    fun addAudioToPlaylist(track : Samples.Track){
        playbackInterface.addToPlaylist(track)
    }

    fun setAutoplay(autoplay : Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener){
        playbackInterface.setAutoplay(autoplay,indexFromLast,autoLoadListener)
    }

    fun customiseNotification(useNavigationAction: Boolean, usePlayPauseAction: Boolean, fastForwardIncrementMs: Long ,
                              rewindIncrementMs: Long){
        playbackInterface.customiseNotification(useNavigationAction,usePlayPauseAction,fastForwardIncrementMs,rewindIncrementMs)
    }

    fun setPendingActivityForNotification(intent: Intent){
        playbackInterface.setPendingActivityForNotification(intent)
    }

    val isPlayingLiveData get() = playbackInterface.isPlayingLiveData

    val currentPlayingTrack get() = playbackInterface.currentPlayingTrack

    val currentSession get() = playbackInterface.getSession

    companion object : SingletonHolder<PlaybackManager,Context>(::PlaybackManager)
}