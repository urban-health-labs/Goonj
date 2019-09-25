package ai.rever.goonj.interfaces

import ai.rever.goonj.models.Track
import android.content.Intent
import androidx.lifecycle.LiveData

interface GoonjPlayerServiceInterface {
    fun play()
    fun pause()
    fun resume()
    fun seekTo(position: Long)
    fun addToPlaylist(track : Track, index: Int? = null){}
    fun startNewSession()
    fun customiseNotification(useNavigationAction: Boolean, usePlayPauseAction: Boolean,
                              fastForwardIncrementMs: Long , rewindIncrementMs: Long,
                              smallIcon: Int)
    fun setAutoplay(autoplay : Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener)
    fun setPendingIntentForNotification(intent: Intent)
    fun removeTrack(index : Int)
    fun moveTrack(currentIndex: Int, finalIndex: Int)
    fun skipToNext()
    fun skipToPrevious()
    fun removeNotification()
    fun completeTrack()
    fun setTrackComplete(removeNotification: Boolean, trackCompletion: (Track) -> Unit)
    val isPlayingLiveData: LiveData<Boolean>
    val currentPlayingTrack: LiveData<Track>
    val trackList : List<Track>

}