package ai.rever.goonj.service

import ai.rever.goonj.interfaces.AudioPlayer
import ai.rever.goonj.models.Track
import android.content.Intent
import androidx.mediarouter.media.MediaItemStatus
import java.util.ArrayList

class GoonjPlayerManager {
    private var player: AudioPlayer? = null
    private var mPlaylist: MutableList<Track> = ArrayList()

    private val currentTrack: Track
        get() =
        if (mPlaylist.isEmpty()) Track()
        else mPlaylist.first { it.currentData.state != MediaItemStatus.PLAYBACK_STATE_FINISHED }

    val trackList get() = mPlaylist

    fun setPlayer(player: AudioPlayer) {
        this.player = player
    }

    fun add(track: Track, index: Int? = null) {
        val trackIndex = index?: mPlaylist.size

        track.currentData.index = trackIndex
        mPlaylist.add(trackIndex, track)

        player?.enqueue(track, trackIndex)

    }

    fun pause() = player?.pause()

    fun resume() = player?.resume()

    fun seek(positionMS: Long) = player?.seekTo(positionMS)

    fun startNewSession(){
        mPlaylist.clear()
        player?.startNewSession()
    }

    val trackPosition get() = player?.getTrackPosition()?: 0

    fun suspend() = player?.suspend()

    fun unsuspend() = player?.unsuspend(currentTrack)

    fun customiseNotification(useNavigationAction: Boolean ,
                              usePlayPauseAction: Boolean ,
                              fastForwardIncrementMs: Long ,
                              rewindIncrementMs: Long,smallIcon: Int ){

        player?.customiseNotification(useNavigationAction, usePlayPauseAction,
            fastForwardIncrementMs, rewindIncrementMs, smallIcon)
    }

    fun setAutoplay(autoplay : Boolean) = player?.setAutoplay(autoplay)

    fun setPendingIntentForNotification(intent: Intent){
        player?.setPendingIntentForNotification(intent)
    }

    fun removeTrack(index : Int){
        mPlaylist.removeAt(index)
        player?.remove(index)
    }

    fun moveTrack(currentIndex: Int, finalIndex: Int){
        val currentTrack = mPlaylist[currentIndex]

        mPlaylist.removeAt(currentIndex)
        mPlaylist.add(finalIndex - 1, currentTrack)

        player?.moveTrack(currentIndex, finalIndex)
    }

    fun skipToNext() = player?.skipToNext()

    fun skipToPrevious() = player?.skipToPrevious()

    fun release() = player?.release()

    fun removeNotification() = player?.removeNotification()

}
