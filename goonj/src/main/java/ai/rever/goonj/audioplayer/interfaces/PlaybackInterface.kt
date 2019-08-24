package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples
import androidx.lifecycle.LiveData

interface PlaybackInterface {
    fun play()
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
    fun addToPlaylist(track : Samples.Track)
    fun startNewSession()
    fun customiseNotification(useNavigationAction: Boolean, usePlayPauseAction: Boolean, fastForwardIncrementMs: Long ,
                              rewindIncrementMs: Long )
    val isPlayingLiveData: LiveData<Boolean>
    val currentPlayingTrack : LiveData<Samples.Track>
}