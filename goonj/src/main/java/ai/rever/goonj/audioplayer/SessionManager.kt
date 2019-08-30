package ai.rever.goonj.audioplayer

import ai.rever.goonj.audioplayer.interfaces.AudioPlayer
import ai.rever.goonj.audioplayer.models.Samples
import android.content.Intent
import androidx.mediarouter.media.MediaItemStatus
import java.util.ArrayList

class SessionManager(private val mName: String) : AudioPlayer.Callback {
    private var mSessionId: Int = 0
    var mPaused: Boolean = false
    private var mSessionValid: Boolean = false
    private var mPlayer: AudioPlayer? = null
    private var mCallback: Callback? = null
    private var mPlaylist: MutableList<Samples.Track> = ArrayList()
    var isRemote: Boolean = false

    val sessionId: String?
        get() = if (mSessionValid) mSessionId.toString() else null

    var currentItem: Samples.Track? = if (mPlaylist.isEmpty()) null else mPlaylist[0]

    val getSession : List<Samples.Track>
        get() = mPlaylist


    // Returns the cached playlist (note this is not responsible for updating it)
    val playlist: List<Samples.Track>
        get() = mPlaylist

    fun setRemotePlayerSelected(isRemote: Boolean){
        this.isRemote = isRemote
    }

    fun setPlayer(player: AudioPlayer) {
        mPlayer = player
        checkPlayer()
        mPlayer?.setCallback(this)
    }

    private fun checkPlayer() {
        if (mPlayer == null) {
            throw IllegalStateException("Player not set!")
        }
    }

    fun hasSession(): Boolean {
        return mSessionValid
    }

    fun add(item: Samples.Track, index: Int ?= -1) {

        index?.let {
            if(it >= 0 && it < mPlaylist.size){
                item.index = index
                mPlaylist.add(it, item)

                if (mPlayer?.isQueuingSupported() == true) {
                    mPlayer?.enqueue(item, it)
                } else {
                    playItemOnRemotePlayer()
                    mPaused = false
                }
            } else {
                item.index = mPlaylist.size
                mPlaylist.add(item)

                if (mPlayer?.isQueuingSupported() == true) {
                    mPlayer?.enqueue(item)
                } else {
                    playItemOnRemotePlayer()
                    mPaused = false
                }
            }
        }
    }

    fun pause() {
        mPaused = true
        mPlayer?.pause()
    }

    fun resume() {
        mPaused = false
        mPlayer?.resume()

    }

    fun stop(){
        mPlayer?.stop()
    }

    fun seek(positionMs : Long){
        if(isRemote){

        }
        mPlayer?.seekTo(positionMs)
    }

    fun startNewSession(){
        mPlaylist.clear()
        mPlayer?.startNewSession()
    }

    fun getTrackPosition(): Long?{
        return mPlayer?.getTrackPosition()
    }

    fun suspend(){
        mPlayer?.pause()
    }

    fun unsuspend(){
        if(mPlayer?.isQueuingSupported() == true) {
            for (item in mPlaylist) {
                mPlayer?.enqueue(item)
            }
        } else {
            if(isRemote) {
                // play an item from playlist that isn't played
                playItemOnRemotePlayer()
            }
        }
    }

    fun customiseNotification(useNavigationAction: Boolean ,
                              usePlayPauseAction: Boolean ,
                              fastForwardIncrementMs: Long ,
                              rewindIncrementMs: Long ){

        mPlayer?.customiseNotification(useNavigationAction,usePlayPauseAction,
            fastForwardIncrementMs, rewindIncrementMs)
    }

    fun setAutoplay(autoplay : Boolean){
        mPlayer?.setAutoplay(autoplay)
    }

    fun setPendingActivityForNotification(intent : Intent){
        mPlayer?.setPendingActivityForNotification(intent)
    }

    fun removeTrack(index : Int){
        mPlaylist.removeAt(index)
        mPlayer?.remove(index)
    }

    fun moveTrack(currentIndex: Int, finalIndex: Int){
        val currentTrack = mPlaylist[currentIndex]
        mPlaylist.removeAt(currentIndex)
        mPlaylist.add(finalIndex-1,currentTrack)

        mPlayer?.moveTrack(currentIndex, finalIndex)
    }

    fun skipToNext(){
        mPlayer?.skipToNext()
    }

    fun skipToPrevious(){
        mPlayer?.skipToPrevious()
    }

    private fun playItemOnRemotePlayer(){
        currentItem = playlist.first {
            it.state != MediaItemStatus.PLAYBACK_STATE_FINISHED
        }
        currentItem?.state = MediaItemStatus.PLAYBACK_STATE_PLAYING
        currentItem?.let {
            mPlayer?.play(it)
            //mPlayer?.enqueue(it)
        }
    }

    fun release(){
        mPlayer?.release()
    }

    // Player.Callback
    override fun onError() {
    }


    override fun onCompletion() {
        currentItem?.state = MediaItemStatus.PLAYBACK_STATE_FINISHED
        playItemOnRemotePlayer()
    }

    override fun onPlaylistChanged() {
        // Playlist has changed, update the cached playlist
    }

    override fun onPlaylistReady() {
        // Notify activity to update Ui
        mCallback?.onStatusChanged()
    }

    // provide a callback interface to tell the UI when significant state changes occur
    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    override fun toString(): String {
        var result = "Media Queue: "
        if (mPlaylist.isNotEmpty()) {
            for (item in mPlaylist) {
                result += "\n" + item.toString()
            }
        } else {
            result += "<empty>"
        }
        return result
    }

    interface Callback {
        fun onStatusChanged()
        fun onItemChanged(item: Samples.Track)
    }
}
