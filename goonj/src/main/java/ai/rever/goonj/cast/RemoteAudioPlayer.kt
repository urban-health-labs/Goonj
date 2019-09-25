package ai.rever.goonj.cast

import ai.rever.goonj.analytics.*
import android.os.Bundle
import androidx.mediarouter.media.MediaItemStatus
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaSessionStatus
import androidx.mediarouter.media.RemotePlaybackClient
import ai.rever.goonj.interfaces.AudioPlayer
import ai.rever.goonj.models.Track
import ai.rever.goonj.service.GoonjService
import ai.rever.goonj.util.SingletonHolder
import android.os.Handler
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.mediarouter.media.MediaItemStatus.*
import androidx.mediarouter.media.MediaSessionStatus.*
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastContext
import java.lang.ref.WeakReference

class RemoteAudioPlayer constructor (service: GoonjService) : AudioPlayer(service) {

    companion object: SingletonHolder<RemoteAudioPlayer, GoonjService>(::RemoteAudioPlayer)

    private var player: RemotePlaybackClient? = null

    private val mIsPlaying: MutableLiveData<Boolean>? by lazy { goonjService?.isPlaying }
    private val mCurrentPlayingTrack : MutableLiveData<Track>? by lazy { goonjService?.mCurrentPlayingTrack }

    private var autoplay : Boolean = true
    private var isHandlerRunning = false

    override fun connect(route: MediaRouter.RouteInfo) {
        // TODO check for play services
        player = RemotePlaybackClient(goonjService, route)
        player?.setStatusCallback(statusCallback)
    }

    override fun release() {
        player?.release()
    }

    override fun enqueue(track: Track, index: Int) {
        track.currentData.state = PLAYBACK_STATE_PLAYING
        play(track)
    }

    override fun play(item: Track) {
        player?.play(item.url.toUri(), "audio/*",
            null, 0, null,
            ItemActionCallbackImp("play") { itemId, _ ->
                if(!isHandlerRunning) {
                    statusHandler()
                }

                item.currentData.remoteItemId = itemId

                if (item.currentData.position > 0) {
                    seekInternal(item)
                }

                if (item.currentData.state == PLAYBACK_STATE_PAUSED) {
                    pause()
                }

                setMediaMetadata(item)
                mCurrentPlayingTrack?.value = item
                mIsPlaying?.value = true
            })
    }

    private fun setMediaMetadata(item: Track){
        val musicMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)

        musicMetadata.putString(MediaMetadata.KEY_TITLE, item.title)
        musicMetadata.putString(MediaMetadata.KEY_ARTIST, item.artistName)

        item.imageUrl?.let {
            musicMetadata.addImage(WebImage(it.toUri()))
        }

        goonjService?.let{
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

    fun getStatus(item: Track, seek: Boolean, positionMs: Long) {
        if (player?.hasSession() != true || item.currentData.remoteItemId == null) {
            // if trackList is not valid or item id not assigend yet.
            // just return, it's not fatal
            return
        }

        player?.getStatus(item.currentData.remoteItemId, null,
            object : ItemActionCallbackImp("getStatus", ::updateTrackPosition) {
                override fun onResult(
                    data: Bundle?,
                    sessionId: String?,
                    sessionStatus: MediaSessionStatus?,
                    itemId: String?,
                    itemStatus: MediaItemStatus?
                ) {
                    super.onResult(data, sessionId, sessionStatus, itemId, itemStatus)
                    val state = itemStatus?.playbackState
                    if (state == PLAYBACK_STATE_PLAYING
                        || state == PLAYBACK_STATE_PAUSED
                        || state == PLAYBACK_STATE_PENDING
                    ) {
                        item.currentData.state = state
                        item.currentData.position = itemStatus.contentPosition
                        item.currentData.duration = itemStatus.contentDuration
                    }
                    if (seek) {
                        when {
                            (item.currentData.position + positionMs) < 0 ->
                                item.currentData.position = 0
                            (item.currentData.position + positionMs) < item.currentData.duration ->
                                item.currentData.position = item.currentData.position + positionMs
                            else -> item.currentData.position = item.currentData.duration
                        }
                        seekInternal(item)
                    }
                }
            }
        )
    }

    override fun suspend() {
        pause()
    }

    override fun unsuspend(track: Track) {
        play(track)
    }

    override fun pause() {
        if (player?.hasSession() != true) {
            // ignore if no trackList
            return
        }

        player?.pause(null, SessionActionCallbackImp("pause") { _, _ ->
            mIsPlaying?.value = false
        })
    }

    override fun resume() {
        if (player?.hasSession() != true) {
            // ignore if no trackList
            return
        }

        player?.resume(null, SessionActionCallbackImp("resume") {  _, _ ->
            if(!isHandlerRunning) {
                statusHandler()
            }
            mIsPlaying?.value = true

        })
    }

    override fun stop() {
        if (player?.hasSession() != true) {
            // ignore if no trackList
            return
        }

        player?.stop(null, SessionActionCallbackImp("stop") { _, _ ->
            mIsPlaying?.value = false
        })

    }


    override fun setAutoplay(autoplay: Boolean) {
        this.autoplay = autoplay

        // TODO : Re-create RemoteAudioPlayer with playlist support
    }

    private fun seekInternal(item: Track) {
        if (player?.hasSession() != true) {
            // ignore if no trackList
            return
        }

        player?.seek(item.currentData.remoteItemId, item.currentData.position,
            null, ItemActionCallbackImp("seek", ::updateTrackPosition))

    }

    private fun setIsPlaying(isPlaying : Boolean) {
        val map = mutableMapOf(IS_REMOTE_PLAYING to isPlaying)
        logEvent(
            true,
            PlayerAnalyticsEnum.SET_PLAYER_STATE_REMOTE,
            map
        )
        mIsPlaying?.value = isPlaying
    }

    private fun statusHandler(){
        val handler = Handler()
        handler.postDelayed(object: Runnable{
            override fun run() {
                mCurrentPlayingTrack?.value?.let {
                    getStatus(it,false,0)
                }

                if(mIsPlaying?.value == true) {
                    isHandlerRunning  = true
                    handler.postDelayed(this, 1000)
                } else {
                    isHandlerRunning = false
                }
            }

        },1000)
    }

    private fun updateTrackPosition(itemId: String?, itemStatus: MediaItemStatus?){
        mCurrentPlayingTrack?.value?.let { track ->
            itemStatus?.contentPosition?.let {
                track.currentData.position = it
            }
        }
    }

    private val statusCallback get() = StatusCallback(this)

    class StatusCallback private constructor(): RemotePlaybackClient.StatusCallback() {

        private lateinit var weakPlayer: WeakReference<RemoteAudioPlayer>
        private val player get() = weakPlayer.get()

        constructor(remoteAudioPlayer: RemoteAudioPlayer): this() {
            weakPlayer = WeakReference(remoteAudioPlayer)
        }

        override fun onItemStatusChanged(
            data: Bundle?,
            sessionId: String?,
            sessionStatus: MediaSessionStatus?,
            itemId: String?,
            itemStatus: MediaItemStatus
        ) {
            when (itemStatus.playbackState) {
                PLAYBACK_STATE_PENDING -> {
                }
                PLAYBACK_STATE_PLAYING -> {
                    player?.setIsPlaying(true)
                }
                PLAYBACK_STATE_PAUSED -> {
                    player?.setIsPlaying(false)
                }
                PLAYBACK_STATE_BUFFERING -> {

                }
                PLAYBACK_STATE_FINISHED -> {

                }
                PLAYBACK_STATE_CANCELED -> {

                }
                PLAYBACK_STATE_INVALIDATED -> {

                }
                PLAYBACK_STATE_ERROR -> {

                }
            }
        }

        override fun onSessionStatusChanged(data: Bundle?, sessionId: String?,
                                            sessionStatus: MediaSessionStatus) {
            when (sessionStatus.sessionState) {
                SESSION_STATE_ACTIVE -> {

                }
                SESSION_STATE_ENDED -> {

                }
                SESSION_STATE_INVALIDATED -> {

                }
            }
        }
    }
}

