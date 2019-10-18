package ai.rever.goonj.manager

import ai.rever.goonj.Goonj
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.download.GoonjDownloadManager
import ai.rever.goonj.player.imp.RemoteAudioPlayer
import ai.rever.goonj.player.AudioPlayer
import ai.rever.goonj.player.imp.LocalAudioPlayer
import ai.rever.goonj.models.Track
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*

internal object GoonjPlayerManager {

    private var mIsRemote = false
    private var isRemote
        get() = mIsRemote
        set(value) {
            if (mIsRemote != value) {
                if (value) {
                    remoteAudioPlayer?.unsuspend()
                    localAudioPlayer?.suspend()
                } else {
                    localAudioPlayer?.unsuspend()
                    remoteAudioPlayer?.suspend()
                }
            }
            mIsRemote = value
        }

    private val player: AudioPlayer? get() {
//        isRemote = mediaRoute?.supportsControlCategory(
//            MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)?: false
        return if (isRemote) remoteAudioPlayer else localAudioPlayer
    }


    private var mTrackList: MutableList<Track> = mutableListOf()

    internal var lastCompletedTrack: Track? = null

    private var remoteAudioPlayer: AudioPlayer? = null
    private var localAudioPlayer: AudioPlayer? = null

//    private val mediaRoute get() = appContext?.let { MediaRouter.getInstance(it).selectedRoute }

    val trackList: List<Track> get() = mTrackList

    val trackPosition get() = player?.getTrackPosition()?: 0

    val playerStateSubject: BehaviorSubject<GoonjPlayerState> = BehaviorSubject.createDefault(GoonjPlayerState.IDLE)

    val autoplayTrackSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val currentTrackSubject: BehaviorSubject<Track> = BehaviorSubject.create()

    val trackListSubject: BehaviorSubject<List<Track>> = BehaviorSubject.createDefault(mTrackList)

    val trackCompleteSubject = PublishSubject.create<Track>()

    private val compositeDisposable = CompositeDisposable()

    val subscribe get() = object: Disposable {

        init {
            remoteAudioPlayer = RemoteAudioPlayer().apply { addTo(compositeDisposable) }

            localAudioPlayer = LocalAudioPlayer().apply { addTo(compositeDisposable) }

            TrackPreFetcherManager.subscribe.addTo(compositeDisposable)

            GoonjDownloadManager.subscribe.addTo(compositeDisposable)
        }


        override fun isDisposed(): Boolean {
            return compositeDisposable.isDisposed
        }

        override fun dispose() {
            compositeDisposable.dispose()
        }
    }

    fun addTrack(track: Track, index: Int = trackList.size) {
        track.state.index = index
        track.state.addedAt = Date()
        mTrackList.add(index, track)
        player?.enqueue(track, index)
        trackListSubject.onNext(mTrackList)
    }

    fun pause() = player?.pause()

    fun resume() = player?.resume()

    fun seekTo(positionMS: Long) = player?.seekTo(positionMS)

    fun startNewSession() {
        mTrackList.clear()
        player?.startNewSession()
    }

    fun removeTrack(index : Int){
        mTrackList.removeAt(index)
        player?.remove(index)
    }

    fun moveTrack(currentIndex: Int, finalIndex: Int){
        val currentTrack = mTrackList[currentIndex]

        mTrackList.removeAt(currentIndex)
        mTrackList.add(finalIndex - 1, currentTrack)

        player?.moveTrack(currentIndex, finalIndex)
    }

    fun skipToNext() = player?.skipToNext()

    fun skipToPrevious() = player?.skipToPrevious()

    fun removeNotification() {
        player?.onRemoveNotification()
        Goonj.stopForeground(true)
    }

    fun finishTrack(){
        pause()
        removeNotification()
        onTrackComplete(currentTrackSubject.value ?: return)
    }

    fun onTrackComplete(track: Track) {
        track.state.completedAt = Date()
        lastCompletedTrack = track
        trackCompleteSubject.onNext(track)
    }
}

