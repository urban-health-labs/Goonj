package ai.rever.goonj.audioplayer

import android.util.Log
import ai.rever.goonj.audioplayer.interfaces.AudioPlayer
import ai.rever.goonj.audioplayer.models.Samples
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
        Log.d(TAG,"isRemote: $isRemote")
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

    fun add(item: Samples.Track) {

        item.index = mPlaylist.size
        mPlaylist.add(item)

        // if player supports queuing, enqueue the item now
        if (mPlayer?.isQueuingSupported() == true) {
            mPlayer?.enqueue(item)
        } else {
            playItemOnRemotePlayer()
            mPaused = false
        }
    }

    fun pause() {
        Log.d(TAG,"pause")
        mPaused = true
        mPlayer?.pause()
    }

    fun resume() {
        Log.d(TAG,"resume")
        mPaused = false
            mPlayer?.resume()

    }

    fun seek(positionMs : Long){
        if(isRemote){

        }
        mPlayer?.seekTo(positionMs)
    }

    fun addItemToPlaylist(track: Samples.Track){
        mPlaylist.add(track)
    }

    fun setPlaylistToPlayer(){
        mPlayer?.setPlaylist(playlist)
    }

    fun setVolume(volume : Float){
        mPlayer?.setVolume(volume)
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

    private fun playItemOnRemotePlayer(){
        currentItem = playlist.first {
            Log.d(TAG,"STATUS: ${it.state}")
            it.state != MediaItemStatus.PLAYBACK_STATE_FINISHED
        }
        currentItem?.state = MediaItemStatus.PLAYBACK_STATE_PLAYING
        currentItem?.let {
            mPlayer?.play(it)
            //mPlayer?.enqueue(it)
        }
    }

    // Player.Callback
    override fun onError() {
        Log.e(TAG,"===============> Error")
    }


    override fun onCompletion() {
        Log.e(TAG,"==============> Finished: ${currentItem?.title}")
        currentItem?.state = MediaItemStatus.PLAYBACK_STATE_FINISHED
        playItemOnRemotePlayer ()
        //mPlayer?.play(SAMPLES[3])
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

    companion object {
        private val TAG = "SessionManager"
        private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)
    }
}
