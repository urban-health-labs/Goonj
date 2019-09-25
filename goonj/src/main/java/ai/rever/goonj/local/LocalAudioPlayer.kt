package ai.rever.goonj.local

import ai.rever.goonj.R
import ai.rever.goonj.analytics.*
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.Nullable
import androidx.core.net.toUri
import androidx.mediarouter.media.MediaRouter
import ai.rever.goonj.download.DownloadUtil
import ai.rever.goonj.models.Track
import ai.rever.goonj.models.getBitmap
import ai.rever.goonj.models.getMediaDescription
import ai.rever.goonj.service.GoonjService
import ai.rever.goonj.util.MEDIA_SESSION_TAG
import ai.rever.goonj.util.PLAYBACK_CHANNEL_ID
import ai.rever.goonj.util.PLAYBACK_NOTIFICATION_ID
import ai.rever.goonj.util.SingletonHolder
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util

class LocalAudioPlayer private constructor (goonjService: GoonjService): FocusAudioPlayer(goonjService) {

    companion object : SingletonHolder<LocalAudioPlayer, GoonjService>(::LocalAudioPlayer)

    private val isPlaying: MutableLiveData<Boolean>? by lazy { this.goonjService?.isPlaying }
    private val currentPlayingTrack : MutableLiveData<Track>? by lazy { this.goonjService?.mCurrentPlayingTrack }

    private var autoplay : Boolean = true

    private var concatenatingMediaSource = ConcatenatingMediaSource()

    private var playList : MutableList<Track> = mutableListOf()
    private var isPlayerPrepared : Boolean = false

    private var pendingIntent = Intent()

    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var cacheDataSourceFactory: CacheDataSourceFactory
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    private var isFirstTimeAfterCreation = true
    private var isFirstTimeAfterNewSession = true

    override fun setPendingIntentForNotification(intent: Intent) {
        pendingIntent = intent
    }

    private fun initialize() {
        initDataSource()
        initNotification()
        initMediaSession()
    }

    private fun initDataSource() {
        appContext?.apply {
            val dataSourceFactory = DefaultDataSourceFactory(
                this, Util.getUserAgent(this, getString(R.string.app_name))
            )
            cacheDataSourceFactory = CacheDataSourceFactory(
                DownloadUtil.getCache(this),
                dataSourceFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
            )
        }
    }

    private fun initNotification() {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            appContext,
            PLAYBACK_CHANNEL_ID, R.string.channel_name,
            PLAYBACK_NOTIFICATION_ID,
            notificationAdapter, notificationListener)

    }

    private fun initMediaSession(){
        mediaSession = MediaSessionCompat(appContext, MEDIA_SESSION_TAG)

        mediaSessionConnector = MediaSessionConnector(mediaSession)
    }

    private fun setup() {
        mediaSession.isActive = true

        playerNotificationManager.setPlayer(player)
        mediaSessionConnector.setPlayer(player)

        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)

        mediaSessionConnector.setQueueNavigator(object: TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player, index: Int): MediaDescriptionCompat {
                return getMediaDescription(appContext, playList[index])
            }
        })

        addListeners()
    }


    private val notificationListener = object : PlayerNotificationManager.NotificationListener {

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification?,
            ongoing: Boolean
        ) {
            notification?.contentIntent = PendingIntent.getActivity(
                this@LocalAudioPlayer.goonjService?.baseContext, PLAYBACK_NOTIFICATION_ID,
                pendingIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }

    private val notificationAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String {
            currentPlayingTrack?.postValue(playList[player.currentWindowIndex])
            return playList[player.currentWindowIndex].title
        }

        @Nullable
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return PendingIntent.getActivity(this@LocalAudioPlayer.goonjService,0, pendingIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @Nullable
        override fun getCurrentContentText(player: Player): String? {
            return playList[player.currentWindowIndex].artistName
        }

        @Nullable
        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap {
//            return playList[player.currentWindowIndex].bitmap?: getBitmap(appContext, playList[player.currentWindowIndex].bitmapResource)!!
            return getBitmap(appContext)!!
        }
    }


    /**
     * Customize Notification Manager
     * @param useNavigationAction Display Previous/Next Action
     * @param usePlayPauseAction Display Play/Pause Action
     * @param fastForwardIncrementMs Set forward increment in milliseconds. 0 ms will hide it
     * @param rewindIncrementMs Set rewind increment in milliseconds. 0 ms will hide it
     */
    override fun customiseNotification(useNavigationAction: Boolean , usePlayPauseAction: Boolean,
                                  fastForwardIncrementMs: Long, rewindIncrementMs: Long,smallIcon: Int?){

        playerNotificationManager.setUseNavigationActions(useNavigationAction)
        playerNotificationManager.setUsePlayPauseActions(usePlayPauseAction)
        playerNotificationManager.setFastForwardIncrementMs(fastForwardIncrementMs)
        playerNotificationManager.setRewindIncrementMs(rewindIncrementMs)
        smallIcon?.let {
            playerNotificationManager.setSmallIcon(it)
        }
    }


    private fun addAudioPlaylist(audio: Track, index: Int) {
        val mediaSource =  ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(audio.url.toUri())

        if(index != -1){
            concatenatingMediaSource.addMediaSource(index, mediaSource)
            playList.add(index, audio)
        } else {
            concatenatingMediaSource.addMediaSource(mediaSource)
            playList.add(audio)
        }

        if(!isPlayerPrepared) {
            player.playWhenReady = true
            player.prepare(concatenatingMediaSource)
            isPlayerPrepared = true
        }
    }

    private val eventListener = object: ExoPlayerEvenListenerImp() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            player.let {
                currentPlayingTrack?.value?.currentData?.duration = it.contentDuration
            }
            /** if player is ready to play and is commanded to play **/
            if (playWhenReady && playbackState == Player.STATE_READY) {
                requestAudioFocus(focusLock)
                isPlaying?.postValue(true)

            } else if (!playWhenReady) {
                removeAudioFocus()
                /** attach listener if player is playing for the first time or
                 * if its playing after a new session is played atleast once**/
                if (isFirstTimeAfterCreation || !isFirstTimeAfterNewSession) {
                    /** set false as the player is created**/
                    isFirstTimeAfterCreation = false
                    isPlaying?.postValue(false)
                } else {
                    /** if the player is playing first time after a new session
                     * do not attach the listener **/
                    isFirstTimeAfterNewSession = false
                }
                this@LocalAudioPlayer.goonjService?.stopForeground(true)
            }
        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray?,
            trackSelections: TrackSelectionArray?
        ) {
            super.onTracksChanged(trackGroups, trackSelections)
            if(!autoplay) {
                pause()
            }
            // Todo: Completion call
        }
    }

    private fun addListeners(){
        player.addAnalyticsListener(ExoPlayerAnalyticsListenerImp)
        player.addListener(eventListener)
    }

    private fun removeListeners(){
        player.removeAnalyticsListener(ExoPlayerAnalyticsListenerImp)
        player.removeListener(eventListener)
    }

    override fun startNewSession(){
        isFirstTimeAfterNewSession = true
        playList.clear()
        concatenatingMediaSource.clear()
        isPlayerPrepared = false
    }

    override fun connect(route: MediaRouter.RouteInfo) {}

    override fun release() {
        mediaSession.release()
        mediaSessionConnector.setPlayer(null)
        player.playWhenReady = false
        isPlaying?.postValue(false)
        removeListeners()
        playerNotificationManager.setPlayer(null)
        player.release()
        isPlayerPrepared = false
    }

    override fun play(item: Track) {
        player.playWhenReady = true
    }

    override fun seekTo(positionMs: Long) {
        player.let {
            it.seekTo(it.currentPosition + positionMs)
        }
    }

    override fun suspend() = pause()

    override fun unsuspend(track: Track) {
        resume()
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun resume() {
        player.playWhenReady = true
    }

    override fun stop() {
        player.stop()
        release()
    }

    override fun enqueue(track: Track, index : Int) {
        addAudioPlaylist(track, index)
        playerNotificationManager.setPlayer(player)
    }

    override fun remove(index: Int){
        concatenatingMediaSource.removeMediaSource(index)
        playList.removeAt(index)
    }

    override fun moveTrack(currentIndex: Int, finalIndex: Int) {
        val currentTrack = playList[currentIndex]
        playList.removeAt(currentIndex)
        playList.add(finalIndex-1,currentTrack)

        concatenatingMediaSource.moveMediaSource(currentIndex,finalIndex)
    }

    override fun skipToNext() {
        player.next()
    }

    override fun skipToPrevious() {
        player.previous()
    }

    override fun setPlaylist(playlist: List<Track>) {
        startNewSession()
        for(item in playlist){
            addAudioPlaylist(item, -1)
        }
    }

    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    override fun getTrackPosition(): Long {
        return player.currentPosition
    }

    override fun setAutoplay(autoplay: Boolean) {
        this.autoplay = autoplay
    }

    override fun removeNotification() {
        playerNotificationManager.setPlayer(null)
        goonjService?.stopForeground(true)
    }

    init {
        initialize()
        setup()
    }
}