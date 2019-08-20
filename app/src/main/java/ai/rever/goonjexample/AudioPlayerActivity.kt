package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.models.Samples.SAMPLES
import ai.rever.goonj.audioplayer.util.isPlaying
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_audio_player.*
import android.media.AudioManager
import android.view.KeyEvent
import android.content.Context
import android.view.KeyEvent.*
import com.google.android.gms.cast.framework.CastButtonFactory


class AudioPlayerActivity : BaseActivity() {
    lateinit var playerViewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_audio_player)

        playerViewModel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)

        customizeNotification()
        setupPlayer()
        setupUI()

        setCastButton()

    }

    private fun setupUI() {

        isPlaying.observe(this, Observer { isAudioPlaying ->
            activity_audio_player_play_pause_session_toggle_btn.isChecked = !isAudioPlaying
        })

        activity_audio_player_play_pause_session_toggle_btn.setOnCheckedChangeListener { _, paused ->
            if (paused) {
                playerViewModel.pause(this)
            } else {
                playerViewModel.play(this)
            }
        }

        activity_audio_player_seek_back_img_btn.setOnClickListener {
            playerViewModel.next(this)
        }
    }

    private fun customizeNotification(){
        playerViewModel.customizeNotification(this,false)
    }
    private fun setupPlayer() {
        playerViewModel.startNewSession(this)
        playerViewModel.addAudioToPlaylist(this, SAMPLES[0])
        playerViewModel.addAudioToPlaylist(this, SAMPLES[1])
        playerViewModel.addAudioToPlaylist(this, SAMPLES[2])
        playerViewModel.addAudioToPlaylist(this, SAMPLES[3])
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // TODO ask for confirmation
        playerViewModel.stop(this)
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
                // return false;
                // Update based on @Rene comment below:
                return super.onKeyDown(keyCode, event)
        }
    }

    private fun setCastButton() {
        CastButtonFactory.setUpMediaRouteButton(this, activity_audio_player_mediaroute_btn)
    }

}

