package ai.rever.goonj.manager

import ai.rever.goonj.Goonj
import ai.rever.goonj.R
import ai.rever.goonj.models.Track
import ai.rever.goonj.util.MEDIA_SESSION_TAG
import ai.rever.goonj.util.PLAYBACK_CHANNEL_ID
import ai.rever.goonj.util.PLAYBACK_NOTIFICATION_ID
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.Nullable
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager

internal object GoonjNotificationManager {

    private val playerNotificationManager: PlayerNotificationManager by lazy {
        PlayerNotificationManager.createWithNotificationChannel(
            Goonj.appContext, PLAYBACK_CHANNEL_ID, R.string.channel_name,
            PLAYBACK_NOTIFICATION_ID,
            notificationAdapter,
            notificationListener
        )
    }

    var activityIntent = Intent()

    val trackList get() = GoonjPlayerManager.trackList

    /**
     * Customize Notification Manager
     * @param useNavigationAction Display Previous/Next Action
     * @param usePlayPauseAction Display Play/Pause Action
     * @param fastForwardIncrementMs Set forward increment in milliseconds. 0 ms will hide it
     * @param rewindIncrementMs Set rewind increment in milliseconds. 0 ms will hide it
     */
    fun customiseNotification(useNavigationAction: Boolean , usePlayPauseAction: Boolean,
                              fastForwardIncrementMs: Long, rewindIncrementMs: Long, smallIcon: Int?){
        playerNotificationManager.setUseNavigationActions(useNavigationAction)
        playerNotificationManager.setUsePlayPauseActions(usePlayPauseAction)
        playerNotificationManager.setFastForwardIncrementMs(fastForwardIncrementMs)
        playerNotificationManager.setRewindIncrementMs(rewindIncrementMs)
        smallIcon?.let {
            playerNotificationManager.setSmallIcon(it)
        }
    }

    private fun getTrack(player: Player) =
        if (player.currentWindowIndex < trackList.size) {
            trackList[player.currentWindowIndex]
        } else {
            GoonjPlayerManager.currentTrackSubject.value
                ?: GoonjPlayerManager.lastCompletedTrack ?: Track()
        }

    private val notificationAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String {
             return getTrack(player).title
        }

        @Nullable
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return PendingIntent.getActivity(
                Goonj.appContext,0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @Nullable
        override fun getCurrentContentText(player: Player): String? {
            return getTrack(player).artistName
        }

        @Nullable
        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            val track = getTrack(player)
            track.load(callback::onBitmap)
            return track.bitmap
        }

    }

    private val notificationListener = object : PlayerNotificationManager.NotificationListener {

        override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
            Goonj.startForeground(notificationId, notification)
        }

        override fun onNotificationCancelled(notificationId: Int) {
            Goonj.stopForeground(true)
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification?,
            ongoing: Boolean
        ) {
            notification?.contentIntent = PendingIntent.getActivity(
                Goonj.appContext, PLAYBACK_NOTIFICATION_ID,
                activityIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }


    fun setPlayer(simpleExoPlayer: SimpleExoPlayer?) {
        playerNotificationManager.setPlayer(simpleExoPlayer)
        simpleExoPlayer?.let{
            playerNotificationManager.setMediaSessionToken(GoonjSessionMediaConnector.token)
        }
    }
}


internal object GoonjSessionMediaConnector {

    private val mediaSessionConnectorGetter: MediaSessionConnector get() = MediaSessionConnector(MediaSessionCompat(Goonj.appContext, MEDIA_SESSION_TAG))

    private val mediaSession: MediaSessionCompat? get() = mediaSessionConnector?.mediaSession
    val token: MediaSessionCompat.Token? get() = mediaSession?.sessionToken

    var mediaSessionConnector: MediaSessionConnector? = null

    fun onCreate(player: SimpleExoPlayer) {
        mediaSessionConnector = mediaSessionConnectorGetter

        mediaSessionConnector?.setPlayer(player)

        mediaSession?.isActive = true
        mediaSessionConnector?.setQueueNavigator(object :
            TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(
                player: Player,
                index: Int
            ): MediaDescriptionCompat {
                return GoonjNotificationManager.trackList[index].mediaDescription
            }
        })
    }

    fun release() {
        mediaSessionConnector?.mediaSession?.release()
        mediaSessionConnector?.setPlayer(null)
        mediaSessionConnector = null
    }
}
