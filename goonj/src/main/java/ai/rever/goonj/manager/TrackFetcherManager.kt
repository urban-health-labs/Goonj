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

    var trackFetcher: ((List<Track>, (List<Track>)->Unit)->Unit)? = null

    var prefetchDistanceWithAutoplay = 5
    var prefetchDistanceWithoutAutoplay = 2


    internal fun onStart() {
        disposable = Goonj.currentTrackFlowable.subscribe { track ->
            if (lastKnownTrack.id != track.id) {
                lastKnownTrack = track
                isFetched = false
            }

            if (!isFetching && !isFetched) {
                if (Goonj.playerState == GoonjPlayerState.PLAYING && (track.state.progress * 100).toInt() > 50) {
                    if (Goonj.autoplay) {
                        if ((Goonj.trackList.size - (track.state.index + 1)) > prefetchDistanceWithAutoplay) {
                            onFetch()
                        }
                    } else {
                        if ((Goonj.trackList.size - (track.state.index + 1)) > prefetchDistanceWithoutAutoplay) {
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
        if (trackFetcher != null && !isFetching) {
            isFetching = true
            trackFetcher?.invoke(Goonj.trackList) { newTracks ->
                if (newTracks.isEmpty()) {
                    isFetched = true
                } else {
                    newTracks.forEach { Goonj.addTrack(it) }
                }
            }
        }
    }
}