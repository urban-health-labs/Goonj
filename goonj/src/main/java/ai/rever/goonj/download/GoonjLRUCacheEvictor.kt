package ai.rever.goonj.download

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheEvictor
import com.google.android.exoplayer2.upstream.cache.CacheSpan
import java.io.IOException
import java.util.*
import kotlin.NoSuchElementException

class GoonjLRUCacheEvictor(private val maxBytes: Long)
    : CacheEvictor, Comparator<CacheSpan> {

    private val downloadManager get() = GoonjDownloadManager.downloadManager

    private val leastRecentlyUsed = TreeSet<CacheSpan>(this)

    private var currentSize: Long = 0

    override fun requiresCacheSpanTouches(): Boolean {
        return true
    }

    override fun onCacheInitialized() {
        // Do nothing.
    }

    override fun onStartFile(cache: Cache, key: String, position: Long, length: Long) {
        if (length != C.LENGTH_UNSET.toLong()) {
            evictCache(cache, length)
        }
    }

    override fun onSpanAdded(cache: Cache, span: CacheSpan) {
        if (span.isNormalCache) {
            leastRecentlyUsed.add(span)
            currentSize += span.length
            evictCache(cache, 0)
        }
    }

    override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
        if (span.isNormalCache) {
            leastRecentlyUsed.remove(span)
            currentSize -= span.length
        }
    }

    override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) {
        onSpanRemoved(cache, oldSpan)
        onSpanAdded(cache, newSpan)
    }

    override fun compare(lhs: CacheSpan, rhs: CacheSpan): Int {
        val lastTouchTimestampDelta = lhs.lastTouchTimestamp - rhs.lastTouchTimestamp
        if (lastTouchTimestampDelta == 0L) {
            // Use the standard compareTo method as a tie-break.
            return lhs.compareTo(rhs)
        }
        return if (lhs.lastTouchTimestamp < rhs.lastTouchTimestamp) -1 else 1
    }

    private fun evictCache(cache: Cache, requiredSpace: Long) {
        while (currentSize + requiredSpace > maxBytes && leastRecentlyUsed.any { it.isNormalCache }) {
            try {
                val first = leastRecentlyUsed.first { it.isNormalCache }
                cache.removeSpan(first)
            } catch (e: NoSuchElementException) {
                return
            } catch (e: Cache.CacheException) {
                // do nothing.
            }
        }
    }

    private val CacheSpan.isNormalCache  get() = try {
        downloadManager.downloadIndex.getDownload(key) == null
    } catch (e: IOException) {
        false
    }
}