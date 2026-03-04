package com.colorclock.watchface

import android.graphics.*
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import java.time.ZonedDateTime
import kotlin.math.min
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom CanvasRenderer that draws three concentric arcs:
 *   • Hours   – inner ring
 *   • Minutes – middle ring (same width as hours)
 *   • Seconds – outer ring (thinner)
 *
 * Each ring is drawn as a series of tiny arc segments whose color fades from
 * the palette's bright colour down to near-black as the sweep reaches the
 * current time position, producing a smooth sonar / radar glow effect.
 */
class ColorClockRenderer(
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    private val paletteProvider: () -> ColorPalette
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = CanvasType.HARDWARE,
    interactiveDrawModeUpdateDelayMillis = 16L, // ~60 fps for smooth second sweep
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {

    /* ------------------------------------------------------------------ */
    /*  Shared-assets (none needed, but the API requires the type)         */
    /* ------------------------------------------------------------------ */
    class ColorClockSharedAssets : SharedAssets {
        override fun onDestroy() {}
    }

    override suspend fun createSharedAssets(): SharedAssets = ColorClockSharedAssets()

    /* ------------------------------------------------------------------ */
    /*  Paint objects – reused every frame                                 */
    /* ------------------------------------------------------------------ */
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /* ------------------------------------------------------------------ */
    /*  Rendering constants                                                */
    /* ------------------------------------------------------------------ */
    companion object {
        private const val ARC_SEGMENTS = 360      // one segment per degree
        private const val GAP_BETWEEN_RINGS = 4f  // dp-ish gap
        private const val RING_RATIO_THICK = 0.14f // width ratio for hours & minutes
        private const val RING_RATIO_THIN  = 0.08f // width ratio for seconds (thinner)
        private const val AMBIENT_SEGMENTS = 60    // fewer segments in ambient mode
        private const val TICK_COUNT = 12          // hour tick marks
    }

    /* ------------------------------------------------------------------ */
    /*  Main draw call                                                     */
    /* ------------------------------------------------------------------ */
    override fun render(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        val palette = paletteProvider()
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val radius = min(cx, cy)
        val isAmbient = renderParameters.drawMode == androidx.wear.watchface.DrawMode.AMBIENT

        // Background
        bgPaint.color = palette.backgroundColor
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // Ring widths
        val thickWidth = radius * RING_RATIO_THICK
        val thinWidth  = radius * RING_RATIO_THIN

        // Calculate ring radii (from outside → inside)
        val secondsRadius = radius - thinWidth / 2f - 2f
        val minutesRadius = secondsRadius - thinWidth / 2f - GAP_BETWEEN_RINGS - thickWidth / 2f
        val hoursRadius   = minutesRadius - thickWidth / 2f - GAP_BETWEEN_RINGS - thickWidth / 2f

        // Current time fractions (0..1)
        val sec = zonedDateTime.second + zonedDateTime.nano / 1_000_000_000f
        val min = zonedDateTime.minute + sec / 60f
        val hr  = (zonedDateTime.hour % 12) + min / 60f

        val secondsFraction = sec / 60f
        val minutesFraction = min / 60f
        val hoursFraction   = hr / 12f

        val segments = if (isAmbient) AMBIENT_SEGMENTS else ARC_SEGMENTS

        // Draw rings
        drawRing(canvas, cx, cy, secondsRadius, thinWidth,  palette.secondsColor, palette.backgroundColor, secondsFraction, segments, isAmbient)
        drawRing(canvas, cx, cy, minutesRadius, thickWidth, palette.minutesColor, palette.backgroundColor, minutesFraction, segments, isAmbient)
        drawRing(canvas, cx, cy, hoursRadius,   thickWidth, palette.hoursColor,   palette.backgroundColor, hoursFraction,   segments, isAmbient)

        // Draw subtle tick marks at 12 positions on outside of seconds ring
        drawTickMarks(canvas, cx, cy, secondsRadius + thinWidth / 2f + 2f, palette, isAmbient)

        // Center dot
        centerDotPaint.color = palette.secondsColor
        canvas.drawCircle(cx, cy, 4f, centerDotPaint)
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        // No highlight layer needed
    }

    /* ------------------------------------------------------------------ */
    /*  Draw a single ring                                                 */
    /* ------------------------------------------------------------------ */
    /**
     * Draws one concentric ring as a sweep of arc segments, starting at 12-o'clock
     * and going clockwise for [fraction] of the full circle.
     *
     * The color starts as a very dark version of [brightColor] at 12-o'clock and
     * gradually brightens to full [brightColor] at the leading edge.
     * Everything past the leading edge is a faint ghost ring so the user still
     * sees the track.
     */
    private fun drawRing(
        canvas: Canvas,
        cx: Float, cy: Float,
        ringRadius: Float,
        strokeWidth: Float,
        brightColor: Int,
        bgColor: Int,
        fraction: Float,
        segments: Int,
        isAmbient: Boolean
    ) {
        val rect = RectF(
            cx - ringRadius, cy - ringRadius,
            cx + ringRadius, cy + ringRadius
        )

        arcPaint.strokeWidth = strokeWidth

        val totalSweep = fraction * 360f
        val segmentAngle = 360f / segments

        // 1) Draw the faint track (full circle ghost)
        arcPaint.color = blendColors(brightColor, bgColor, 0.85f) // mostly background
        canvas.drawArc(rect, -90f, 360f, false, arcPaint)

        // 2) Draw the active sweep with gradient effect
        if (totalSweep > 0f) {
            val activeSegments = (totalSweep / segmentAngle).toInt().coerceAtLeast(1)
            for (i in 0 until activeSegments) {
                // t goes from 0 (start of sweep, dimmest) to 1 (leading edge, brightest)
                val t = i.toFloat() / activeSegments.toFloat()

                // Fade from dark → bright
                val dimColor = darkenColor(brightColor, 0.15f) // very dark
                val segColor = blendColors(dimColor, brightColor, t)

                arcPaint.color = segColor
                val startAngle = -90f + i * segmentAngle
                canvas.drawArc(rect, startAngle, segmentAngle + 0.5f, false, arcPaint)
            }

            // Add a bright cap at the leading edge for a crisp look
            if (!isAmbient) {
                arcPaint.color = brightColor
                val capAngle = -90f + totalSweep - 1.5f
                canvas.drawArc(rect, capAngle, 2f, false, arcPaint)
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Tick marks                                                         */
    /* ------------------------------------------------------------------ */
    private fun drawTickMarks(canvas: Canvas, cx: Float, cy: Float, outerRadius: Float, palette: ColorPalette, isAmbient: Boolean) {
        val tickLength = 6f
        tickPaint.strokeWidth = 1.5f
        tickPaint.color = blendColors(palette.secondsColor, palette.backgroundColor, 0.4f)

        for (i in 0 until TICK_COUNT) {
            val angle = Math.toRadians((i * 30.0) - 90.0)
            val startX = cx + (outerRadius + 1f) * cos(angle).toFloat()
            val startY = cy + (outerRadius + 1f) * sin(angle).toFloat()
            val endX   = cx + (outerRadius + 1f + tickLength) * cos(angle).toFloat()
            val endY   = cy + (outerRadius + 1f + tickLength) * sin(angle).toFloat()
            canvas.drawLine(startX, startY, endX, endY, tickPaint)
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Color utilities                                                    */
    /* ------------------------------------------------------------------ */

    /** Linearly blend between two colours. ratio=0 → colorA, ratio=1 → colorB */
    private fun blendColors(colorA: Int, colorB: Int, ratio: Float): Int {
        val inv = 1f - ratio
        val a = (Color.alpha(colorA) * inv + Color.alpha(colorB) * ratio).toInt()
        val r = (Color.red(colorA)   * inv + Color.red(colorB)   * ratio).toInt()
        val g = (Color.green(colorA) * inv + Color.green(colorB) * ratio).toInt()
        val b = (Color.blue(colorA)  * inv + Color.blue(colorB)  * ratio).toInt()
        return Color.argb(a, r, g, b)
    }

    /** Darken a colour.  factor=0 → black,  factor=1 → unchanged */
    private fun darkenColor(color: Int, factor: Float): Int {
        return Color.argb(
            Color.alpha(color),
            (Color.red(color)   * factor).toInt(),
            (Color.green(color) * factor).toInt(),
            (Color.blue(color)  * factor).toInt()
        )
    }
}
