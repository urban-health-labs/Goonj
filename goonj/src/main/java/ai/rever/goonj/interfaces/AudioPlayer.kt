package ai.rever.goonj.interfaces

import ai.rever.goonj.manager.GoonjPlayerManager
import androidx.mediarouter.media.MediaRouter
import ai.rever.goonj.models.Track

interface AudioPlayer {

    fun release()
    fun seekTo(positionMs : Long)
    fun pause()
    fun resume()
    fun stop()

    fun enqueue(track: Track, index: Int)

    fun suspend()
    fun unsuspend()

    fun setAutoplay(autoplay: Boolean)

    fun seekTo(index: Int, positionMs: Long) {}

    fun getTrackPosition(): Long = GoonjPlayerManager.currentPlayingTrack.value?.trackState?.position?: 0

    fun connect(route: MediaRouter.RouteInfo) {}
    fun startNewSession() {}
    fun remove(index : Int){}
    fun enqueue(trackList: List<Track>) {}
    fun setVolume(volume: Float) {}
    fun moveTrack(currentIndex: Int, finalIndex: Int){}
    fun skipToNext(){}
    fun skipToPrevious(){}
    fun onRemoveNotification(){}
}

//    companion object{
//        fun create(goonjService: GoonjService, route: MediaRouter.RouteInfo?): AudioPlayer {
//            val isRemote = route?.
//                supportsControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)?: false
//
//            val player: AudioPlayer = if (isRemote) {
//                RemoteAudioPlayer.getInstance(goonjService)
//            } else {
//                LocalAudioPlayer.getInstance(goonjService)
//            }
//
//            route?.let {
//                player.connect(it)
//            }
//            return player
//        }
//    }