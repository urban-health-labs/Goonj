package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.interfaces.PlaybackManager
import ai.rever.goonj.audioplayer.service.AudioPlayerService
import android.app.Application
import android.content.Intent

class GoonjExampleApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val pendingIntent = Intent(applicationContext,AudioPlayerActivity::class.java)
        PlaybackManager.getInstance(this).register(pendingIntent)
        PlaybackManager.getInstance(this)
    }

    override fun onTerminate() {
        PlaybackManager.getInstance(this).unregister()
        super.onTerminate()
    }
}