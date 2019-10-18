package ai.rever.goonj.manager

import ai.rever.goonj.Goonj
import ai.rever.goonj.util.MEDIA_SESSION_TAG
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import io.reactivex.disposables.Disposable

internal object LocalPlayerSMCManager {

    private val mediaSessionConnectorGetter: MediaSessionConnector
        get() = MediaSessionConnector(
            MediaSessionCompat(Goonj.appContext, MEDIA_SESSION_TAG)
        )

    private val mediaSession: MediaSessionCompat? get() = mediaSessionConnector?.mediaSession
    val token: MediaSessionCompat.Token? get() = mediaSession?.sessionToken

    private var mediaSessionConnector: MediaSessionConnector? = null

    fun subscribe(player: SimpleExoPlayer) = object: Disposable {
        init {
            mediaSessionConnector = mediaSessionConnectorGetter

            mediaSessionConnector?.setPlayer(player)

            mediaSession?.isActive = true
            mediaSessionConnector?.setQueueNavigator(object :
                TimelineQueueNavigator(mediaSession) {
                override fun getMediaDescription(
                    player: Player,
                    index: Int
                ): MediaDescriptionCompat {
                    return GoonjPlayerManager.trackList[index].mediaDescription
                }
            })
        }


        override fun isDisposed() = mediaSessionConnector == null

        override fun dispose() {
            mediaSessionConnector?.mediaSession?.release()
            mediaSessionConnector?.setPlayer(null)
            mediaSessionConnector = null

        }

    }

}