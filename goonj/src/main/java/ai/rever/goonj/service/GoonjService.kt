package ai.rever.goonj.service

import android.content.Intent
import android.os.IBinder
import ai.rever.goonj.interfaces.GoonjPlayerServiceInterface
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.player.LocalAudioPlayer
import ai.rever.goonj.player.RemoteAudioPlayer
import android.app.Service
import android.content.Context
import androidx.lifecycle.LifecycleService

open class GoonjService: Service(),
    GoonjPlayerServiceInterface {


//    private val mediaRouter: MediaRouter by lazy { MediaRouter.getInstance(this) }

    private val binder = Binder()

//    private val selector: MediaRouteSelector by lazy {
//        MediaRouteSelector.Builder()
//            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
//            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
//            .build()
//    }


//    private val mediaRouterCallback = object : MediaRouter.Callback(){
//        override fun onRouteSelected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
//            player = AudioPlayer.create(this@GoonjService, route)
//            player?.let {
//                GoonjPlayerManager.setPlayer(it)
//            }
//            GoonjPlayerManager.unsuspend()
//
//            if(route?.isDefault == true) {
//                GoonjPlayerManager.resume()
//            }
//        }
//
//        override fun onRouteUnselected(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
//            GoonjPlayerManager.suspend()
//        }
//    }

    inner class Binder : android.os.Binder() {
        val goonjPlayerServiceInterface: GoonjPlayerServiceInterface
            get() = this@GoonjService
    }

    override fun onCreate() {
        super.onCreate()
        GoonjPlayerManager.onStart()

//        mediaRouter.addCallback(selector, mediaRouterCallback,
//            MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)

    }

    override fun onDestroy() {
        GoonjPlayerManager.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}