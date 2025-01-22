package fr.openium.kodex.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream

internal class CacheManager(private val context: Context) {
    private val memoryCache: LruCache<Int, Bitmap> = createMemoryCache()
    private val cacheDir = File(context.cacheDir, CACHE_PATH)

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    private fun createMemoryCache(): LruCache<Int, Bitmap> {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt() // Use 1/8th of available memory for cache
        val cacheSize = maxMemory / 8
        return object : LruCache<Int, Bitmap>(cacheSize) {
            override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount / 1024
        }
    }

    fun initCache() {
        val cacheDir = File(context.cacheDir, CACHE_PATH)
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        }
        cacheDir.mkdirs()
    }

    fun getBitmapFromCache(pageNo: Int): Bitmap? =
        memoryCache.get(pageNo) ?: decodeBitmapFromDiskCache(pageNo)

    private fun decodeBitmapFromDiskCache(pageNo: Int): Bitmap? {
        val file = File(cacheDir, pageNo.toString())
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    fun addBitmapToCache(pageNo: Int, bitmap: Bitmap, compressQuality: Int = 75) {
        memoryCache.put(pageNo, bitmap)

        val file = File(cacheDir, pageNo.toString())
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, fos)
        }
    }

    fun clearCache() {
        memoryCache.evictAll()
        clearDirectoryCache()
    }

    fun clearDirectoryCache() {
        cacheDir.deleteRecursively()
    }

    companion object {
        const val CACHE_PATH = "kodex_cache_dir"
    }
}
