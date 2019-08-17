package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.service.AudioPlayerService
import ai.rever.goonj.audioplayer.util.*
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel(){

    var audioUrl = "https://firebasestorage.googleapis.com/v0/b/blissful-ly.appspot.com/o/audio%2F01%20-%20Shiva%20Manas%20Puja.mp3?alt=media&token=3083e16a-98b6-4794-a9db-4af7b26a3186"
    var audioUrl2 = "https://firebasestorage.googleapis.com/v0/b/blissful-ly.appspot.com/o/audio%2FBeside%20the%20Ocean.mp3?alt=media&token=dff725a1-e488-4830-b3f5-2269458310ae"

    fun startNewSession(context: Context){
        performPlayerAction(context, ACTION_START_NEW_SESSION)
    }

    fun play(context: Context) {
        performPlayerAction(context, ACTION_RESUME_SESSION)
    }

    fun pause(context: Context) {
        performPlayerAction(context, ACTION_PAUSE_SESSION)
    }

    fun stop(context: Context){
        performPlayerAction(context, ACTION_STOP)
    }

    fun next(context: Context){
        performPlayerAction(context, ACTION_NEXT)
    }

    fun addAudioToPlaylist(context: Context, audioSample: Samples.Sample){
        var intent = Intent(context, AudioPlayerService::class.java)
        intent.action = ACTION_ADD_AUDIO_TO_PLAYLIST
        intent.putExtra(audioURLKey,audioSample)
        context.startService(intent)
    }

    private fun performPlayerAction(context: Context,action: String){
        var intent = Intent(context,AudioPlayerService::class.java)
        intent.action = action
        context.startService(intent)
    }

}