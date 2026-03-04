package com.colorclock.watchface

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setPadding
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Companion phone / watch config activity that lets the user pick
 * one of the five colour palettes.  The selected palette is stored
 * via SharedPreferences AND sent to the watchface via the
 * UserStyleSchema API (the WatchFaceService reads it from there).
 *
 * For Wear OS the recommended way is the built-in style editor,
 * but this activity gives us a polished custom UI feel.
 */
class PalettePickerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0D0D0D"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Title
        val title = TextView(this).apply {
            text = "Choose Color Palette"
            setTextColor(Color.WHITE)
            textSize = 18f
            setPadding(0, 8, 0, 24)
        }
        root.addView(title)

        // Current selection
        val prefs = getSharedPreferences(ColorClockWatchFaceService.PREFS_NAME, Context.MODE_PRIVATE)
        val currentOrdinal = prefs.getInt(ColorClockWatchFaceService.PREF_KEY_PALETTE, 0)

        // Create a button for each palette
        for (palette in ColorPalette.entries) {
            val card = createPaletteCard(palette, palette.ordinal == currentOrdinal) {
                prefs.edit()
                    .putInt(ColorClockWatchFaceService.PREF_KEY_PALETTE, palette.ordinal)
                    .apply()
                // Refresh all cards
                recreate()
            }
            root.addView(card)
        }

        scrollView.addView(root)
        setContentView(scrollView)
    }

    private fun createPaletteCard(
        palette: ColorPalette,
        isSelected: Boolean,
        onClick: () -> Unit
    ): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 0, 0, 12)
            layoutParams = lp
            setBackgroundColor(if (isSelected) Color.parseColor("#2A2A3E") else Color.parseColor("#1A1A1A"))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }

        // Mini preview circle
        val preview = PalettePreviewView(this, palette).apply {
            layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                setMargins(0, 0, 16, 0)
            }
        }
        container.addView(preview)

        // Label
        val label = TextView(this).apply {
            text = palette.displayName
            setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#AAAAAA"))
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        container.addView(label)

        // Checkmark
        if (isSelected) {
            val check = TextView(this).apply {
                text = "✓"
                setTextColor(Color.parseColor("#4CAF50"))
                textSize = 22f
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            container.addView(check)
        }

        return container
    }

    /**
     * Tiny view that draws a miniature preview of the three concentric rings.
     */
    private class PalettePreviewView(
        context: Context,
        private val palette: ColorPalette
    ) : View(context) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val r = min(cx, cy) - 2f

            bgPaint.color = palette.backgroundColor
            canvas.drawCircle(cx, cy, r, bgPaint)

            // Seconds ring (outer, thin)
            paint.strokeWidth = r * 0.1f
            paint.color = palette.secondsColor
            val sr = r - paint.strokeWidth / 2f
            canvas.drawArc(RectF(cx - sr, cy - sr, cx + sr, cy + sr), -90f, 270f, false, paint)

            // Minutes ring
            paint.strokeWidth = r * 0.16f
            paint.color = palette.minutesColor
            val mr = sr - r * 0.1f - 2f - paint.strokeWidth / 2f
            canvas.drawArc(RectF(cx - mr, cy - mr, cx + mr, cy + mr), -90f, 200f, false, paint)

            // Hours ring
            paint.color = palette.hoursColor
            val hr = mr - paint.strokeWidth - 2f
            canvas.drawArc(RectF(cx - hr, cy - hr, cx + hr, cy + hr), -90f, 120f, false, paint)
        }
    }
}
