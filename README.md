# Color Clock – Wear OS Watchface

A premium analog watchface for Wear OS that displays time through three concentric colour rings with a mesmerising sonar / radar sweep effect.

## How It Works

| Ring | Position | Width | Range |
|------|----------|-------|-------|
| **Seconds** | Outer | Thin | 0 – 60 s |
| **Minutes** | Middle | Thick | 0 – 60 min |
| **Hours** | Inner | Thick | 0 – 12 h |

Each ring sweeps clockwise from 12 o'clock.  
The colour starts **dark** at the beginning of the sweep and gradually **brightens** to the palette's signature colour at the leading edge, creating a smooth radar-glow effect.

## Colour Palettes

| Palette | Seconds | Minutes | Hours | Background |
|---------|---------|---------|-------|------------|
| **Metallic** | Silver | Old Gold | Copper | Dark Navy |
| **Winter** | Ice Blue | Frost | Deep Winter | Midnight |
| **Space** | Nebula Lavender | Cosmic Purple | Stellar Cyan | Void Black |
| **Autumn** | Sandy Orange | Burnt Sienna | Deep Crimson | Ember Black |
| **Dark** | Light Grey | Mid Grey | Charcoal | Pure Black |

Users can switch palettes on-watch via the built-in Wear OS style editor or the custom configuration activity.

## Project Structure

```
app/
└── src/main/
    ├── AndroidManifest.xml
    ├── java/com/colorclock/watchface/
    │   ├── ColorPalette.kt         – 5 preset colour palettes
    │   ├── ColorClockRenderer.kt   – Canvas renderer (3 concentric arcs)
    │   ├── ColorClockWatchFaceService.kt – Wear OS service entry-point
    │   └── PalettePickerActivity.kt – On-watch palette picker UI
    └── res/
        ├── drawable/               – Preview & launcher vectors
        ├── values/strings.xml      – Localisation strings
        └── xml/watchface.xml       – Wallpaper descriptor
```

## Build & Install

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected Wear OS device / emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Requirements:** Android SDK 34, Kotlin 1.9+, Gradle 8.5+

## Publishing to Google Play

1. Generate a signed release APK / AAB: `./gradlew bundleRelease`
2. Create a Wear OS listing on Google Play Console
3. Upload the AAB and fill in the store listing (screenshots, description, pricing)
4. Set the app as **paid** or add in-app purchase for premium palettes

## License

Proprietary – All rights reserved.