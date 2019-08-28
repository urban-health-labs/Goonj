package ai.rever.goonj.audioplayer.interfaces

import android.app.Service
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouter
import ai.rever.goonj.audioplayer.cast.RemotePlayer
import ai.rever.goonj.audioplayer.local.LocalPlayer
import ai.rever.goonj.audioplayer.models.Samples
import android.content.Intent
import java.lang.ref.WeakReference

abstract class AudioPlayer {

    protected lateinit var mCallback: Callback

    abstract fun isRemotePlayback(): Boolean
    abstract fun isQueuingSupported(): Boolean

    abstract fun connect(route: MediaRouter.RouteInfo?)
    abstract fun release()

    // basic operations that are always supported
    abstract fun play(item: Samples.Track)

    abstract fun seekTo(positionMs : Long)
    open fun getStatus(item: Samples.Track, seek: Boolean, positionMs: Long){}
    abstract fun pause()
    abstract fun resume()
    abstract fun stop()
    abstract fun startNewSession()

    // advanced queuing (enqueue & remove) are only supported
    // if isQueuingSupported() returns true
    abstract fun enqueue(item: Samples.Track, index: Int = -1)

    abstract fun remove(index : Int)

    abstract fun setPlaylist(playlist: List<Samples.Track>)

    abstract fun setVolume(volume: Float)

    abstract fun getTrackPosition() : Long?

    open fun customiseNotification(useNavigationAction: Boolean,
                                   usePlayPauseAction: Boolean,
                                   fastForwardIncrementMs: Long,
                                   rewindIncrementMs: Long) {}

    abstract fun setAutoplay(autoplay: Boolean)

    open fun setPendingActivityForNotification(intent: Intent){}

    open fun moveTrack(currentIndex: Int, finalIndex: Int){}

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    companion object{
        fun create(context: Service, route: MediaRouter.RouteInfo?): AudioPlayer {
            val player: AudioPlayer
            if (route != null && route.supportsControlCategory(
                    MediaControlIntent.CATEGORY_REMOTE_PLAYBACK
                )
            ) {
                player = RemotePlayer.getInstance(WeakReference(context))
            } else {
                player = LocalPlayer.getInstance(context)
            }
            player.connect(route)
            return player
        }
    }

    interface Callback {
        fun onError()
        fun onCompletion()
        fun onPlaylistChanged()
        fun onPlaylistReady()
    }

}