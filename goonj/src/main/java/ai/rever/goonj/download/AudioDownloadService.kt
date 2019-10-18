package ai.rever.goonj.download

import ai.rever.goonj.R
import android.app.Notification
import androidx.annotation.Nullable
import ai.rever.goonj.util.DOWNLOAD_CHANNEL_ID
import ai.rever.goonj.util.DOWNLOAD_NOTIFICATION_ID
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper


internal class AudioDownloadService : DownloadService (
    DOWNLOAD_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_CHANNEL_ID,
    R.string.channel_download_name
) {

    override fun getForegroundNotification(downloads: MutableList<Download>?): Notification =
        DownloadNotificationHelper(this, DOWNLOAD_CHANNEL_ID)
            .buildProgressNotification(R.drawable.exo_icon_play,
                null, null, downloads)

    override fun getDownloadManager(): DownloadManager {
        return GoonjDownloadManager.downloadManager
    }

    @Nullable
    override fun getScheduler(): Scheduler? {
        return null
    }
}