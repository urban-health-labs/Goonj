package ai.rever.goonj.manager

import ai.rever.goonj.Goonj
import ai.rever.goonj.R
import ai.rever.goonj.util.PLAYBACK_CHANNEL_ID
import ai.rever.goonj.util.PLAYBACK_NOTIFICATION_ID
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.Nullable
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

internal object GoonjNotificationManager {

    val playerNotificationManager: PlayerNotificationManager by lazy {
        PlayerNotificationManager.createWithNotificationChannel(
            Goonj.appContext, PLAYBACK_CHANNEL_ID, R.string.channel_name,
            PLAYBACK_NOTIFICATION_ID,
            notificationAdapter,
            notificationListener
        )
    }

    var activityIntent = Intent()

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

    private val notificationAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String {
            return GoonjPlayerManager.trackList[player.currentWindowIndex].title
        }

        @Nullable
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return PendingIntent.getActivity(
                Goonj.appContext,0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @Nullable
        override fun getCurrentContentText(player: Player): String? {
            return GoonjPlayerManager.trackList[player.currentWindowIndex].artistName
        }

        @Nullable
        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            val track = GoonjPlayerManager.trackList[player.currentWindowIndex]
            track.load {
                callback.onBitmap(it)
            }
            return track.bitmap
        }

    }

    private val notificationListener = object : PlayerNotificationManager.NotificationListener {

        override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
            Goonj.startForeground(notificationId, notification)
        }

        override fun onNotificationCancelled(notificationId: Int) {
            Goonj.stopSelf()
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

}
