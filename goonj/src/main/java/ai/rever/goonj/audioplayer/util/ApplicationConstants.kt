package ai.rever.goonj.audioplayer.util

import ai.rever.goonj.audioplayer.models.Samples
import androidx.lifecycle.MutableLiveData

// Audio LocalPlayer Helper
const val ACTION_PAUSE_SESSION = "ly.blissful.bliss.action.PAUSE_SESSION"
const val ACTION_RESUME_SESSION = "ly.blissful.bliss.action.RESUME_SESSION"
const val ACTION_STOP = "ly.blissful.bliss.action.STOP"
const val ACTION_NEXT = "ly.blissful.bliss.action.NEXT"
const val ACTION_ADD_AUDIO_TO_PLAYLIST = "ly.blissful.bliss.action.ADD_AUDIO_TO_PLAYLIST"
const val ACTION_ADD_PLAYLIST = "ly.blissful.bliss.action.ACTION_ADD_PLAYLIST"
const val ACTION_START_NEW_SESSION = "ly.blissful.bliss.action.ACTION_START_NEW_SESSION"
const val ACTION_CUSTOMIZE_NOTIFICATION = "ly.blissful.bliss.action.ACTION_CUSTOMIZE_NOTIFICATION"
const val ACTION_SEEK_TO = "ly.blissful.bliss.action.ACTION_SEEK_TO"

const val audioURLKey = "audio_url"

val isPlaying : MutableLiveData<Boolean> = MutableLiveData()
val currentPlayingItem : MutableLiveData<Samples.Sample?> = MutableLiveData()

//
const val PLAYBACK_CHANNEL_ID = "playback_channel"
const val PLAYBACK_NOTIFICATION_ID = 1
const val MEDIA_SESSION_TAG = "audio_demo"
const val DOWNLOAD_CHANNEL_ID = "download_channel"
const val DOWNLOAD_NOTIFICATION_ID = 2

const val USE_NAV_ACTION = "use_nav_action"
const val USE_PLAY_PAUSE = "use_play_pause"
const val FAST_FORWARD_INC = "fast_forward_inc"
const val REWIND_INC = "rewind_inc"
const val SEEK_TO = "seek_to"
