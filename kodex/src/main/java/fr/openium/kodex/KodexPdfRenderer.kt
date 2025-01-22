package fr.openium.kodex

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.Size
import fr.openium.kodex.util.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

internal class KodexPdfRenderer(
    context: Context,
    fileDescriptor: ParcelFileDescriptor,
    private val compressQuality: Int = 75
) {
    private val openPages = ConcurrentHashMap<Int, PdfRenderer.Page>()
    private var pdfRenderer: PdfRenderer = PdfRenderer(fileDescriptor)
    private val cacheManager = CacheManager(context)

    private val pageDimensionCache = mutableMapOf<Int, Size>()

    init {
        cacheManager.initCache()
    }

    // --- Memory
    private fun getBitmapFromMemoryCache(pageNo: Int): Bitmap? =
        cacheManager.getBitmapFromCache(pageNo)

    private fun addBitmapToMemoryCache(pageNo: Int, bitmap: Bitmap) {
        try {
            cacheManager.addBitmapToCache(pageNo, bitmap, compressQuality)
        } catch (e: Exception) {
            cacheManager.clearDirectoryCache()
            cacheManager.addBitmapToCache(pageNo, bitmap, compressQuality)
        }
    }

    // --- PdfRenderer

    fun getPageCount(): Int =
        synchronized(this) {
            pdfRenderer.pageCount
        }

    suspend fun renderPage(pageNo: Int, screenWidthInPixels: Int): Bitmap? {
        if (pageNo >= getPageCount()) {
            return null
        }

        // Check if the bitmap is already in memory cache
        val cachedBitmap = getBitmapFromMemoryCache(pageNo)
        if (cachedBitmap != null) {
            return cachedBitmap
        }

        return withContext(Dispatchers.IO) {
            synchronized(this@KodexPdfRenderer) {
                val pageSize = getPageDimensions(pageNo)

                // Calculate the height while maintaining the aspect ratio
                val aspectRatio = pageSize.width.toFloat() / pageSize.height
                val heightInPixels = (screenWidthInPixels / aspectRatio).toInt()

                val bitmap = Bitmap.createBitmap(screenWidthInPixels, maxOf(1, heightInPixels), Bitmap.Config.ARGB_8888)

                openPageSafely(pageNo).use { pdfPage ->
                    try {
                        bitmap.eraseColor(Color.WHITE)
                        pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        // Perform cache operations
                        addBitmapToMemoryCache(pageNo = pageNo, bitmap = bitmap)
                        bitmap
                    } catch (e: Exception) {
                        return@withContext null
                    }
                }
            }
        }
    }

    private fun <T> withPdfPage(pageNo: Int, block: (PdfRenderer.Page) -> T): T =
        pdfRenderer.openPage(pageNo).use { page ->
            return block(page)
        }

    private fun getPageDimensions(pageNo: Int): Size {
        // Return the cached size if available
        pageDimensionCache[pageNo]?.let {
            return it
        }

        // Calculate the size and update the cache
        return withPdfPage(pageNo) { page ->
            Size(page.width, page.height).also { pageSize ->
                pageDimensionCache[pageNo] = pageSize
            }
        }
    }

    private fun openPageSafely(pageNo: Int): PdfRenderer.Page {
        synchronized(this) {
            closeAllOpenPages()
            return pdfRenderer.openPage(pageNo).also { page ->
                openPages[pageNo] = page
            }
        }
    }

    private fun closeAllOpenPages() {
        synchronized(this) {
            openPages.values.forEach { page ->
                try {
                    page.close()
                } catch (e: IllegalStateException) {
                    Log.e("PDFRendererCore", "Page was already closed")
                }
            }
            openPages.clear() // Clear the map after closing all pages.
        }
    }

    fun closePdfRender() {
        synchronized(this) {
            closeAllOpenPages()
            pdfRenderer.close()
            cacheManager.clearCache()
        }
    }
}