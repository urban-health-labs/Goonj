package ai.rever.goonj.audioplayer.service

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

    playbackObserver = object : io.reactivex.Observer<Long> {
        override fun onNext(position: Long) {
            val currentPlayingTrack = currentPlayingTrack.value
            currentPlayingTrack?.let {
                it.position = position
                mCurrentPlayingTrack.value = it
            }
        }

        override fun onSubscribe(d: Disposable) {}

        override fun onError(e: Throwable) {}

        override fun onComplete() {}
    }

    playbackObservable.subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(playbackObserver)
}

fun AudioPlayerService.removeProgressObserver() {
    playbackObservable.unsubscribeOn(AndroidSchedulers.mainThread())
}