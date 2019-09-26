package ai.rever.goonj.manager

import ai.rever.goonj.Goonj
import ai.rever.goonj.player.RemoteAudioPlayer
import ai.rever.goonj.interfaces.AudioPlayer
import ai.rever.goonj.player.LocalAudioPlayer
import ai.rever.goonj.models.Track
import androidx.lifecycle.MutableLiveData
import androidx.mediarouter.media.MediaItemStatus
import io.reactivex.subjects.BehaviorSubject


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

    internal val isPlayingBehaviorSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()
    internal val currentPlayingTrack: BehaviorSubject<Track> = BehaviorSubject.create()


    private val player: AudioPlayer? get() {
//        isRemote = mediaRoute?.supportsControlCategory(
//            MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)?: false
        return if (isRemote) remoteAudioPlayer else localAudioPlayer
    }


    private var mTrackList: MutableList<Track> = mutableListOf()

    private var remoteAudioPlayer: AudioPlayer? = null
    private var localAudioPlayer: AudioPlayer? = null

//    private val mediaRoute get() = appContext?.let { MediaRouter.getInstance(it).selectedRoute }

    internal val trackList: List<Track> get() = mTrackList
    internal val trackPosition get() = player?.getTrackPosition()?: 0

    internal fun onStart() {
        this.remoteAudioPlayer = RemoteAudioPlayer()
        this.localAudioPlayer = LocalAudioPlayer()
    }

    internal fun addTrack(track: Track, index: Int = mTrackList.size) {
        track.state.index = index
        mTrackList.add(index, track)

        player?.enqueue(track, index)
    }

    internal fun pause() = player?.pause()

    internal fun resume() = player?.resume()

    internal fun seekTo(positionMS: Long) = player?.seekTo(positionMS)

    internal fun startNewSession(){
        mTrackList.clear()
        player?.startNewSession()
    }

    internal fun setAutoplay(autoplay : Boolean) = player?.setAutoplay(autoplay)

    internal fun removeTrack(index : Int){
        mTrackList.removeAt(index)
        player?.remove(index)
    }

    internal fun moveTrack(currentIndex: Int, finalIndex: Int){
        val currentTrack = mTrackList[currentIndex]

        mTrackList.removeAt(currentIndex)
        mTrackList.add(finalIndex - 1, currentTrack)

        player?.moveTrack(currentIndex, finalIndex)
    }

    internal fun skipToNext() = player?.skipToNext()

    internal fun skipToPrevious() = player?.skipToPrevious()

    internal fun release() {
        remoteAudioPlayer?.release()
        localAudioPlayer?.release()
    }

    internal fun removeNotification() {
        player?.onRemoveNotification()
    }

    internal fun addOnTrackComplete(trackCompletion: (Track) -> Unit) {
        trackCompletions.add(trackCompletion)
    }

    internal fun removeOnTrackComplete(trackCompletion: (Track) -> Unit) {
        trackCompletions.remove(trackCompletion)
    }

    internal fun finishTrack(){
        pause()
        removeNotification()
        onTrackComplete(currentPlayingTrack.value ?: return)
    }

    internal fun onTrackComplete(track: Track) {
        trackCompletions.forEach { it(track) }
    }

    private var trackCompletions: ArrayList<(Track) -> Unit> = arrayListOf()
}

