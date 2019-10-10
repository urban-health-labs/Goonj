package ai.rever.goonjexample

import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.analytics.isLoggable
import ai.rever.goonj.interfaces.GoonjPlayer
import ai.rever.goonj.models.SAMPLES
import ai.rever.goonj.models.Track
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_audio_player.*

class AudioPlayerActivity : AppCompatActivity(), GoonjPlayer {

    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_audio_player)

        setListener()
        customizeNotification()
        setupPlayer()

        setCastButton()

        isLoggable = true

    }

    override fun onResume() {
        super.onResume()

        compositeDisposable += playerStateFlowable.subscribe {
            audioPlayerPlayPauseToggleBtn.isChecked = it != GoonjPlayerState.PLAYING
        }

        compositeDisposable += autoplayFlowable.subscribe {
            audioPlayerAutoplaySwitch.isChecked = it
        }

        compositeDisposable += currentTrackFlowable.subscribe(::onPlayingTrackChange)

        compositeDisposable += trackListFlowable.subscribe {
            tvLoadedTrackCount.text = "Loaded track count: ${it.size}"
        }

    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private var knownTrack: Track = Track()

    private fun onPlayingTrackChange(currentItem: Track) {
        if (knownTrack.id != currentItem.id) {
            currentItem.load { audioPlayerAlbumArtIV.setImageBitmap(it) }
            audioPlayerAlbumTitleTv.text = currentItem.title
            audioPlayerAlbumArtistTv.text = currentItem.artistName
        }
        audioPlayerCurrentPosition.text = (currentItem.state.position / 1000).toString()
        audioPlayerContentDuration.text = (currentItem.state.duration / 1000).toString()
        audioPlayerProgressBar.progress =
            ((currentItem.state.position.toDouble() / currentItem.state.duration.toDouble()) * 100.0).toInt()
        knownTrack = currentItem
    }

    private fun setListener() {

        audioPlayerPlayPauseToggleBtn?.apply {
            setOnClickListener {
                if (isChecked) {
                    pause()
                } else {
                    resume()
                }
            }
        }

        audioPlayerForward10s.setOnClickListener {
            seekTo(trackPosition + 5000)
        }

        audioPlayerRewind10s.setOnClickListener {
            seekTo(trackPosition - 3000)
        }

        audioPlayerAutoplaySwitch.apply {
            setOnClickListener {
                autoplay = isChecked
            }
        }

        audioPlayerSkipNext.setOnClickListener {
            skipToNext()
        }

        audioPlayerSkipPrev.setOnClickListener {
            skipToPrevious()
        }
    }

    private fun customizeNotification() {
        customizeNotification(
            true, true,
            10000, 5000, R.mipmap.ic_launcher
        )
    }

    private fun setupPlayer() {
        startNewSession()
        addTrack(SAMPLES[0])
        resume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        removeNotification()
        pause()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val audioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return when (event?.keyCode) {
            KEYCODE_VOLUME_UP -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
                )
                true
            }
            KEYCODE_VOLUME_DOWN -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
                )
                true
            }
            KEYCODE_BACK -> {
                onBackPressed()
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun setCastButton() {
        if (isGooglePlayServicesAvailable())
            CastButtonFactory.setUpMediaRouteButton(this, audioPlayerMRB)
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val status = apiAvailability.isGooglePlayServicesAvailable(this)
        if (status != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(status)) {
                apiAvailability.getErrorDialog(this, status, 2404).show()
            }
            return false
        }
        return true
    }
}

