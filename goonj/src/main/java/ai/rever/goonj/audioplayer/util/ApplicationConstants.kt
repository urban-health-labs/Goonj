package ai.rever.goonj.audioplayer.util

import androidx.lifecycle.MutableLiveData

// Audio LocalPlayer Helper
const val ACTION_PAUSE_SESSION = "ly.blissful.bliss.action.PAUSE_SESSION"
const val ACTION_RESUME_SESSION = "ly.blissful.bliss.action.RESUME_SESSION"
const val ACTION_STOP = "ly.blissful.bliss.action.STOP"
const val ACTION_NEXT = "ly.blissful.bliss.action.NEXT"
const val ACTION_ADD_AUDIO_TO_PLAYLIST = "ly.blissful.bliss.action.ADD_AUDIO_TO_PLAYLIST"
const val ACTION_ADD_PLAYLIST = "ly.blissful.bliss.action.ACTION_ADD_PLAYLIST"
const val ACTION_START_NEW_SESSION = "ly.blissful.bliss.action.ACTION_START_NEW_SESSION"

// Notification
const val REMOVE_NOTIFICATION_FOREGROUND = 0
const val REMOVE_FOREGROUND = 1
const val ACTIVE_NOTIFICATION_FOREGROUND = 2

const val AUDIO_CHANNEL_ID = "bliss_audio"

val audioURLKey = "audio_url"

// Audio Service
val isPlaying : MutableLiveData<Boolean> = MutableLiveData()

//
val PLAYBACK_CHANNEL_ID = "playback_channel"
val PLAYBACK_NOTIFICATION_ID = 1
val MEDIA_SESSION_TAG = "audio_demo"
val DOWNLOAD_CHANNEL_ID = "download_channel"
val DOWNLOAD_NOTIFICATION_ID = 2