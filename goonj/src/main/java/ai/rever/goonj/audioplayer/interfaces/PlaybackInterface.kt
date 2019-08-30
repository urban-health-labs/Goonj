package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples
import android.content.Intent
import androidx.lifecycle.LiveData

interface PlaybackInterface {
    fun play()
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
    fun addToPlaylist(track : Samples.Track, index: Int?= -1){}
    fun startNewSession()
    fun customiseNotification(useNavigationAction: Boolean, usePlayPauseAction: Boolean,
                              fastForwardIncrementMs: Long , rewindIncrementMs: Long )
    fun setAutoplay(autoplay : Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener)
    fun setPendingActivityForNotification(intent: Intent)
    fun removeTrack(index : Int)
    fun moveTrack(currentIndex: Int, finalIndex: Int)
    fun skipToNext()
    fun skipToPrevious()
    val isPlayingLiveData: LiveData<Boolean>
    val currentPlayingTrack : LiveData<Samples.Track>
    val getSession : List<Samples.Track>
}