package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.interfaces.GoonjPlayer
import ai.rever.goonj.audioplayer.analytics.analyticsObservable
import ai.rever.goonj.audioplayer.analytics.analyticsObserver
import ai.rever.goonj.audioplayer.analytics.isLoggable
import ai.rever.goonj.audioplayer.models.Samples.SAMPLES
import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_audio_player.*
import android.media.AudioManager
import android.view.KeyEvent
import android.content.Context
import android.util.Log
import android.view.KeyEvent.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers

class AudioPlayerActivity : AppCompatActivity(), GoonjPlayer {

    val TAG = "AUDIO_PLAYER_ACTIVITY"
    val SECOND_LAST = 2

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

            /** This lines loads new items when the current item is the second last item**/
            if(currentItem.index == session(this).size - SECOND_LAST){
                /**
                 * Load your new items here and add to the playlist
                 */
                Log.d(TAG,"========= LOAD NEW ITEMS")
            }
        })

        audioPlayerForward10s.setOnClickListener {
            seek(this,5000)
        }

        audioPlayerRewind10s.setOnClickListener {
            seek(this, -3000)
        }

        audioPlayerAutoplaySwitch.setOnCheckedChangeListener { _, autoplay ->
            setAutoplay(this,autoplay)
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
        addAudioToPlaylist(this, SAMPLES[4])
        addAudioToPlaylist(this, SAMPLES[5])
    }

    override fun onBackPressed() {
        super.onBackPressed()
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
        CastButtonFactory.setUpMediaRouteButton(this, audioPlayerMRB)
    }

    override fun onDestroy() {
        analyticsObservable.unsubscribeOn(AndroidSchedulers.mainThread())
        super.onDestroy()
    }

}

