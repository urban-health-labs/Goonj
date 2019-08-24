package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples
import ai.rever.goonj.audioplayer.service.AudioPlayerService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import java.lang.Exception

class PlaybackManager (private val mContext : Context){

    private var playbackInterface : PlaybackInterface? = null
    private var mServiceBound = false
    private var mServiceConnection: ServiceConnection? = null

    fun register() {
        if(mServiceConnection == null){
            mServiceConnection = object : ServiceConnection{
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    playbackInterface = (service as AudioPlayerService.Binder).service
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
                mContext.applicationContext.unbindService(this.mServiceConnection!!)
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(){
        if(playbackInterface!=null) {
            playbackInterface?.play()
        }

    }

    fun pause(){
        playbackInterface?.pause()
    }

    fun stop(){
        playbackInterface?.stop()
    }

    fun seekTo(position : Long){
        playbackInterface?.seekTo(position)
    }

    fun startNewSession(){
        playbackInterface?.startNewSession()
    }

    fun addAudioToPlaylist(track : Samples.Track){
        playbackInterface?.addToPlaylist(track)
    }

    fun customiseNotification(useNavigationAction: Boolean, usePlayPauseAction: Boolean, fastForwardIncrementMs: Long ,
                              rewindIncrementMs: Long){
        playbackInterface?.customiseNotification(useNavigationAction,usePlayPauseAction,fastForwardIncrementMs,rewindIncrementMs)
    }

    companion object {

        private var mInstance: PlaybackManager? = null

        fun getInstance(context: Context): PlaybackManager {
            if (mInstance == null) {
                synchronized(PlaybackManager::class.java) {
                    if (mInstance == null) {
                        mInstance = PlaybackManager(context)
                    }
                }
            }
            return mInstance!!
        }
    }
}