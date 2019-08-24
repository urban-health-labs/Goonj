package ai.rever.goonj.audioplayer.local

import ai.rever.goonj.BuildConfig
import ai.rever.goonj.R
import ai.rever.goonj.audioplayer.analytics.*
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.net.toUri
import androidx.mediarouter.media.MediaRouter
import ai.rever.goonj.audioplayer.download.DownloadUtil
import ai.rever.goonj.audioplayer.interfaces.AudioPlayer
import ai.rever.goonj.audioplayer.models.Samples
import ai.rever.goonj.audioplayer.util.*
import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.lang.ref.WeakReference

class LocalPlayer (var weakReferenceService: WeakReference<Service>) : AudioPlayer(),  AudioManager.OnAudioFocusChangeListener {


    val TAG = "LOCAL_PLAYER"
    private val DEBUG = BuildConfig.DEBUG
    val service: Service? get() = weakReferenceService.get()

    private var exoPlayer : SimpleExoPlayer
    private lateinit var playerNotificationManager: PlayerNotificationManager

    var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector : MediaSessionConnector? = null

    var context : Context? = service
    private var concatenatingMediaSource = ConcatenatingMediaSource()
    private lateinit var cacheDataSourceFactory : CacheDataSourceFactory

    var playList : MutableList<Samples.Track> = mutableListOf()

    companion object{
        @SuppressLint("StaticFieldLeak")
        var INSTANCE : LocalPlayer? = null

        fun getInstance(service: Service): AudioPlayer {
            if(INSTANCE == null){
                INSTANCE = LocalPlayer(WeakReference(service))
            }

            return INSTANCE!!
        }
    }

    private fun onSetup(){
        Log.d(TAG,"onSetup Local")
        setupDataSource()
        initNotification()
        setupMediaSession()
        addListeners()
    }

    private fun setupDataSource(){
        Log.d(TAG,"setupDataSource Local")
        context?.let {context ->
            val dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name))
            )

            cacheDataSourceFactory = CacheDataSourceFactory(
                DownloadUtil.getCache(context), dataSourceFactory, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
            )
        }
    }


    private val notificationListener = object : PlayerNotificationManager.NotificationListener {
        override fun onNotificationStarted(notificationId: Int, notification: Notification) {
            notification.contentIntent = PendingIntent.getActivity(
                service?.baseContext, PLAYBACK_NOTIFICATION_ID,
                Intent(),

                PendingIntent.FLAG_CANCEL_CURRENT
            )
            service?.startForeground(notificationId, notification)
        }

        override fun onNotificationCancelled(notificationId: Int) {
            pause()
            //service.stopSelf()
        }
    }

    private val notificationAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String {
            mCURRENT_PLAYING_ITEM.postValue(playList[player.currentWindowIndex])
            return playList[player.currentWindowIndex].title
        }

        @Nullable
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return PendingIntent.getActivity(service,0, Intent(), PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @Nullable
        override fun getCurrentContentText(player: Player): String? {
            return playList[player.currentWindowIndex].artist
        }

        @Nullable
        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap {
            return Samples.getBitmap(context, playList[player.currentWindowIndex].bitmapResource)!!
        }
    }

    private fun initNotification(){
        Log.d(TAG,"setupDataSource Local")

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context, PLAYBACK_CHANNEL_ID,R.string.channel_name, PLAYBACK_NOTIFICATION_ID,
            notificationAdapter, notificationListener)

        playerNotificationManager.setPlayer(exoPlayer)

    }

    /**
     * Customize Notification Manager
     * @param useNavigationAction Display Previous/Next Action
     * @param usePlayPauseAction Display Play/Pause Action
     * @param fastForwardIncrementMs Set forward increment in milliseconds. 0 ms will hide it
     * @param rewindIncrementMs Set rewind increment in milliseconds. 0 ms will hide it
     */
    override fun customiseNotification(useNavigationAction: Boolean , usePlayPauseAction: Boolean,
                                  fastForwardIncrementMs: Long, rewindIncrementMs: Long){

        playerNotificationManager.setUseNavigationActions(useNavigationAction)
        playerNotificationManager.setUsePlayPauseActions(usePlayPauseAction)
        playerNotificationManager.setFastForwardIncrementMs(fastForwardIncrementMs)
        playerNotificationManager.setRewindIncrementMs(rewindIncrementMs)
    }

    private fun setupMediaSession(){
        Log.d(TAG,"setupDataSource Local")
        mediaSession = MediaSessionCompat(context, MEDIA_SESSION_TAG)
        mediaSession?.isActive = true

        playerNotificationManager.setMediaSessionToken(mediaSession?.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector?.setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                Log.d(TAG,"Window Index: $windowIndex")
                return Samples.getMediaDescription(context, playList[windowIndex])
            }
        })
        mediaSessionConnector?.setPlayer(exoPlayer)
    }

    private fun addAudioPlaylist(vararg audioList: Samples.Track) {
        Log.d(TAG,"setupDataSource Local")
        for(audio in audioList){
            val mediaSource =  ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(audio.url.toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
            playList.add(audio)
        }
        exoPlayer.prepare(concatenatingMediaSource)
        exoPlayer.playWhenReady = true

        exoPlayer.repeatMode
    }

    private val analyticsListener = object : AnalyticsListener {
        override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime?) {
            val map = mutableMapOf(EVENT_TIME to eventTime)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_SEEK_PROCESSED, map)
        }

        override fun onPlayerError(eventTime: AnalyticsListener.EventTime?, error: ExoPlaybackException?) {
            val map = mutableMapOf(EVENT_TIME to eventTime, ERROR to error)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_PLAYER_ERROR, map)
        }

        override fun onSeekStarted(eventTime: AnalyticsListener.EventTime?) {
            val map = mutableMapOf(EVENT_TIME to eventTime)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_SEEK_STARTED, map)
        }

        override fun onLoadingChanged(eventTime: AnalyticsListener.EventTime?, isLoading: Boolean) {
            val map = mutableMapOf(EVENT_TIME to eventTime, IS_LOADING to isLoading)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_LOADING_CHANGED, map)
        }

        override fun onVolumeChanged(eventTime: AnalyticsListener.EventTime?, volume: Float) {
            val map = mutableMapOf(EVENT_TIME to eventTime, VOLUME to volume)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_VOLUME_CHANGED, map)
        }

        override fun onLoadCompleted(
            eventTime: AnalyticsListener.EventTime?,
            loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
            mediaLoadData: MediaSourceEventListener.MediaLoadData?
        ) {
            val map = mutableMapOf(EVENT_TIME to eventTime, LOAD_EVENT_INFO to loadEventInfo, MEDIA_LOAD_EVENT to mediaLoadData)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_LOAD_COMPLETED,map)
        }

        override fun onMetadata(eventTime: AnalyticsListener.EventTime?, metadata: Metadata?) {
            val map = mutableMapOf(EVENT_TIME to eventTime, METADATA to metadata)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_METADATA,map)
        }
    }

    private val eventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val map = mutableMapOf(PLAY_WHEN_READY to playWhenReady, PLAYBACK_STATE to playbackState)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_PLAYER_STATE_CHANGED, map)

            if (playWhenReady && playbackState == Player.STATE_READY) {
                requestAudioFocus()
                mIsPlaying.postValue(true)
            } else if (playWhenReady) {

            } else {
                removeAudioFocus()
                mIsPlaying.postValue(false)
            }
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            val map = mutableMapOf(PLAYBACK_PARAMETERS to playbackParameters)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_PLAYBACK_PARAMETERS_CHANGED, map)
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            val map = mutableMapOf(TRACK_GROUPS to trackGroups, TRACK_SELECTIONS to trackSelections)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_TRACKS_CHANGED, map)
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            val map = mutableMapOf(IS_LOADING to isLoading)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_LOADING_CHANGED, map)
        }

        override fun onPositionDiscontinuity(reason: Int) {
            val map = mutableMapOf(REASON to reason)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_POSITION_DISCONTINUITY, map)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            val map = mutableMapOf(REPEAT_MODE to repeatMode)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_REPEAT_MODE_CHANGED, map)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            val map = mutableMapOf(SHUFFLE_MODE_ENABLED to shuffleModeEnabled)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_SHUFFLE_MODE_ENABLED_CHANGED , map)
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            val map = mutableMapOf(TIMELINE to timeline, MANIFEST to manifest, REASON to reason)
            logEventBehaviour(false, PlayerAnalyticsEnum.ON_TIMELINE_CHANGED, map)
        }



    }

    private fun addListeners(){
        exoPlayer.addAnalyticsListener(analyticsListener)
        exoPlayer.addListener(eventListener)
    }

    private fun removeListeners(){
        exoPlayer.removeAnalyticsListener(analyticsListener)
        exoPlayer.removeListener(eventListener)
    }

    override fun startNewSession(){
        playList.clear()
        concatenatingMediaSource.clear()
    }

    override fun isRemotePlayback(): Boolean {
        return false
    }

    override fun isQueuingSupported(): Boolean {
        return true
    }

    override fun connect(route: MediaRouter.RouteInfo?) {
        if (DEBUG) {
            Log.d(TAG, "connecting to: $route")
        }
    }

    override fun release() {
        if (DEBUG) {
            Log.d(TAG, "releasing")
        }
        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null)
        exoPlayer.playWhenReady = false
        mIsPlaying.postValue(false)
        removeListeners()
        exoPlayer.release()
        playerNotificationManager.setPlayer(null)
    }

    override fun play(item: Samples.Track) {
        if (DEBUG) {
            Log.d(TAG, "play: item=$item")
        }
        exoPlayer.playWhenReady = true
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(exoPlayer.currentPosition + positionMs)
    }

    override fun pause() {
        if (DEBUG) {
            Log.d(TAG, "pause")
        }
        exoPlayer.playWhenReady = false
    }

    override fun resume() {
        if (DEBUG) {
            Log.d(TAG, "resume")
            Log.d(TAG,"Playlist Size: ${playList.size}")
        }
        exoPlayer.playWhenReady = true
    }

    override fun stop() {
        if (DEBUG) {
            Log.d(TAG, "stop")
        }
        exoPlayer.stop()
    }

    override fun enqueue(item: Samples.Track) {
        if (DEBUG) {
            Log.d(TAG, "enqueue")
        }
        addAudioPlaylist(item)
    }

    override fun remove(iid: String): Samples.Track? {
        throw UnsupportedOperationException("LocalPlayer doesn't support remove!")    }

    override fun setPlaylist(playlist: List<Samples.Track>) {
        startNewSession()
        for(item in playlist){
            addAudioPlaylist(item)
        }
    }

    override fun setVolume(volume: Float) {
        exoPlayer.volume = volume
    }

    override fun getTrackPosition(): Long? {
        return exoPlayer.currentPosition
    }

    lateinit var audioManager : AudioManager
    lateinit var playbackAttributes: AudioAttributes
    lateinit var focusRequest: AudioFocusRequest
    var mPlaybackDelayed = false
    var mResumeOnFocusGain = false
    val mFocusLock = Object()

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if(mPlaybackDelayed || mResumeOnFocusGain){
                    synchronized(mFocusLock){
                        mPlaybackDelayed = false
                        mResumeOnFocusGain = false
                    }
                    exoPlayer.volume = 1f
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(mFocusLock) {
                    mResumeOnFocusGain = false
                    mPlaybackDelayed = false
                }
                pause()
                removeAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                exoPlayer.volume = 0.1f
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(mFocusLock){
                    mResumeOnFocusGain = exoPlayer.playWhenReady
                    mPlaybackDelayed = false
                }
                exoPlayer.volume = 0.1f
            }
        }
    }

    init {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
        exoPlayer.playWhenReady = true

        onSetup()
    }

}