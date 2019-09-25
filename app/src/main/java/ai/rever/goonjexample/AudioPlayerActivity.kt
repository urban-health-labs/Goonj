package ai.rever.goonjexample

import ai.rever.goonj.analytics.analyticsObservable
import ai.rever.goonj.analytics.isLoggable
import ai.rever.goonj.interfaces.GoonjPlayer
import ai.rever.goonj.interfaces.AutoLoadListener
import ai.rever.goonj.models.SAMPLES
import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_audio_player.*
import android.media.AudioManager
import android.view.KeyEvent
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.KeyEvent.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.squareup.picasso.Picasso
import io.reactivex.disposables.Disposable

class AudioPlayerActivity : AppCompatActivity(), GoonjPlayer {

    val TAG = "AUDIO_PLAYER_ACTIVITY"
    var load = true

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_audio_player)

        setupUI()
        customizeNotification()
        setupPlayer()

        setCastButton()

        isLoggable = true
        disposable = analyticsObservable.subscribe {
            logAnalyticsEvent(it.toString())
        }

    }

    private fun setupUI() {

        isPlayingLiveData?.observe(this, Observer { isAudioPlaying ->
            audioPlayerPlayPauseToggleBtn.isChecked = !isAudioPlaying
        })

        audioPlayerPlayPauseToggleBtn.setOnCheckedChangeListener { _, paused ->
            if (paused) {
                pause()
            } else {
                play()
            }
        }

        currentPlayingTrack?.observe(this, Observer { currentItem ->
            Picasso.get().load(currentItem?.imageUrl).into(audioPlayerAlbumArtIV)
            audioPlayerAlbumTitleTv.text = currentItem?.title
            audioPlayerAlbumArtistTv.text = currentItem?.artistName
            Log.d(TAG,"TRACK: $currentItem")
            Log.d(TAG,"Position: ${currentItem.currentData.position}")
            audioPlayerCurrentPosition.text = (currentItem.currentData.position/1000).toString()
            audioPlayerContentDuration.text = (currentItem.currentData.duration/1000).toString()
            audioPlayerProgressBar.progress =
                ((currentItem.currentData.position.toDouble() / currentItem.currentData.duration.toDouble()) * 100.0).toInt()
        })

        audioPlayerForward10s.setOnClickListener {
            seek(5000)
        }

        audioPlayerRewind10s.setOnClickListener {
            seek(-3000)
        }

        audioPlayerAutoplaySwitch.setOnCheckedChangeListener { _, autoplay ->
            val autoLoad = object : AutoLoadListener {
                override fun onLoadTracks() {
                    Log.d(TAG,"============= LOAD NEW TRACKS")
                    if(load) {
                        val handler = Handler()
                        handler.postDelayed({
                            if(load) {
                                addTrack(SAMPLES[4])
                                // add at particular index
//                                addTrack(applicationContext, SAMPLES[5], 2)
//                                removeTrack(applicationContext,0)
//                                moveTrack(applicationContext,0,2)
                                load = false
                            }
                        },4000)

                    }
                }
            }
            setAutoplay(autoplay,1,autoLoad)
        }

        audioPlayerSkipNext.setOnClickListener {
            skipToNext()
        }

        audioPlayerSkipPrev.setOnClickListener {
            skipToPrevious()
        }


    }

    private fun customizeNotification(){
        customizeNotification(true,true,
            10000,5000, R.mipmap.ic_launcher)
    }
    private fun setupPlayer() {
        startNewSession()
        addTrack(SAMPLES[0])
        addTrack(SAMPLES[1])
        addTrack(SAMPLES[2])
        addTrack(SAMPLES[3])
    }

    override fun onBackPressed() {
        removeNotification()
        super.onBackPressed()
        pause()
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
        if (isGooglePlayServicesAvailable())
            CastButtonFactory.setUpMediaRouteButton(this, audioPlayerMRB)
    }

    private fun isGooglePlayServicesAvailable() : Boolean{
        val apiAvailability = GoogleApiAvailability.getInstance()
        val status = apiAvailability.isGooglePlayServicesAvailable(this)
        if(status != ConnectionResult.SUCCESS){
            if(apiAvailability.isUserResolvableError(status)){
                apiAvailability.getErrorDialog(this,status,2404).show()
            }
            return false
        }
        return true
    }

    override fun onDestroy() {
        Log.d(TAG,"=========> apa destroy")
        super.onDestroy()
        disposable.dispose()
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

