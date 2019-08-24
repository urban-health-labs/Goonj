package ai.rever.goonj.audioplayer.service

import android.util.Log
import ai.rever.goonj.audioplayer.util.mIsPlaying
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

fun AudioPlayerService.setupProgressObserver() {
    playbackObservable = Observable.interval(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .takeWhile { mIsPlaying.value?: false && mSessionManager.getTrackPosition()!= null }
        .map { mSessionManager.getTrackPosition() }
    addProgressObserver()
}

fun AudioPlayerService.addProgressObserver() {
    val TAG = "PROGRESS"

    playbackObserver = object : io.reactivex.Observer<Long> {
        override fun onNext(position: Long) {
            // TODO use the position for analytics
            Log.d(TAG, "Position: $position")
        }

        override fun onSubscribe(d: Disposable) {
            Log.e(TAG, "onSubscribe: ")
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "onError: ")
        }

        override fun onComplete() {
            Log.e(TAG, "onComplete: All Done!")
        }
    }

    playbackObservable.subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(playbackObserver)
}

fun AudioPlayerService.removeObserver() {
    playbackObservable.unsubscribeOn(AndroidSchedulers.mainThread())
}