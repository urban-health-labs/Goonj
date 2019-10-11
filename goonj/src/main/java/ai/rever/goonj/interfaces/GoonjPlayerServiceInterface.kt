package ai.rever.goonj.interfaces

import android.app.Notification

interface GoonjPlayerServiceInterface {
    fun startForeground(notificationId: Int, notification: Notification?)
    fun stopSelf()
    fun stopForeground(removeNotification: Boolean)
}

