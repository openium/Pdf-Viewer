package fr.openium.kodex.ui

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.openium.kodex.KodexPdfRenderer
import fr.openium.kodex.TAG
import fr.openium.kodex.ext.asFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 3f
private const val ZOOM_DURATION_IN_MILLIS = 300

@Composable
fun Kodex(
    data: Any?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    onPageChanged: (currentPage: Int, totalPages: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var kodexPdfRenderer by remember { mutableStateOf<KodexPdfRenderer?>(null) }

    var totalPageCount by remember { mutableIntStateOf(0) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(data) {
        scope.launch(Dispatchers.IO) {
            data?.let {
                try {
                    context.asFile(data)?.let { file ->
                        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                        kodexPdfRenderer = KodexPdfRenderer(context, fileDescriptor)
                        kodexPdfRenderer?.let {
                            totalPageCount = it.getPageCount()
                            onPageChanged(0, totalPageCount)
                        }
                    } ?: throw IllegalArgumentException("Unsupported data type: $data")
                } catch (e: Exception) {
                    Log.e(TAG, "Can't load file with $data", e)
                }
            }
        }

        onDispose {
            kodexPdfRenderer?.closePdfRender()
            scope.cancel()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        // Animate the scale and reset offsets on double-tap
                        scope.launch {
                            val targetScale = if (scale > 1f) 1f else 2f

                            // Calculate the target offsets based on the zoom target
                            val targetOffsetX = if (targetScale == 1f) {
                                0f
                            } else {
                                -((tapOffset.x - size.width / 2) * (targetScale - 1))
                            }
                            val targetOffsetY = if (targetScale == 1f) {
                                0f
                            } else {
                                -((tapOffset.y - size.height / 2) * (targetScale - 1))
                            }

                            // Animate scale, offsetX, and offsetY concurrently
                            val scaleJob = launch {
                                animate(
                                    initialValue = scale,
                                    targetValue = targetScale,
                                    animationSpec = tween(durationMillis = ZOOM_DURATION_IN_MILLIS, easing = FastOutSlowInEasing)
                                ) { value, _ ->
                                    scale = value
                                }
                            }

                            val offsetXJob = launch {
                                animate(
                                    initialValue = offsetX,
                                    targetValue = targetOffsetX,
                                    animationSpec = tween(durationMillis = ZOOM_DURATION_IN_MILLIS, easing = FastOutSlowInEasing)
                                ) { value, _ ->
                                    offsetX = value
                                }
                            }

                            val offsetYJob = launch {
                                animate(
                                    initialValue = offsetY,
                                    targetValue = targetOffsetY,
                                    animationSpec = tween(durationMillis = ZOOM_DURATION_IN_MILLIS, easing = FastOutSlowInEasing)
                                ) { value, _ ->
                                    offsetY = value
                                }
                            }

                            // Wait for all animations to complete
                            scaleJob.join()
                            offsetXJob.join()
                            offsetYJob.join()
                        }
                    }
                )
            }
    ) {
        LazyColumn(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement
        ) {
            items(totalPageCount) { index ->
                KodexPage(
                    index = index,
                    kodexPdfRenderer = kodexPdfRenderer,
                    onPageRendered = {
                        onPageChanged(index + 1, totalPageCount)
                    }
                )
            }
        }
    }
}

@Composable
private fun KodexPage(
    index: Int,
    kodexPdfRenderer: KodexPdfRenderer?,
    onPageRendered: () -> Unit = {}
) {
    val screenWidthInPixels = LocalContext.current.resources.displayMetrics.widthPixels

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(index, kodexPdfRenderer) {
        withContext(Dispatchers.IO) {
            kodexPdfRenderer?.renderPage(index, screenWidthInPixels)?.let { renderedBitmap ->
                bitmap = renderedBitmap
                onPageRendered()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
    ) {
        bitmap?.let {
            Image(
                modifier = Modifier.fillMaxWidth(),
                bitmap = it.asImageBitmap(),
                contentDescription = "Page $index"
            )
        }
    }
}

