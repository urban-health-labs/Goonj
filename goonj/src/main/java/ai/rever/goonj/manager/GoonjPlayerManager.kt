package ai.rever.goonj.manager

import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.player.RemoteAudioPlayer
import ai.rever.goonj.interfaces.AudioPlayer
import ai.rever.goonj.player.LocalAudioPlayer
import ai.rever.goonj.models.Track
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


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

    val playerStateBehaviorSubject: BehaviorSubject<GoonjPlayerState> = BehaviorSubject.create()

    val currentTrackSubject: BehaviorSubject<Track> = BehaviorSubject.create()

    val autoplayTrackSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val trackCompleteSubject = PublishSubject.create<Track>()

    private val player: AudioPlayer? get() {
//        isRemote = mediaRoute?.supportsControlCategory(
//            MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)?: false
        return if (isRemote) remoteAudioPlayer else localAudioPlayer
    }


    private var mTrackList: MutableList<Track> = mutableListOf()

    private var remoteAudioPlayer: AudioPlayer? = null
    private var localAudioPlayer: AudioPlayer? = null

//    private val mediaRoute get() = appContext?.let { MediaRouter.getInstance(it).selectedRoute }

    val trackList: List<Track> get() = mTrackList
    val trackPosition get() = player?.getTrackPosition()?: 0

    fun onStart() {
        this.remoteAudioPlayer = RemoteAudioPlayer()
        this.localAudioPlayer = LocalAudioPlayer()
    }

    fun addTrack(track: Track, index: Int = mTrackList.size) {
        track.trackState.index = index
        mTrackList.add(index, track)
        player?.enqueue(track, index)
    }

    fun pause() = player?.pause()

    fun resume() = player?.resume()

    fun seekTo(positionMS: Long) = player?.seekTo(positionMS)

    fun startNewSession(){
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

    fun release() {
        remoteAudioPlayer?.release()
        localAudioPlayer?.release()
    }

    fun removeNotification() {
        player?.onRemoveNotification()
    }

    fun finishTrack(){
        pause()
        removeNotification()
        onTrackComplete(currentTrackSubject.value ?: return)
    }

    fun onTrackComplete(track: Track) {
        trackCompleteSubject.onNext(track)
    }

}
