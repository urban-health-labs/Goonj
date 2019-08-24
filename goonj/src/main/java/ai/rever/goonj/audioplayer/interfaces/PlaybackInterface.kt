package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples

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
}