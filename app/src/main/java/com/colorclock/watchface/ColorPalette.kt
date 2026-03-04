package com.colorclock.watchface

import android.graphics.Color

/**
 * Defines the 5 preset color palettes for the Color Clock watchface.
 * Each palette provides a base color for seconds, minutes, and hours rings.
 * The renderer fades each from bright → dark as the hand sweeps clockwise.
 */
enum class ColorPalette(
    val displayName: String,
    val secondsColor: Int,   // brightest tone for the seconds ring
    val minutesColor: Int,   // brightest tone for the minutes ring
    val hoursColor: Int,     // brightest tone for the hours ring
    val backgroundColor: Int // center / background fill
) {
    METALLIC(
        displayName = "Metallic",
        secondsColor  = Color.parseColor("#C0C0C0"), // silver
        minutesColor  = Color.parseColor("#CFB53B"), // old gold
        hoursColor    = Color.parseColor("#B87333"), // copper
        backgroundColor = Color.parseColor("#1A1A2E")
    ),
    WINTER(
        displayName = "Winter",
        secondsColor  = Color.parseColor("#A8D8EA"), // ice blue
        minutesColor  = Color.parseColor("#62B6CB"), // frost
        hoursColor    = Color.parseColor("#1B4965"), // deep winter
        backgroundColor = Color.parseColor("#0D1B2A")
    ),
    SPACE(
        displayName = "Space",
        secondsColor  = Color.parseColor("#E8D5F5"), // nebula lavender
        minutesColor  = Color.parseColor("#7B2FBE"), // cosmic purple
        hoursColor    = Color.parseColor("#00B4D8"), // stellar cyan
        backgroundColor = Color.parseColor("#020024")
    ),
    AUTUMN(
        displayName = "Autumn",
        secondsColor  = Color.parseColor("#F4A261"), // sandy orange
        minutesColor  = Color.parseColor("#E76F51"), // burnt sienna
        hoursColor    = Color.parseColor("#8B1A1A"), // deep crimson
        backgroundColor = Color.parseColor("#1A0A00")
    ),
    DARK(
        displayName = "Dark",
        secondsColor  = Color.parseColor("#B0B0B0"), // light grey
        minutesColor  = Color.parseColor("#707070"), // mid grey
        hoursColor    = Color.parseColor("#404040"), // charcoal
        backgroundColor = Color.parseColor("#000000")
    );

    companion object {
        fun fromOrdinal(ordinal: Int): ColorPalette =
            entries.getOrElse(ordinal) { METALLIC }
    }
}
