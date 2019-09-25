package ai.rever.goonjexample

import ai.rever.goonj.GoonjPlayer
import ai.rever.goonj.models.Track
import android.app.Application
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

class GoonjExampleApp: Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onAppCreate() {
        val pendingIntent = Intent(applicationContext, AudioPlayerActivity::class.java)

        GoonjPlayer.initialize(this)
            .setPendingIntentForNotification(pendingIntent)
            .setTrackComplete {  }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroy() {
        Log.e("=============>", "destroy")

        GoonjPlayer.unregister()
    }
}

