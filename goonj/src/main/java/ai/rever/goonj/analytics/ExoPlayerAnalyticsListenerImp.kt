package ai.rever.goonj.analytics

import ai.rever.goonj.analytics.GoonjAnalytics.logEvent
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.MediaSourceEventListener

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
}