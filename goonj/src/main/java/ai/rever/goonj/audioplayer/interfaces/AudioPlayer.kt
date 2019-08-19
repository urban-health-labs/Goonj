package ai.rever.goonj.audioplayer.interfaces

import android.app.Service
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouter
import ai.rever.goonj.audioplayer.cast.RemotePlayer
import ai.rever.goonj.audioplayer.local.LocalPlayer
import ai.rever.goonj.audioplayer.models.Samples

abstract class AudioPlayer {

    protected lateinit var mCallback: Callback

    abstract fun isRemotePlayback(): Boolean
    abstract fun isQueuingSupported(): Boolean

    abstract fun connect(route: MediaRouter.RouteInfo?)
    abstract fun release()

    // basic operations that are always supported
    abstract fun play(item: Samples.Sample)

    abstract fun seek(item: Samples.Sample)
    abstract fun getStatus(item: Samples.Sample, update: Boolean)
    abstract fun pause()
    abstract fun resume()
    abstract fun stop()
    abstract fun startNewSession()

    // advanced queuing (enqueue & remove) are only supported
    // if isQueuingSupported() returns true
    abstract fun enqueue(item: Samples.Sample)

    abstract fun remove(iid: String): Samples.Sample?

    abstract fun setPlaylist(playlist: List<Samples.Sample>)

    abstract fun setVolume(volume: Float)

    abstract fun getTrackPosition() : Long?

    abstract fun customiseNotification(useNavigationAction: Boolean,
                                   usePlayPauseAction: Boolean,
                                   fastForwardIncrementMs: Long,
                                   rewindIncrementMs: Long)


    open fun getStatistics(): String {
        return ""
    }

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
                player = RemotePlayer(context)
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