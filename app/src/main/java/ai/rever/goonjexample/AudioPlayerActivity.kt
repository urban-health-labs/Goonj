package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.analytics.*
import ai.rever.goonj.audioplayer.interfaces.GoonjPlayer
import ai.rever.goonj.audioplayer.interfaces.AutoLoadListener
import ai.rever.goonj.audioplayer.models.Samples
import ai.rever.goonj.audioplayer.models.Samples.SAMPLES
import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_audio_player.*
import android.media.AudioManager
import android.view.KeyEvent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.KeyEvent.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.cast.framework.CastButtonFactory
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class AudioPlayerActivity : AppCompatActivity(), GoonjPlayer {

    val TAG = "AUDIO_PLAYER_ACTIVITY"
    var load = true

    val analyticsObserver = object : io.reactivex.Observer<AnalyticsModel> {
        override fun onComplete() {
            logAnalyticsEvent("onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            logAnalyticsEvent("onSubscribe")
        }

        override fun onNext(t: AnalyticsModel) {
            logAnalyticsEvent(t.toString())
        }

        override fun onError(e: Throwable) {
            logAnalyticsEvent(e.message, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_audio_player)

        setupUI()
        customizeNotification()
        setupPlayer()


        setCastButton()

        isLoggable = true
        analyticsObservable.subscribe(analyticsObserver)

    }

    private fun setupUI() {

        isPlayingLiveData(this).observe(this, Observer { isAudioPlaying ->
            audioPlayerPlayPauseToggleBtn.isChecked = !isAudioPlaying
        })

        audioPlayerPlayPauseToggleBtn.setOnCheckedChangeListener { _, paused ->
            if (paused) {
                pause(this)
            } else {
                play(this)
            }
        }

        currentPlayingTrack(this).observe(this, Observer { currentItem ->
            Picasso.get().load(currentItem?.albumArtUrl).into(audioPlayerAlbumArtIV)
            audioPlayerAlbumTitleTv.text = currentItem?.title
            audioPlayerAlbumArtistTv.text = currentItem?.artist
            Log.d(TAG,"TRACK: $currentItem")
            Log.d(TAG,"Position: ${currentItem.position}")
            audioPlayerCurrentPosition.text = (currentItem.position/1000).toString()
            audioPlayerContentDuration.text = (currentItem.duration/1000).toString()
            audioPlayerProgressBar.progress =
                ((currentItem.position.toDouble() / currentItem.duration.toDouble()) * 100.0).toInt()
        })

        audioPlayerForward10s.setOnClickListener {
            seek(this,5000)
        }

        audioPlayerRewind10s.setOnClickListener {
            seek(this, -3000)
        }

        audioPlayerAutoplaySwitch.setOnCheckedChangeListener { _, autoplay ->
            val autoLoad = object : AutoLoadListener{
                override fun onLoadTracks() {
                    Log.d(TAG,"============= LOAD NEW TRACKS")
                    if(load) {
                        val handler = Handler()
                        handler.postDelayed({
                            if(load) {
                                addAudioToPlaylist(applicationContext, SAMPLES[4])
                                // add at particular index
//                                addAudioToPlaylist(applicationContext, SAMPLES[5], 2)
//                                removeTrack(applicationContext,0)
//                                moveTrack(applicationContext,0,2)
                                load = false
                            }
                        },4000)

                    }
                }
            }
            setAutoplay(this,autoplay,1,autoLoad)
        }

        audioPlayerSkipNext.setOnClickListener {
            skipToNext(this)
        }

        audioPlayerSkipPrev.setOnClickListener {
            skipToPrevious(this)
        }


    }

    private fun customizeNotification(){
        customizeNotification(this,true,true,
            10000,5000,R.mipmap.ic_launcher)
    }
    private fun setupPlayer() {
        startNewSession(this)
        addAudioToPlaylist(this, SAMPLES[0])
//        addAudioToPlaylist(this, SAMPLES[1])
//        addAudioToPlaylist(this, SAMPLES[2])
//        addAudioToPlaylist(this, SAMPLES[3])
    }

    override fun onBackPressed() {
        removeNotification(this)
        super.onBackPressed()
        pause(this)

    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (val keyCode = event.keyCode) {
            KEYCODE_VOLUME_UP -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
                )
                return true
            }
            KEYCODE_VOLUME_DOWN -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
                )
                return true
            }
            KEYCODE_BACK ->{
                onBackPressed()
                return true
            }
            else ->
                return super.onKeyDown(keyCode, event)
        }
    }

    private fun setCastButton() {
        CastButtonFactory.setUpMediaRouteButton(this, audioPlayerMRB)
    }

    override fun onDestroy() {
        Log.d(TAG,"=========> apa destroy")
        analyticsObservable.unsubscribeOn(AndroidSchedulers.mainThread())
        super.onDestroy()
    }


    private fun logAnalyticsEvent(message : String?, error : Boolean ?= false){
        val TAG = "ANALYTICS"
        if(error == true){
            Log.e(TAG,"=======error: $message")
        } else if(isLoggable){
            message?.let {
                Log.d(TAG,message)
            }
        }
    }


}

