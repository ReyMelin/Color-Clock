# Color Clock – Wear OS Watch Face

Color Clock is a premium Wear OS watch face built with Watch Face Format (WFF).  
It displays time using three concentric sweeping rings inspired by sonar and radar visuals.

## Design

The watch face uses three clockwise sweeps that begin at 12 o’clock:

| Ring | Position | Width | Range |
|------|----------|-------|-------|
| Seconds | Outer | Thin | 0–60 s |
| Minutes | Middle | Thick | 0–60 min |
| Hours | Inner | Thick | 0–12 h |

Each ring includes:
- a faint full-circle ghost track
- a live active sweep
- a palette-driven color treatment

## Visual Style

The intended visual effect is a radar-like sweep:
- darkest at the start of the sweep
- brightest at the leading edge
- rich dark background for contrast

## Palettes

Planned palettes:
- Metallic
- Winter
- Space
- Autumn
- Dark

Palette switching will be implemented through Wear OS watch face editor styling using WFF color configuration.

## Tech Stack

This watch face is built using:
- Watch Face Format (WFF)
- Android SDK / Wear OS
- Gradle

## Project Structure

app/
└── src/main/
    ├── AndroidManifest.xml
    └── res/
        ├── drawable/
        ├── raw/watchface.xml
        ├── values/strings.xml
        └── xml/watch_face_info.xml

## Build

```bash
./gradlew assembleDebug