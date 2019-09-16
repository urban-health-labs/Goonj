package ai.rever.goonj.audioplayer.util

import ai.rever.goonj.audioplayer.interfaces.PlaybackManager
import ai.rever.goonj.audioplayer.service.AudioPlayerService
import android.content.Context

open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}

//open class PlayerManagerHolder<B: AudioPlayerService>(creator: (Context) -> PlaybackManager<B>) {
//    private var creator: ((Context) -> PlaybackManager<B>)? = creator
//    @Volatile private var instance: PlaybackManager<B>? = null
//
//    fun getInstance(arg: Context): PlaybackManager<B> {
//        val i = instance
//        if (i != null) {
//            return i
//        }
//
//        return synchronized(this) {
//            val i2 = instance
//            if (i2 != null) {
//                i2
//            } else {
//                val created = creator!!(arg)
//                instance = created
//                creator = null
//                created
//            }
//        }
//    }
//}
