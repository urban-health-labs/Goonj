package ai.rever.goonj.analytics

import ai.rever.goonj.analytics.GoonjAnalytics.logEvent
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

open class ExoPlayerEvenListenerImp : Player.EventListener {
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val map = mutableMapOf(PLAY_WHEN_READY to playWhenReady, PLAYBACK_STATE to playbackState)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_PLAYER_STATE_CHANGED,
            map
        )
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?,
                                 trackSelections: TrackSelectionArray?) {
        val map = mutableMapOf(
            TRACK_GROUPS to trackGroups,
            TRACK_SELECTIONS to trackSelections)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_TRACKS_CHANGED,
            map
        )
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        val map = mutableMapOf(PLAYBACK_PARAMETERS to playbackParameters)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_PLAYBACK_PARAMETERS_CHANGED,
            map
        )
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        val map = mutableMapOf(IS_LOADING to isLoading)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_LOADING_CHANGED,
            map
        )
    }

    override fun onPositionDiscontinuity(reason: Int) {
        val map = mutableMapOf(REASON to reason)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_POSITION_DISCONTINUITY,
            map
        )
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        val map = mutableMapOf(REPEAT_MODE to repeatMode)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_REPEAT_MODE_CHANGED,
            map
        )
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        val map = mutableMapOf(SHUFFLE_MODE_ENABLED to shuffleModeEnabled)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_SHUFFLE_MODE_ENABLED_CHANGED,
            map
        )
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        val map = mutableMapOf(TIMELINE to timeline, MANIFEST to manifest, REASON to reason)
        logEvent(
            false,
            PlayerAnalyticsEnum.ON_TIMELINE_CHANGED,
            map
        )
    }
}