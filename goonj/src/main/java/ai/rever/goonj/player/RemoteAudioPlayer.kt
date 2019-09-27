package ai.rever.goonj.player

import ai.rever.goonj.Goonj.appContext
import ai.rever.goonj.GoonjPlayerState
import ai.rever.goonj.analytics.*
import android.os.Bundle
import androidx.mediarouter.media.MediaItemStatus
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaSessionStatus
import androidx.mediarouter.media.RemotePlaybackClient
import ai.rever.goonj.interfaces.AudioPlayer
import ai.rever.goonj.manager.GoonjPlayerManager
import ai.rever.goonj.models.Track
import android.os.Handler
import androidx.core.net.toUri
import androidx.mediarouter.media.MediaItemStatus.*
import androidx.mediarouter.media.MediaSessionStatus.*
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastContext
import java.lang.ref.WeakReference

class RemoteAudioPlayer: AudioPlayer {

    private var player: RemotePlaybackClient? = null

    private var autoplay : Boolean = true
    private var isHandlerRunning = false

    override fun connect(route: MediaRouter.RouteInfo) {
        // TODO check for resume services

        player = RemotePlaybackClient(appContext, route)
        player?.setStatusCallback(statusCallback)
    }

    override fun release() {
        player?.release()
    }

    override fun enqueue(track: Track, index: Int) {
        track.trackState.state = GoonjPlayerState.PLAYING
        play(track)
    }

    private fun play(track: Track) {
        player?.play(track.url.toUri(), "audio/*",
            null, 0, null,
            ItemActionCallbackImp("resume") { itemId, itemStatus ->
                if(!isHandlerRunning) {
                    statusHandler()
                }

                track.trackState.remoteItemId = itemId

                if (track.trackState.position > 0) {
                    seekInternal(track)
                }

                if (track.trackState.state == GoonjPlayerState.PLAYING) {
                    pause()
                }

                track.mediaLoadRequestData?.let {
                    setMediaLoadRequest(it)
                }

                GoonjPlayerManager.currentPlayingTrack.onNext(track)
                setStatus(itemStatus, defaultState = GoonjPlayerState.PAUSED)
            })
    }

    private fun setMediaLoadRequest(mediaLoadRequestData: MediaLoadRequestData){

        appContext?.let{
            val castSession = CastContext.getSharedInstance(it).sessionManager.currentCastSession
            try {
                val remoteMediaClient = castSession.remoteMediaClient

                remoteMediaClient.load(mediaLoadRequestData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun seekTo(positionMs: Long) {
        getStatus(true, positionMs)
    }

    fun getStatus(seek: Boolean, positionMs: Long = 0) {
        val track = GoonjPlayerManager.currentPlayingTrack.value
        if (player?.hasSession() != true || track?.trackState?.remoteItemId == null) {
            // if trackList is not valid or track id not assigend yet.
            // just return, it's not fatal
            return
        }

        player?.getStatus(track.trackState.remoteItemId, null,
            object : ItemActionCallbackImp("getStatus", ::updateTrackPosition) {
                override fun onResult(
                    data: Bundle?,
                    sessionId: String?,
                    sessionStatus: MediaSessionStatus?,
                    itemId: String?,
                    itemStatus: MediaItemStatus?
                ) {
                    super.onResult(data, sessionId, sessionStatus, itemId, itemStatus)
                    if (seek) {
                        when {
                            (positionMs) < 0 ->
                                track.trackState.position = 0
                            (positionMs) < track.trackState.duration ->
                                track.trackState.position = positionMs
                            else -> track.trackState.position = track.trackState.duration - 1
                        }
                        seekInternal(track)
                    }

                    setStatus(itemStatus)
                }
            }
        )
    }

    override fun suspend() {
        pause()
    }

    override fun unsuspend() {
        GoonjPlayerManager.currentPlayingTrack.value?.let {
            play(it)
        }
    }

    override fun pause() {
        if (player?.hasSession() != true) {
            // ignore if no trackList
            return
        }

        player?.pause(null, SessionActionCallbackImp("pause") { _, _ ->
            setStatus(defaultState = GoonjPlayerState.PAUSED)
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
            setStatus(defaultState = GoonjPlayerState.PLAYING)
        })
    }

    override fun stop() {
        if (player?.hasSession() != true) {
            // ignore if no trackList
            return
        }

        player?.stop(null, SessionActionCallbackImp("stop") { _, _ ->
            setStatus(defaultState = GoonjPlayerState.CANCELED)
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

        player?.seek(item.trackState.remoteItemId, item.trackState.position,
            null, ItemActionCallbackImp("seekTo", ::updateTrackPosition))

    }

    private fun setStatus(itemStatus: MediaItemStatus? = null, defaultState: GoonjPlayerState = GoonjPlayerState.IDLE) {
        val map = mutableMapOf(IS_REMOTE_PLAYING to itemStatus)
        logEvent(
            true,
            PlayerAnalyticsEnum.SET_PLAYER_STATE_REMOTE,
            map
        )

        val track = GoonjPlayerManager.currentPlayingTrack.value?: return
        if (itemStatus?.apply {
                track.trackState.state = when (playbackState) {
                PLAYBACK_STATE_BUFFERING -> GoonjPlayerState.BUFFERING
                PLAYBACK_STATE_PENDING -> GoonjPlayerState.IDLE
                PLAYBACK_STATE_CANCELED -> GoonjPlayerState.CANCELED
                PLAYBACK_STATE_FINISHED -> GoonjPlayerState.ENDED
                PLAYBACK_STATE_PLAYING -> GoonjPlayerState.PLAYING
                PLAYBACK_STATE_ERROR -> GoonjPlayerState.ERROR
                PLAYBACK_STATE_PAUSED -> GoonjPlayerState.PAUSED
                PLAYBACK_STATE_INVALIDATED -> GoonjPlayerState.INVALIDATE
                else -> GoonjPlayerState.IDLE
            }
            if (contentPosition > 0) {
                track.trackState.position = contentPosition
            }
            if (contentDuration > 0) {
                track.trackState.duration = contentDuration
            }

        } == null) {
            track.trackState.state = defaultState
        }
        GoonjPlayerManager.playerStateBehaviorSubject.onNext(track.trackState.state)
    }

    private fun statusHandler(){
        val handler = Handler()
        handler.postDelayed(object: Runnable{
            override fun run() {
                getStatus(false)
                if(GoonjPlayerManager.playerStateBehaviorSubject.value == GoonjPlayerState.PLAYING) {
                    isHandlerRunning  = true
                    handler.postDelayed(this, 1000)
                } else {
                    isHandlerRunning = false
                }
            }

        },1000)
    }

    private fun updateTrackPosition(itemId: String?, itemStatus: MediaItemStatus?){
        GoonjPlayerManager.currentPlayingTrack.value?.let { track ->
            if (track.id == itemId) {
                itemStatus?.contentPosition?.let {
                    track.trackState.position = it
                }
            }
        }
    }

    private val statusCallback get() = StatusCallback(this)

    /**
     * Making static inner class insure callback does not memory leak, yet access private method
     */
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
            player?.setStatus(itemStatus)
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

