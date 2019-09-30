package ai.rever.goonjexample

import ai.rever.goonj.Goonj
import ai.rever.goonj.models.Track
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class GoonjExampleApp: Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onAppCreate() {
        val pendingIntent = Intent(applicationContext, AudioPlayerActivity::class.java)

        Goonj.initialize(this)
            .setPendingIntentForNotification(pendingIntent)
            .addOnTrackComplete(::onTrackComplete)
            .setImageLoader(::imageLoader)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroy() {
        Log.e("=============>", "destroy")

        Goonj.unregister()
    }

    private fun imageLoader(track: Track, callback: (Bitmap?) -> Unit) {
        Glide.with(this).asBitmap().load(track.imageUrl)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap,
                                             transition: Transition<in Bitmap>?) {
                    callback(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    callback(placeholder?.toBitmap())
                }
            })

    }

    private fun onTrackComplete(track: Track) {

    }
}

