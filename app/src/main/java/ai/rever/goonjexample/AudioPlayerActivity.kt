package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.interfaces.GoonjPlayer
import ai.rever.goonj.audioplayer.analytics.analyticsObservable
import ai.rever.goonj.audioplayer.analytics.analyticsObserver
import ai.rever.goonj.audioplayer.analytics.isLoggable
import ai.rever.goonj.audioplayer.models.Samples.SAMPLES
import ai.rever.goonj.audioplayer.util.isPlaying
import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_audio_player.*
import android.media.AudioManager
import android.view.KeyEvent
import android.content.Context
import android.view.KeyEvent.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import io.reactivex.android.schedulers.AndroidSchedulers

class AudioPlayerActivity : AppCompatActivity(), GoonjPlayer {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_audio_player)

        customizeNotification()
        setupPlayer()
        setupUI()

        setCastButton()

        isLoggable = true
        analyticsObservable.subscribe(analyticsObserver)

    }

    private fun setupUI() {

        isPlaying.observe(this, Observer { isAudioPlaying ->
            activity_audio_player_play_pause_session_toggle_btn.isChecked = !isAudioPlaying
        })

        activity_audio_player_play_pause_session_toggle_btn.setOnCheckedChangeListener { _, paused ->
            if (paused) {
                pause(this)
            } else {
                play(this)
            }
        }

        activity_audio_player_seek_back_img_btn.setOnClickListener {
            next(this)
        }
    }

    private fun customizeNotification(){
        customizeNotification(this,true,true,10000,5000)
    }
    private fun setupPlayer() {
        startNewSession(this)
        addAudioToPlaylist(this, SAMPLES[0])
        addAudioToPlaylist(this, SAMPLES[1])
        addAudioToPlaylist(this, SAMPLES[2])
        addAudioToPlaylist(this, SAMPLES[3])
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // TODO ask for confirmation
        stop(this)
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
        CastButtonFactory.setUpMediaRouteButton(this, activity_audio_player_mediaroute_btn)
    }

    override fun onDestroy() {
        analyticsObservable.unsubscribeOn(AndroidSchedulers.mainThread())
        super.onDestroy()
    }

}

