package ai.rever.goonj.interfaces

import ai.rever.goonj.models.Track
import ai.rever.goonj.service.GoonjService
import android.app.Notification
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface GoonjPlayerServiceInterface {
    fun startForeground(notificationId: Int, notification: Notification?)
    fun stopSelf()
}

