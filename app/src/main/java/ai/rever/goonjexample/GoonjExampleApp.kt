package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.interfaces.PlaybackManager
import ai.rever.goonj.audioplayer.service.AudioPlayerService
import android.app.Application
import android.content.Intent

class GoonjExampleApp: Application() {
    override fun onCreate() {
        super.onCreate()
//        val serviceIntent = Intent(this, AudioPlayerService::class.java)
//        startService(serviceIntent)

        PlaybackManager.getInstance(this).register()
    }

    override fun onTerminate() {
        PlaybackManager.getInstance(this).unregister()
        super.onTerminate()
    }
}