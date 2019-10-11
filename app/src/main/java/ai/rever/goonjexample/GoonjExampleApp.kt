package ai.rever.goonjexample

import ai.rever.goonj.Goonj
import ai.rever.goonj.models.SAMPLES
import ai.rever.goonj.models.Track
import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins

class GoonjExampleApp: Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private lateinit var disposable: Disposable

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onAppCreate() {

        RxJavaPlugins.setErrorHandler {
            Log.e("============>", it.localizedMessage ?: "empty")
        }

        Goonj.register<AudioPlayerActivity>(this)

        Goonj.imageLoader = ::imageLoader

        Goonj.trackPreFetcher = ::trackFetcher

        Goonj.preFetchDistanceWithAutoplay = 2
        Goonj.preFetchDistanceWithoutAutoplay = 1

        disposable = Goonj.trackCompletionObservable.subscribe(::onTrackComplete)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroy() {
        Log.e("=============>", "destroy")

        Goonj.unregister()
        disposable.dispose()
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

    private fun trackFetcher(callback: (List<Track>) -> Unit) {
        val fetched = arrayListOf<Track>()
        var nextIndex = Goonj.trackList.size - 1
        if (Goonj.autoplay) {
            fetched.add(SAMPLES[++nextIndex % SAMPLES.size])
            fetched.add(SAMPLES[++nextIndex % SAMPLES.size])
            fetched.add(SAMPLES[++nextIndex % SAMPLES.size])
        } else {
            fetched.add(SAMPLES[++nextIndex % SAMPLES.size])
            fetched.add(SAMPLES[++nextIndex % SAMPLES.size])
        }
        callback(fetched)
    }

    private fun onTrackComplete(track: Track) {
        /**
         * Could be used for
         *   i)   updating completed track to server or some database
         *   ii)  adding additional track to playlist after calculating left track count to play
         */
//        Log.e("===========>", track.title)
    }
}

