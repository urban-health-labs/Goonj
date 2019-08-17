package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.service.AudioPlayerService
import android.app.Application
import android.content.Intent

class GoonjExample: Application() {
    override fun onCreate() {
        super.onCreate()
        startService( Intent(this, AudioPlayerService::class.java))
    }
}