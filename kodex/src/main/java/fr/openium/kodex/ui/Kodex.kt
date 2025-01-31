package fr.openium.kodex.ui

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastAny
import fr.openium.kodex.KodexPdfRenderer
import fr.openium.kodex.TAG
import fr.openium.kodex.ext.asFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 3f
private const val DOUBLE_TAP_MAX_SCALE = 2f
private const val DOUBLE_TAP_SCALE_DURATION_IN_MILLIS = 300

@Composable
fun Kodex(
    data: Any?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    onPageChanged: (currentPage: Int, totalPages: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var kodexPdfRenderer by remember { mutableStateOf<KodexPdfRenderer?>(null) }
    var totalPageCount by remember { mutableIntStateOf(0) }

    val scale = remember { Animatable(MIN_SCALE, visibilityThreshold = 1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val topOverflow = remember { Animatable(0f) }
    val bottomOverflow = remember { Animatable(0f) }
    val lazyListState = rememberLazyListState()

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
            .width(100.dp)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGesturesWithoutConsume(
                    onGesture = { centroid, pan, zoom, _ ->
                        scope.launch {
                            val targetScale = (scale.value * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                            val scaleChange = targetScale / scale.value

                            val maxOffsetX = (size.width * targetScale - size.width) / 2
                            val maxOffsetY = (size.height * targetScale - size.height) / 2

                            val center = size.toSize().center
                            val targetOffsetX = offsetX.value * scaleChange - (centroid.x - center.x) * (scaleChange - 1) + pan.x

                            if (scaleChange != 1f) {
                                val yOffset = (offsetY.value * scaleChange - (centroid.y - center.y) * (scaleChange - 1))
                                    .coerceIn(-maxOffsetY, maxOffsetY)

                                val targetTopOverflow = (maxOffsetY - yOffset) / targetScale
                                val targetBottomOverflow = (maxOffsetY + yOffset) / targetScale

                                Log.d("TAP", "targetY : $yOffset")
                                Log.d("TAP", "top : $targetTopOverflow")
                                Log.d("TAP", "bottom : $targetBottomOverflow")
                                Log.d("TAP", "----------------")

                                topOverflow.snapTo(targetValue = targetTopOverflow)
                                bottomOverflow.snapTo(targetValue = targetBottomOverflow)
                                offsetY.snapTo(targetValue = yOffset)
                            }

                            offsetX.snapTo(targetValue = targetOffsetX.coerceIn(-maxOffsetX, maxOffsetX))
                            scale.snapTo(targetValue = targetScale)
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        // Animate the scale and reset offsets on double-tap
                        scope.launch {
                            val targetScale = if (scale.value > MIN_SCALE) MIN_SCALE else DOUBLE_TAP_MAX_SCALE
                            val scaleChange = targetScale / scale.value

                            val maxOffsetX = (size.width * targetScale - size.width) / 2
                            val maxOffsetY = (size.height * targetScale - size.height) / 2

                            val center = size.toSize().center

                            // Calculate the target offsets based on the zoom target
                            val targetOffsetX = if (targetScale == MIN_SCALE) {
                                0f
                            } else {
                                offsetX.value * scaleChange - (tapOffset.x - center.x) * (scaleChange - 1)
                            }.coerceIn(-maxOffsetX, maxOffsetX)

                            val targetOffsetY = if (targetScale == MIN_SCALE) {
                                0f
                            } else {
                                offsetY.value * scaleChange - (tapOffset.y - center.y) * (scaleChange - 1)
                            }.coerceIn(-maxOffsetY, maxOffsetY)

                            val animationSpec = tween<Float>(
                                durationMillis = DOUBLE_TAP_SCALE_DURATION_IN_MILLIS,
                                easing = FastOutSlowInEasing,
                            )

                            val jobs = buildList {
                                add(
                                    launch {
                                        scale.animateTo(
                                            targetValue = targetScale,
                                            animationSpec = animationSpec,
                                        )
                                    }
                                )
                                add(
                                    launch {
                                        offsetX.animateTo(
                                            targetValue = targetOffsetX,
                                            animationSpec = animationSpec,
                                        )
                                    }
                                )
                                add(
                                    launch {
                                        offsetY.animateTo(
                                            targetValue = targetOffsetY,
                                            animationSpec = animationSpec,
                                        )
                                    }
                                )
                            }

                            // Wait for all animations to complete
                            jobs.joinAll()
                        }
                    }
                )
            }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                }
                .padding(
                    with(density) {
                        PaddingValues(
                            top = topOverflow.value.toDp(),
                            bottom = bottomOverflow.value.toDp()
                        )
                    },
                ),
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement
        ) {
            items(totalPageCount) { index ->
                KodexPage(
                    index = index,
                    kodexPdfRenderer = kodexPdfRenderer,
                    onPageRendered = remember {
                        {
                            onPageChanged(index + 1, totalPageCount)
                        }
                    },
                )
            }
        }
    }
}

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
    start = this.calculateStartPadding(LayoutDirection.Ltr) + other.calculateStartPadding(LayoutDirection.Ltr),
    top = this.calculateTopPadding() + other.calculateTopPadding(),
    end = this.calculateEndPadding(LayoutDirection.Ltr) + other.calculateEndPadding(LayoutDirection.Ltr),
    bottom = this.calculateBottomPadding() + other.calculateBottomPadding(),
)

private suspend fun PointerInputScope.detectTransformGesturesWithoutConsume(
    panZoomLock: Boolean = false,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val zoomChange = event.calculateZoom()
            val rotationChange = event.calculateRotation()
            val panChange = event.calculatePan()

            if (!pastTouchSlop) {
                zoom *= zoomChange
                rotation += rotationChange
                pan += panChange

                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                val zoomMotion = abs(1 - zoom) * centroidSize
                val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                val panMotion = pan.getDistance()

                if (zoomMotion > touchSlop ||
                    rotationMotion > touchSlop ||
                    panMotion > touchSlop
                ) {
                    pastTouchSlop = true
                    lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                }
            }

            if (pastTouchSlop) {
                val centroid = event.calculateCentroid(useCurrent = false)
                val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                if (effectiveRotation != 0f ||
                    zoomChange != 1f ||
                    panChange != Offset.Zero
                ) {
                    onGesture(centroid, panChange, zoomChange, effectiveRotation)
                }
            }
        } while (event.changes.fastAny { it.pressed })
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
