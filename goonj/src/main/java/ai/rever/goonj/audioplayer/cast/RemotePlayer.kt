package ai.rever.goonj.audioplayer.cast

import ai.rever.goonj.BuildConfig
import ai.rever.goonj.audioplayer.analytics.*
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.mediarouter.media.MediaItemStatus
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaSessionStatus
import androidx.mediarouter.media.RemotePlaybackClient
import ai.rever.goonj.audioplayer.interfaces.AudioPlayer
import ai.rever.goonj.audioplayer.models.Track
import ai.rever.goonj.audioplayer.service.AudioPlayerService
import ai.rever.goonj.audioplayer.util.SingletonHolder
import android.net.Uri
import android.os.Handler
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastContext
import java.lang.ref.WeakReference

class RemotePlayer constructor (var contextWeakReference: WeakReference<Context>) : AudioPlayer() {

    companion object : SingletonHolder<RemotePlayer, WeakReference<Context>>(::RemotePlayer)

    private var TAG = "REMOTE_PLAYER"
    private var DEBUG = BuildConfig.DEBUG
    private var mRoute: MediaRouter.RouteInfo? = null

    private var mClient: RemotePlaybackClient? = null
    private var client: RemotePlaybackClient?
        get() {
            return mClient
        }
        set(value) {
            Log.e(TAG, "========> mClient get")
            value?.setStatusCallback(mStatusCallback)
            mClient = value
        }
    private val mContext: Context? get() = contextWeakReference.get()
    private val mIsPlaying: MutableLiveData<Boolean>? get() = (mContext as? AudioPlayerService)?.mIsPlaying
    private val mCurrentPlayingTrack : MutableLiveData<Track>? get() = (mContext as? AudioPlayerService)?.mCurrentPlayingTrack

    private var mAutoplay : Boolean = true
    private var mIsHandlerRunning = false

    override fun isRemotePlayback(): Boolean {
        return true
    }

    override fun connect(route: MediaRouter.RouteInfo?) {
        mRoute = route
        client = RemotePlaybackClient(mContext, route)

        if (DEBUG) {
            Log.d(
                TAG, "connected to: " + route
                        + ", isRemotePlaybackSupported: " + client?.isRemotePlaybackSupported
                        + ", isQueuingSupported: " + client?.isQueuingSupported
            )
        }
    }

    override fun release() {
        client?.release()
    }

    override fun startNewSession() {
        Log.d(TAG,"Start new getSession")
    }

    override fun play(item: Track) {
        client?.play(item.url.toUri(), "audio/*", null, 0, null, object : RemotePlaybackClient.ItemActionCallback() {
            override fun onResult(
                data: Bundle?,
                sessionId: String?,
                sessionStatus: MediaSessionStatus?,
                itemId: String?,
                itemStatus: MediaItemStatus?
            ) {
                logStatus("play: succeeded", sessionId, sessionStatus, itemId, itemStatus)
                Log.d(TAG,"=======> handler started")
                if(!mIsHandlerRunning) {
                    statusHandler()
                }
                item.remoteItemId = itemId
                Log.e(TAG,"===========> Set remoteID: $itemId")
                if (item.position > 0) {
                    seekInternal(item)
                }
                if (item.state == MediaItemStatus.PLAYBACK_STATE_PAUSED) {
                    pause()
                }

                setMediaMetadata(item)
                mCallback.onPlaylistChanged()
                mCurrentPlayingTrack?.value = item
                mIsPlaying?.value = true
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("play: failed", error , code)
            }
        })
    }

    private fun setMediaMetadata(item: Track){
        val musicMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)

        musicMetadata.putString(MediaMetadata.KEY_TITLE, item.title)
        musicMetadata.putString(MediaMetadata.KEY_ARTIST, item.artist)
        musicMetadata.addImage(WebImage(Uri.parse(item.albumArtUrl)))

        mContext?.let{
            val castSession = CastContext.getSharedInstance(it).sessionManager.currentCastSession
            try {
                val mediaInfo = MediaInfo.Builder(item.url)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("audio/*")
                    .setMetadata(musicMetadata)
                    .build()
                val remoteMediaClient = castSession.remoteMediaClient

                val mediaLoadRequestData = MediaLoadRequestData.Builder()
                    .setMediaInfo(mediaInfo)
                    .setAutoplay(true)
                    .build()

                remoteMediaClient.load(mediaLoadRequestData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun seekTo(positionMs: Long) {
        mCurrentPlayingTrack?.value?.let {
            getStatus(it, true, positionMs)
        }
    }

    override fun getStatus(item: Track, seek: Boolean, positionMs: Long) {
        if (client?.hasSession() != true || item.remoteItemId == null) {
            // if getSession is not valid or item id not assigend yet.
            // just return, it's not fatal
            return
        }

        Log.e(TAG,"RemoteID: ${item.remoteItemId}")
        client?.getStatus(item.remoteItemId, null, object : RemotePlaybackClient.ItemActionCallback() {
            override fun onResult(
                data: Bundle?,
                sessionId: String?,
                sessionStatus: MediaSessionStatus?,
                itemId: String?,
                itemStatus: MediaItemStatus?
            ) {
                logStatus("============> getStatus: succeeded", sessionId, sessionStatus, itemId, itemStatus)
                val state = itemStatus?.playbackState
                if (state == MediaItemStatus.PLAYBACK_STATE_PLAYING
                    || state == MediaItemStatus.PLAYBACK_STATE_PAUSED
                    || state == MediaItemStatus.PLAYBACK_STATE_PENDING
                ) {
                    item.state = state
                    item.position = itemStatus.contentPosition
                    item.duration = itemStatus.contentDuration
                    item.timestamp = itemStatus.timestamp
                }
                if (seek) {
                    if((item.position + positionMs) < 0 ) {
                        item.position = 0
                    }
                    else if((item.position + positionMs) < item.duration ) {
                        item.position = item.position + positionMs
                    } else {
                        item.position = item.duration - 1000
                    }
                    seekInternal(item)
                }
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("getStatus: failed", error, code)
//                if (seek) {
//                    mCallback.onPlaylistReady()
//                }
            }
        })
    }
    private val mStatusCallback = object : RemotePlaybackClient.StatusCallback() {
        override fun onItemStatusChanged(
            data: Bundle?,
            sessionId: String?,
            sessionStatus: MediaSessionStatus?,
            itemId: String?,
            itemStatus: MediaItemStatus?
        ) {
            if (itemStatus?.playbackState == MediaItemStatus.PLAYBACK_STATE_FINISHED) {
                mCallback.onCompletion()
            } else if (itemStatus?.playbackState == MediaItemStatus.PLAYBACK_STATE_ERROR) {
                mCallback.onError()
            }

            if(itemStatus?.playbackState == MediaItemStatus.PLAYBACK_STATE_PAUSED){
                setPlayerStateRemote(false)
            } else if(itemStatus?.playbackState == MediaItemStatus.PLAYBACK_STATE_PLAYING){
                setPlayerStateRemote(true)
            }
        }

        override fun onSessionStatusChanged(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
            logStatus("onSessionStatusChanged", sessionId, sessionStatus, null, null)
            mCallback.onPlaylistChanged()
        }

        override fun onSessionChanged(sessionId: String?) {
            if (DEBUG) {
                Log.d(TAG, "onSessionChanged: sessionId=$sessionId")
            }
        }
    }

    override fun pause() {
        if (client?.hasSession() != true) {
            // ignore if no getSession
            return
        }
        client?.pause(null, object : RemotePlaybackClient.SessionActionCallback() {
            override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {

                logStatus("pause: succeeded", sessionId, sessionStatus, null, null)
                mCallback.onPlaylistChanged()

                mIsPlaying?.value = false

            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("pause: failed", error, code)
            }
        })
    }

    override fun resume() {
        if (client?.hasSession() != true) {
            // ignore if no getSession
            return
        }
        client?.resume(null, object : RemotePlaybackClient.SessionActionCallback() {
            override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
                logStatus("resume: succeeded", sessionId, sessionStatus, null, null)
                if(!mIsHandlerRunning) {
                    statusHandler()
                }
                mCallback.onPlaylistChanged()

                mIsPlaying?.value = true
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("resume: failed", error, code)
            }
        })
    }

    override fun stop() {
        if (client?.hasSession() != true) {
            // ignore if no getSession
            return
        }
        client?.stop(null, object : RemotePlaybackClient.SessionActionCallback() {
            override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
                logStatus("stop: succeeded", sessionId, sessionStatus, null, null)
                mCallback.onPlaylistChanged()
                mIsPlaying?.value = false
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("stop: failed", error, code)
            }
        })
    }


    override fun setPlaylist(playlist: List<Track>) {}

    override fun setVolume(volume: Float) {}

    override fun getTrackPosition(): Long? {
        return null
    }

    override fun setAutoplay(autoplay: Boolean) {
        mAutoplay = autoplay

        // TODO : Re-create RemotePlayer with playlist support
    }

    private fun seekInternal(item: Track) {
        if (client?.hasSession() != true) {
            // ignore if no getSession
            return
        }
        client?.seek(item.remoteItemId, item.position, null, object : RemotePlaybackClient.ItemActionCallback() {
            override fun onResult(
                data: Bundle?,
                sessionId: String?,
                sessionStatus: MediaSessionStatus?,
                itemId: String?,
                itemStatus: MediaItemStatus?
            ) {
                logStatus("seek: succeeded", sessionId, sessionStatus, itemId, itemStatus)
                mCallback.onPlaylistChanged()
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("seek: failed", error, code)
            }
        })
    }

    private fun logStatus(
        message: String,
        sessionId: String?, sessionStatus: MediaSessionStatus?,
        itemId: String?, itemStatus: MediaItemStatus?
    ) {
        if (DEBUG) {
            var result = ""
            if (sessionId != null && sessionStatus != null) {
                result += "sessionId=$sessionId, sessionStatus=$sessionStatus"
            }
            if ((itemId != null) and (itemStatus != null)) {
                result += ((if (result.isEmpty()) "" else ", ")
                        + "itemId=" + itemId + ", itemStatus=" + itemStatus)
            }
            Log.d(TAG, "$message: $result")
        }

        val map = mutableMapOf(MESSAGE to message, SESSION_ID to sessionId, SESSION_STATUS to sessionStatus,
            ITEM_ID to itemId, ITEM_STATUS to itemStatus)
        logEventBehaviour(true, PlayerAnalyticsEnum.REMOTE_LOG_STATUS, map)

        updateCurrentTrack(itemStatus)
    }

    private fun logError(message: String?, error: String?, code: Int) {
        val map = mutableMapOf(MESSAGE to message, ERROR_REMOTE to error, ERROR_REMOTE_CODE to code)
        logEventBehaviour(true, PlayerAnalyticsEnum.REMOTE_LOG_ERROR, map)
    }

    private fun setPlayerStateRemote(isRemotePlaying : Boolean){
        val map = mutableMapOf(IS_REMOTE_PLAYING to isRemotePlaying)
        logEventBehaviour(true, PlayerAnalyticsEnum.SET_PLAYER_STATE_REMOTE, map)
        mIsPlaying?.value = isRemotePlaying
    }

    private fun statusHandler(){
        val handler = Handler()
        handler.postDelayed(object: Runnable{
            override fun run() {
                getStatus(mCurrentPlayingTrack?.value!!,false,0)
                if(mIsPlaying?.value == true && isRemotePlayback()) {
                    mIsHandlerRunning  = true
                    handler.postDelayed(this, 1000)
                } else {
                    mIsHandlerRunning = false
                }
            }

        },1000)
    }

    private fun updateCurrentTrack(itemStatus: MediaItemStatus?){
        val currentPlayingTrack = mCurrentPlayingTrack?.value
        currentPlayingTrack?.let { track ->
            itemStatus?.contentPosition?.let {
                track.position = itemStatus.contentPosition
                mCurrentPlayingTrack?.value = track
            }
        }
    }
}