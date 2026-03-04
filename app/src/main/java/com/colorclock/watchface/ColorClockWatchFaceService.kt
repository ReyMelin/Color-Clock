package com.colorclock.watchface

import android.content.Context
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import androidx.wear.watchface.*
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.ListUserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer

/**
 * The WatchFace service entry-point.  Wear OS instantiates this service
 * and asks us to build a [WatchFace].
 */
class ColorClockWatchFaceService : WatchFaceService() {

    companion object {
        const val PALETTE_SETTING_ID = "color_palette"
        const val PREFS_NAME = "color_clock_prefs"
        const val PREF_KEY_PALETTE = "selected_palette"
    }

    override fun createUserStyleSchema(): UserStyleSchema {
        val options = ColorPalette.entries.map { palette ->
            ListUserStyleSetting.ListOption(
                UserStyleSetting.Option.Id(palette.name),
                resources,
                when (palette) {
                    ColorPalette.METALLIC -> R.string.palette_metallic
                    ColorPalette.WINTER   -> R.string.palette_winter
                    ColorPalette.SPACE    -> R.string.palette_space
                    ColorPalette.AUTUMN   -> R.string.palette_autumn
                    ColorPalette.DARK     -> R.string.palette_dark
                },
                null  // no icon
            )
        }

        val paletteSetting = ListUserStyleSetting(
            UserStyleSetting.Id(PALETTE_SETTING_ID),
            resources,
            R.string.palette_setting_name,
            R.string.palette_setting_description,
            null, // icon
            options,
            listOf(WatchFaceLayer.BASE)
        )

        return UserStyleSchema(listOf(paletteSetting))
    }

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {

        // Palette provider that reads from the user-style system
        val paletteProvider: () -> ColorPalette = {
            val currentStyle = currentUserStyleRepository.userStyle.value
            val paletteSetting = currentUserStyleRepository.schema.userStyleSettings
                .firstOrNull { it.id.value == PALETTE_SETTING_ID }
            val selectedOption = paletteSetting?.let { currentStyle[it] }
            val paletteName = (selectedOption as? ListUserStyleSetting.ListOption)
                ?.id?.value?.decodeToString() ?: ColorPalette.METALLIC.name
            try {
                ColorPalette.valueOf(paletteName)
            } catch (_: Exception) {
                ColorPalette.METALLIC
            }
        }

        val renderer = ColorClockRenderer(
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            complicationSlotsManager = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository,
            paletteProvider = paletteProvider
        )

        return WatchFace(WatchFaceType.ANALOG, renderer)
    }
}
