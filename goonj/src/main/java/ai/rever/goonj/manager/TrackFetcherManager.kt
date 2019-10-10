package ai.rever.goonj.manager

import ai.rever.goonj.Goonj
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.models.Track
import io.reactivex.disposables.Disposable

internal object TrackFetcherManager {
    private var isFetched = false
    private var isFetching = false
    private var disposable: Disposable? = null

    private var lastKnownTrack = Track()

    var trackPreFetcher: (((List<Track>)->Unit)->Unit)? = null

    var tryPrefetchAtProgress = 0.5
    var preFetchDistanceWithAutoplay = 5
    var preFetchDistanceWithoutAutoplay = 2

    internal fun onStart() {
        disposable = Goonj.currentTrackFlowable.subscribe { track ->
            if (lastKnownTrack.id != track.id) {
                lastKnownTrack = track
                isFetched = false
            }

            if (!isFetching && !isFetched && trackPreFetcher != null) {
                if (track.state.progress > tryPrefetchAtProgress) {
                    if (Goonj.autoplay) {
                        if ((Goonj.trackList.size - (track.state.index + 1)) < preFetchDistanceWithAutoplay) {
                            onFetch()
                        }
                    } else {
                        if ((Goonj.trackList.size - (track.state.index + 1)) < preFetchDistanceWithoutAutoplay) {
                            onFetch()
                        }
                    }
                }
            }
        }
    }

    internal fun release() {
        disposable?.dispose()
    }

    private fun onFetch() {
        trackPreFetcher?.apply {
            isFetching = true
            invoke { nextList ->
                isFetching = false
                if (nextList.isEmpty()) {
                    isFetched = true
                } else {
                    nextList.forEach { Goonj.addTrack(it) }
                }
            }
        }
    }
}