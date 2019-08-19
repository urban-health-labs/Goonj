package ai.rever.goonj.audioplayer.cast

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.mediarouter.media.MediaItemStatus
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaSessionStatus
import androidx.mediarouter.media.RemotePlaybackClient
import ai.rever.goonj.audioplayer.interfaces.AudioPlayer
import ai.rever.goonj.audioplayer.util.Samples
import ai.rever.goonj.audioplayer.util.isPlaying
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.gms.cast.MediaMetadata
import java.util.ArrayList
import com.google.android.gms.common.images.WebImage
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import android.R.attr.duration
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.framework.CastContext


class RemotePlayer(var mContext: Context) : AudioPlayer() {

    private var TAG = "REMOTE_PLAYER"
    private var DEBUG = true
    private var mRoute: MediaRouter.RouteInfo? = null
    private var mEnqueuePending: Boolean = false
    private var mStatsInfo = ""
    private val mTempQueue = ArrayList<Samples.Sample>()

    private var mClient: RemotePlaybackClient? = null

    override fun isRemotePlayback(): Boolean {
        return true
    }

    override fun isQueuingSupported(): Boolean {
        return mClient?.isQueuingSupported ?: false
    }

    override fun connect(route: MediaRouter.RouteInfo?) {
        mRoute = route
        mClient = RemotePlaybackClient(mContext, route)
        mClient?.setStatusCallback(mStatusCallback)

        if (DEBUG) {
            Log.d(
                TAG, "connected to: " + route
                        + ", isRemotePlaybackSupported: " + mClient?.isRemotePlaybackSupported
                        + ", isQueuingSupported: " + mClient?.isQueuingSupported
            )
        }
    }

    override fun release() {
        mClient?.release()

        if (DEBUG) {
            Log.d(TAG, "released.")
        }
    }

    override fun startNewSession() {
        Log.d(TAG,"Start new session")
    }

    override fun play(item: Samples.Sample) {
        if (DEBUG) {
            Log.d(TAG, "play: item=$item")
        }
        mClient?.play(item.url.toUri(), "audio/mp3", null, 0, null, object : RemotePlaybackClient.ItemActionCallback() {
            override fun onResult(
                data: Bundle?,
                sessionId: String?,
                sessionStatus: MediaSessionStatus?,
                itemId: String?,
                itemStatus: MediaItemStatus?
            ) {
                logStatus("play: succeeded", sessionId, sessionStatus, itemId, itemStatus)
                item.remoteItemId = itemId
                if (item.position > 0) {
                    seekInternal(item)
                }
                if (item.state == MediaItemStatus.PLAYBACK_STATE_PAUSED) {
                    pause()
                }

                setMediaMetadata(item)
                mCallback.onPlaylistChanged()
                isPlaying.value = true
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("play: failed", error , code)
            }
        })
    }

    fun setMediaMetadata(item: Samples.Sample){
        val musicMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)

        musicMetadata.putString(MediaMetadata.KEY_TITLE, item.title)
        musicMetadata.putString(MediaMetadata.KEY_ARTIST, item.description)
        musicMetadata.addImage(WebImage(Uri.parse(item.albumArtUrl)))

        val castSession = CastContext.getSharedInstance(mContext).sessionManager.currentCastSession

        try {
            val mediaInfo = MediaInfo.Builder(item.url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("audio/*")
                .setMetadata(musicMetadata)
                .build()
            val remoteMediaClient = castSession.remoteMediaClient
            remoteMediaClient.load(mediaInfo, true, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun seek(item: Samples.Sample) {
        seekInternal(item)
    }

    override fun getStatus(item: Samples.Sample, update: Boolean) {

        if (mClient?.hasSession() != true || item.remoteItemId == null) {
            // if session is not valid or item id not assigend yet.
            // just return, it's not fatal
            return
        }

        if (DEBUG) {
            Log.d(TAG, "getStatus: item=$item, update=$update")
        }
        mClient?.getStatus(item.remoteItemId, null, object : RemotePlaybackClient.ItemActionCallback() {
            override fun onResult(
                data: Bundle?,
                sessionId: String?,
                sessionStatus: MediaSessionStatus?,
                itemId: String?,
                itemStatus: MediaItemStatus?
            ) {
                logStatus("getStatus: succeeded", sessionId, sessionStatus, itemId, itemStatus)
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
                if (update) {
                    mCallback.onPlaylistReady()
                }
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("getStatus: failed", error, code)
                if (update) {
                    mCallback.onPlaylistReady()
                }
            }
        })
    }

    override fun pause() {
        if (mClient?.hasSession() != true) {
            // ignore if no session
            return
        }
        if (DEBUG) {
            Log.d(TAG, "pause")
        }


        mClient?.pause(null, object : RemotePlaybackClient.SessionActionCallback() {
            override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {

                logStatus("pause: succeeded", sessionId, sessionStatus, null, null)
                mCallback.onPlaylistChanged()

                isPlaying.value = false

            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("pause: failed", error, code)
            }
        })
    }

    override fun resume() {
        if (mClient?.hasSession() != true) {
            // ignore if no session
            return
        }
        if (DEBUG) {
            Log.d(TAG, "resume")
        }
        mClient?.resume(null, object : RemotePlaybackClient.SessionActionCallback() {
            override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
                logStatus("resume: succeeded", sessionId, sessionStatus, null, null)
                mCallback.onPlaylistChanged()

                isPlaying.value = true
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("resume: failed", error, code)
            }
        })
    }

    override fun stop() {
        if (mClient?.hasSession() != true) {
            // ignore if no session
            return
        }
        if (DEBUG) {
            Log.d(TAG, "stop")
        }
        mClient?.stop(null, object : RemotePlaybackClient.SessionActionCallback() {
            override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
                logStatus("stop: succeeded", sessionId, sessionStatus, null, null)
                if (mClient?.isSessionManagementSupported == true) {
                    endSession()
                }
                mCallback.onPlaylistChanged()
                isPlaying.value = false
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("stop: failed", error, code)
            }
        })
    }

    override fun enqueue(item: Samples.Sample) {
        throwIfQueuingUnsupported()

        if (mClient?.hasSession() != true && !mEnqueuePending) {
            mEnqueuePending = true
            if (mClient?.isSessionManagementSupported == true) {
                startSession(item)
            } else {
                enqueueInternal(item)
            }
        } else if (mEnqueuePending) {
            mTempQueue.add(item)
        } else {
            enqueueInternal(item)
        }
    }

    override fun remove(iid: String): Samples.Sample? {
        throwIfNoSession()
        throwIfQueuingUnsupported()

        if (DEBUG) {
            Log.d(TAG, "remove: itemId=$iid")
        }
        mClient?.remove(iid, null, object : RemotePlaybackClient.ItemActionCallback() {
            override fun onResult(
                data: Bundle?,
                sessionId: String?,
                sessionStatus: MediaSessionStatus?,
                itemId: String?,
                itemStatus: MediaItemStatus?
            ) {
                logStatus("remove: succeeded", sessionId, sessionStatus, itemId, itemStatus)
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("remove: failed", error, code)
            }
        })

        return null
    }

    override fun setPlaylist(playlist: List<Samples.Sample>) {

    }

    override fun setVolume(volume: Float) {
        Log.d(TAG,"Volume")
    }

    override fun getTrackPosition(): Long? {
        return null
    }

    private fun enqueueInternal(item: Samples.Sample) {
        throwIfQueuingUnsupported()

        if (DEBUG) {
            Log.d(TAG, "enqueue: item=$item")
        }
        mClient?.enqueue(item.url.toUri(), "audio/*", null, 0, null, object : RemotePlaybackClient.ItemActionCallback() {
            override fun onResult(
                data: Bundle?,
                sessionId: String?,
                sessionStatus: MediaSessionStatus?,
                itemId: String?,
                itemStatus: MediaItemStatus?
            ) {
                logStatus("enqueue: succeeded", sessionId, sessionStatus, itemId, itemStatus)
                item.remoteItemId = itemId
                if (item.position > 0) {
                    seekInternal(item)
                }
                if (item.state == MediaItemStatus.PLAYBACK_STATE_PAUSED) {
                    pause()
                }
                if (mEnqueuePending) {
                    mEnqueuePending = false
                    for (temp in mTempQueue) {
                        enqueueInternal(temp)
                    }
                    mTempQueue.clear()
                }
                mCallback.onPlaylistChanged()
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("enqueue: failed", error, code)
                mCallback.onPlaylistChanged()
            }
        })
    }

    private fun seekInternal(item: Samples.Sample) {
        throwIfNoSession()

        if (DEBUG) {
            Log.d(TAG, "seek: item=$item")
        }
        mClient?.seek(item.remoteItemId, item.position, null, object : RemotePlaybackClient.ItemActionCallback() {
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

    private fun startSession(item: Samples.Sample) {
        mClient?.startSession(null, object : RemotePlaybackClient.SessionActionCallback() {
            override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
                logStatus("startSession: succeeded", sessionId, sessionStatus, null, null)
                enqueueInternal(item)
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
                logError("endSession: failed", error, code)
            }
        })
    }

    private fun endSession() {
        mClient?.endSession(null, object : RemotePlaybackClient.SessionActionCallback() {
            override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
                logStatus("endSession: succeeded", sessionId, sessionStatus, null, null)
            }

            override fun onError(error: String?, code: Int, data: Bundle?) {
               logError("endSession: failed", error, code)
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
    }

    private fun logError(message: String?, error: String?, code: Int) {
        Log.d(TAG, "$message: error=$error, code=$code")
    }

    private fun throwIfNoSession() {
        if (mClient?.hasSession() != true) {
            throw IllegalStateException("Session is invalid")
        }
    }

    private fun throwIfQueuingUnsupported() {
        if (!isQueuingSupported()) {
            throw UnsupportedOperationException("Queuing is unsupported")
        }
    }

    private val mStatusCallback = object : RemotePlaybackClient.StatusCallback() {
        override fun onItemStatusChanged(
            data: Bundle?,
            sessionId: String?,
            sessionStatus: MediaSessionStatus?,
            itemId: String?,
            itemStatus: MediaItemStatus?
        ) {
            logStatus("onItemStatusChanged", sessionId, sessionStatus, itemId, itemStatus)
            if (itemStatus?.playbackState == MediaItemStatus.PLAYBACK_STATE_FINISHED) {
                mCallback.onCompletion()
            } else if (itemStatus?.playbackState == MediaItemStatus.PLAYBACK_STATE_ERROR) {
                mCallback.onError()
            }

            if(itemStatus?.playbackState == MediaItemStatus.PLAYBACK_STATE_PAUSED){
                setPlayerState(false)
            } else if(itemStatus?.playbackState == MediaItemStatus.PLAYBACK_STATE_PLAYING){
                setPlayerState(true)
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

    private fun setPlayerState(isRemotePlaying : Boolean){
        isPlaying.value = isRemotePlaying
    }



}