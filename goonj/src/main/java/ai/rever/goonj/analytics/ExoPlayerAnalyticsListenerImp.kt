package ai.rever.goonj.analytics

import ai.rever.goonj.analytics.GoonjAnalytics.logEvent
import android.util.Log.e
import android.view.Surface
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import java.io.IOException
import java.lang.Exception

object ExoPlayerAnalyticsListenerImp: AnalyticsListener {
    override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime?) {
        val map = mutableMapOf(EVENT_TIME to eventTime)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_SEEK_PROCESSED,
            map
        )
    }

    override fun onPlayerError(eventTime: AnalyticsListener.EventTime?, error: ExoPlaybackException?) {
        val map = mutableMapOf(EVENT_TIME to eventTime, ERROR to error)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_PLAYER_ERROR,
            map
        )
    }

    override fun onSeekStarted(eventTime: AnalyticsListener.EventTime?) {
        val map = mutableMapOf(EVENT_TIME to eventTime)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_SEEK_STARTED,
            map
        )
    }

    override fun onLoadingChanged(eventTime: AnalyticsListener.EventTime?, isLoading: Boolean) {
        val map = mutableMapOf(EVENT_TIME to eventTime, IS_LOADING to isLoading)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_LOADING_CHANGED,
            map
        )
    }

    override fun onVolumeChanged(eventTime: AnalyticsListener.EventTime?, volume: Float) {
        val map = mutableMapOf(EVENT_TIME to eventTime, VOLUME to volume)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_VOLUME_CHANGED,
            map
        )
    }


    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
        val map = mutableMapOf(EVENT_TIME to eventTime, LOAD_EVENT_INFO to loadEventInfo, MEDIA_LOAD_EVENT to mediaLoadData)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_LOAD_COMPLETED,
            map
        )
    }

    override fun onMetadata(eventTime: AnalyticsListener.EventTime?, metadata: Metadata?) {
        val map = mutableMapOf(EVENT_TIME to eventTime, METADATA to metadata)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_METADATA,
            map
        )
    }

    override fun onPlaybackParametersChanged(
        eventTime: AnalyticsListener.EventTime?,
        playbackParameters: PlaybackParameters?
    ) {
    }

    override fun onDownstreamFormatChanged(
        eventTime: AnalyticsListener.EventTime?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onDrmKeysLoaded(eventTime: AnalyticsListener.EventTime?) {
    }

    override fun onMediaPeriodCreated(eventTime: AnalyticsListener.EventTime?) {
    }

    override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime?, surface: Surface?) {
    }

    override fun onReadingStarted(eventTime: AnalyticsListener.EventTime?) {
    }

    override fun onBandwidthEstimate(
        eventTime: AnalyticsListener.EventTime?,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long
    ) {
    }

    override fun onPlayerStateChanged(
        eventTime: AnalyticsListener.EventTime?,
        playWhenReady: Boolean,
        playbackState: Int
    ) {
    }

    override fun onAudioAttributesChanged(
        eventTime: AnalyticsListener.EventTime?,
        audioAttributes: AudioAttributes?
    ) {
    }

    override fun onDrmSessionAcquired(eventTime: AnalyticsListener.EventTime?) {
    }

    override fun onDrmKeysRestored(eventTime: AnalyticsListener.EventTime?) {
    }

    override fun onDecoderDisabled(
        eventTime: AnalyticsListener.EventTime?,
        trackType: Int,
        decoderCounters: DecoderCounters?
    ) {
    }

    override fun onShuffleModeChanged(
        eventTime: AnalyticsListener.EventTime?,
        shuffleModeEnabled: Boolean
    ) {
    }

    override fun onDecoderInputFormatChanged(
        eventTime: AnalyticsListener.EventTime?,
        trackType: Int,
        format: Format?
    ) {
    }

    override fun onAudioSessionId(eventTime: AnalyticsListener.EventTime?, audioSessionId: Int) {
    }

    override fun onDrmSessionManagerError(
        eventTime: AnalyticsListener.EventTime?,
        error: Exception?
    ) {
    }

    override fun onSurfaceSizeChanged(
        eventTime: AnalyticsListener.EventTime?,
        width: Int,
        height: Int
    ) {
    }

    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onTracksChanged(
        eventTime: AnalyticsListener.EventTime?,
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
    }

    override fun onPositionDiscontinuity(eventTime: AnalyticsListener.EventTime?, reason: Int) {
    }

    override fun onRepeatModeChanged(eventTime: AnalyticsListener.EventTime?, repeatMode: Int) {
    }

    override fun onUpstreamDiscarded(
        eventTime: AnalyticsListener.EventTime?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onLoadCanceled(
        eventTime: AnalyticsListener.EventTime?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?
    ) {
    }

    override fun onDrmSessionReleased(eventTime: AnalyticsListener.EventTime?) {
    }

    override fun onMediaPeriodReleased(eventTime: AnalyticsListener.EventTime?) {
    }

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime?, reason: Int) {
    }

    override fun onDecoderInitialized(
        eventTime: AnalyticsListener.EventTime?,
        trackType: Int,
        decoderName: String?,
        initializationDurationMs: Long
    ) {
    }

    override fun onDroppedVideoFrames(
        eventTime: AnalyticsListener.EventTime?,
        droppedFrames: Int,
        elapsedMs: Long
    ) {
    }

    override fun onDecoderEnabled(
        eventTime: AnalyticsListener.EventTime?,
        trackType: Int,
        decoderCounters: DecoderCounters?
    ) {
    }

    override fun onVideoSizeChanged(
        eventTime: AnalyticsListener.EventTime?,
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
    }

    override fun onAudioUnderrun(
        eventTime: AnalyticsListener.EventTime?,
        bufferSize: Int,
        bufferSizeMs: Long,
        elapsedSinceLastFeedMs: Long
    ) {
    }

    override fun onDrmKeysRemoved(eventTime: AnalyticsListener.EventTime?) {
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?,
        error: IOException?,
        wasCanceled: Boolean
    ) {
    }
}