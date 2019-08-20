package ai.rever.goonj.audioplayer.local

import ai.rever.goonj.R
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
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.lang.ref.WeakReference



class LocalPlayer (var weakReferenceService: WeakReference<Service>) : AudioPlayer(){


    val TAG = "LOCAL_PLAYER"
    private val DEBUG = true

    val service: Service? get() = weakReferenceService.get()

    var exoPlayer : SimpleExoPlayer
    lateinit var playerNotificationManager: PlayerNotificationManager

    var mediaSession: MediaSessionCompat? = null
    var mediaSessionConnector : MediaSessionConnector? = null

    var context : Context? = service
    private var concatenatingMediaSource = ConcatenatingMediaSource()
    private lateinit var cacheDataSourceFactory : CacheDataSourceFactory

    var playList : MutableList<Samples.Sample> = mutableListOf()

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

    private val listener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playWhenReady && playbackState == Player.STATE_READY) {
                Log.d(TAG,"PLAYER IS PLAYING")
                isPlaying.postValue(true)
            } else if (playWhenReady) {

            } else {
                Log.d(TAG,"PLAYER IS PAUSED")
                isPlaying.postValue(false)
            }
        }
    }

    private fun onSetup(){
        Log.d(TAG,"onSetup Local")
        setupDataSource()
        addAudioListener()

        initNotification()
        setupMediaSession()
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

    private fun addAudioListener(){
        Log.d(TAG,"setupDataSource Local")
        exoPlayer.addListener(listener)
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

    private fun addAudioPlaylist(vararg audioList: Samples.Sample) {
        Log.d(TAG,"setupDataSource Local")
        for(audio in audioList){
            val mediaSource =  ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(audio.url.toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
            playList.add(audio)
        }
        exoPlayer.prepare(concatenatingMediaSource)
        exoPlayer.playWhenReady = true
    }

    override fun startNewSession(){
        Log.d(TAG,"setupDataSource Local")
        playList.clear()
        concatenatingMediaSource.clear()
    }

    private val notificationListener = object : PlayerNotificationManager.NotificationListener {
        override fun onNotificationStarted(notificationId: Int, notification: Notification) {
            notification.contentIntent = PendingIntent.getActivity(
                service?.baseContext, PLAYBACK_NOTIFICATION_ID,
                //Intent(context, AudioPlayerActivity::class.java),
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
            return playList[player.currentWindowIndex].title
        }

        @Nullable
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return PendingIntent.getActivity(service,0, Intent(), PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @Nullable
        override fun getCurrentContentText(player: Player): String? {
            return playList[player.currentWindowIndex].description
        }

        @Nullable
        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap {
            return Samples.getBitmap(context, playList[player.currentWindowIndex].bitmapResource)!!
        }
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
        isPlaying.postValue(false)
        exoPlayer.removeListener(listener)
        exoPlayer.release()
        playerNotificationManager.setPlayer(null)
    }

    override fun play(item: Samples.Sample) {
        if (DEBUG) {
            Log.d(TAG, "play: item=$item")
        }
        exoPlayer.playWhenReady = true
    }

    override fun seek(item: Samples.Sample) {
        if (DEBUG) {
            Log.d(TAG, "seek: item=$item")
        }
        exoPlayer.seekTo(item.position)
    }

    override fun getStatus(item: Samples.Sample, update: Boolean) {
        Log.d(TAG,"Status")
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

    override fun enqueue(item: Samples.Sample) {
        if (DEBUG) {
            Log.d(TAG, "enqueue")
        }
        addAudioPlaylist(item)
    }

    override fun remove(iid: String): Samples.Sample? {
        throw UnsupportedOperationException("LocalPlayer doesn't support remove!")    }

    override fun setPlaylist(playlist: List<Samples.Sample>) {
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
    init {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
        exoPlayer.playWhenReady = true

        onSetup()
    }

}