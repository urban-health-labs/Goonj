package ai.rever.goonj.analytics

import ai.rever.goonj.analytics.GoonjAnalytics.logEvent
import android.os.Bundle
import androidx.mediarouter.media.MediaItemStatus
import androidx.mediarouter.media.MediaSessionStatus
import androidx.mediarouter.media.RemotePlaybackClient

open class ItemActionCallbackImp(var operation: String, var onSuccess: ((String?, MediaItemStatus?) -> Unit)? = null) : RemotePlaybackClient.ItemActionCallback() {


    override fun onResult(
        data: Bundle?,
        sessionId: String?,
        sessionStatus: MediaSessionStatus?,
        itemId: String?,
        itemStatus: MediaItemStatus?
    ) {
        logStatus("$operation: succeeded", sessionId,
            sessionStatus, itemId, itemStatus)
        onSuccess?.invoke(itemId, itemStatus)
    }

    override fun onError(error: String?, code: Int, data: Bundle?) {
        logError("$operation: failed", error , code)
    }
}

open class SessionActionCallbackImp(var operation: String, var onSuccess: ((String?, MediaSessionStatus?) -> Unit)? = null) : RemotePlaybackClient.SessionActionCallback() {

    override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
        logStatus("$operation: succeeded", sessionId,
            sessionStatus, null, null)
        onSuccess?.invoke(sessionId, sessionStatus)
    }


    override fun onError(error: String?, code: Int, data: Bundle?) {
        logError("$operation: failed", error , code)

    }
}

private fun logStatus(
    message: String,
    sessionId: String?, sessionStatus: MediaSessionStatus?,
    itemId: String?, itemStatus: MediaItemStatus?
) {
    val map = mutableMapOf(
        MESSAGE to message, SESSION_ID to sessionId, SESSION_STATUS to sessionStatus,
        ITEM_ID to itemId, ITEM_STATUS to itemStatus)
    logEvent(
        true,
        PlayerAnalyticsEnum.REMOTE_LOG_STATUS,
        map
    )
}

private fun logError(message: String?, error: String?, code: Int) {
    val map = mutableMapOf(
        MESSAGE to message,
        ERROR_REMOTE to error,
        ERROR_REMOTE_CODE to code)

    logEvent(
        true,
        PlayerAnalyticsEnum.REMOTE_LOG_ERROR,
        map
    )
}