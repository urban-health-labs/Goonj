package ai.rever.goonj.interfaces

import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouter
import ai.rever.goonj.cast.RemoteAudioPlayer
import ai.rever.goonj.local.LocalAudioPlayer
import ai.rever.goonj.models.Track
import ai.rever.goonj.service.GoonjService
import android.content.Intent
import java.lang.ref.WeakReference

abstract class AudioPlayer(goonjService: GoonjService) {

    private val weakService : WeakReference<GoonjService> by lazy { WeakReference(goonjService) }
    val goonjService: GoonjService? get() = weakService.get()
    val appContext get() = goonjService?.applicationContext


    abstract fun connect(route: MediaRouter.RouteInfo)
    abstract fun release()
    abstract fun play(item: Track)
    abstract fun seekTo(positionMs : Long)
    abstract fun pause()
    abstract fun resume()
    abstract fun stop()

    open fun startNewSession() {}

    abstract fun enqueue(track: Track, index: Int = -1)

    abstract fun suspend()
    abstract fun unsuspend(track: Track)

    open fun remove(index : Int){}
    open fun setPlaylist(playlist: List<Track>) {}
    open fun setVolume(volume: Float) {}
    open fun getTrackPosition() : Long = 0

    open fun customiseNotification(useNavigationAction: Boolean,
                                   usePlayPauseAction: Boolean,
                                   fastForwardIncrementMs: Long,
                                   rewindIncrementMs: Long,
                                   smallIcon: Int?) {}

    abstract fun setAutoplay(autoplay: Boolean)
    open fun setPendingIntentForNotification(intent: Intent){}
    open fun moveTrack(currentIndex: Int, finalIndex: Int){}
    open fun skipToNext(){}
    open fun skipToPrevious(){}
    open fun removeNotification(){}

    companion object{
        fun create(goonjService: GoonjService, route: MediaRouter.RouteInfo?): AudioPlayer {
            val isRemote = route?.
                supportsControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)?: false

            val player: AudioPlayer = if (isRemote) {
                RemoteAudioPlayer.getInstance(goonjService)
            } else {
                LocalAudioPlayer.getInstance(goonjService)
            }

            route?.let {
                player.connect(it)
            }


            return player
        }
    }
}