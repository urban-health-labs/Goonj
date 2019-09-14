package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Track
import android.content.Intent
import androidx.lifecycle.LiveData

interface PlaybackInterface {
    fun play()
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
    fun addToPlaylist(track : Track, index: Int?= -1){}
    fun startNewSession()
    fun customiseNotification(useNavigationAction: Boolean, usePlayPauseAction: Boolean,
                              fastForwardIncrementMs: Long , rewindIncrementMs: Long,
                              smallIcon: Int)
    fun setAutoplay(autoplay : Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener)
    fun setPendingActivityForNotification(intent: Intent)
    fun removeTrack(index : Int)
    fun moveTrack(currentIndex: Int, finalIndex: Int)
    fun skipToNext()
    fun skipToPrevious()
    fun removeNotification()
    val isPlayingLiveData: LiveData<Boolean>
    val currentPlayingTrack : LiveData<Track>
    val getSession : List<Track>
}