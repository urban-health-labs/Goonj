package ai.rever.goonj.service

import android.app.Notification

interface GoonjPlayerServiceInterface {
    fun startForeground(notificationId: Int, notification: Notification?)
    fun stopSelf()
    fun stopForeground(removeNotification: Boolean)
}

