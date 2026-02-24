# 🎵 Music Player App

A modern Android music player built with **Jetpack Compose**, **MVVM**, and **Hilt** — featuring a real PCM waveform, 5-band equalizer, and foreground service with notification controls.


---

## Architecture Overview

The app follows **MVVM** with a clear separation of concerns.

```
app/
├── ui/
│   ├── screens/
│   │   ├── onboarding/             # Auto-scroll pager onboarding
│   │   ├── music_player/           # Player UI + ViewModel
│   │   └── base_screens/           # MainActivity
│   └── theme/                      # CustomColors, AppFonts
│
├── utility/
│   └── media/
│       ├── MusicPlayerService.kt        # Foreground service (keeps process alive)
│       ├── MusicNotificationManager.kt  # Builds MediaStyle notification
│       ├── MusicController.kt           # @Singleton SharedFlow command bridge
│       └── MusicCommand.kt              # Enum: PLAY, PAUSE, NEXT, PREVIOUS
│
├── data/
│   └── repository/
│       └── MusicPlayerRepository.kt    # Loads AudioItems from assets
│
└── di/
    └── AppModule.kt                    # Hilt dependency graph
```

**Data flow:**

```
UI (Compose)
    ↕  observes StateFlow
MusicPlayerViewModel  (owns MediaPlayer, Equalizer, Waveform)
    ↕  emits / collects
MusicController  (@Singleton SharedFlow bridge)
    ↕  intent actions
MusicPlayerService  (Foreground Service → Notification)
```

**Why Foreground Service + MusicController?**
The `ViewModel` owns `MediaPlayer` but a `Service` is needed to keep the process alive in the background. Since `Service` and `ViewModel` can't hold direct references to each other, `MusicController` acts as a decoupled `@Singleton` command pipe — notification button taps emit a `MusicCommand` which the `ViewModel` collects and acts on.

---

## Waveform — Implementation Approach

The waveform is built from **real PCM audio data**, not random/fake values.

**Pipeline:**

```
Asset file (.mp3 / .m4a)
      ↓
MediaExtractor  →  selects audio track, reads duration
      ↓
MediaCodec      →  decodes compressed audio to raw PCM samples (ShortArray)
      ↓
Split by time   →  divide total duration into N equal bars
      ↓
Peak amplitude  →  max(abs(sample)) per bar → normalized to 0f..1f
      ↓
Animatable[]    →  each bar animates smoothly to its height (tween 350ms)
      ↓
Canvas          →  drawn as rounded RoundRect bars, active/inactive colored by progress
```

**Key decisions:**
- Waveform is emitted **progressively** — bars appear as they are decoded, not all at once at the end
- On song change, bars animate from old heights to new heights via `Animatable`
- On first load, bars are initialized directly at target height — no blink/flash
- The `Slider` is overlaid transparently on top of the waveform so the thumb aligns precisely with the waveform edges

---

## Equalizer — Implementation Approach

Uses Android's built-in **`android.media.audiofx.Equalizer`** attached to the `MediaPlayer` audio session.

**Band mapping:**

The UI exposes 5 fixed frequency bands (`60Hz, 230Hz, 1kHz, 3.5kHz, 10kHz`). Since Android hardware EQ band count and frequencies vary by device, each UI band is mapped to the **closest hardware band by center frequency** at runtime:

```kotlin
// Finds hardware band whose centerFreq is closest to our target
fun mapUiBandToHwBand(eq: Equalizer, uiBandIndex: Int): Short
```

**Bass & Treble knobs:**

Rather than a separate DSP effect, the knobs blend into the EQ bands:

| Knob | Affects |
|---|---|
| Bass | Band 0 × 1.0 + Band 1 × 0.5 |
| Treble | Band 4 × 1.0 + Band 3 × 0.5 |

This means band slider gain and shelf knob gain are **combined** before being written to the hardware EQ, so they don't cancel each other out.

**Lifecycle:**
- EQ is attached **after** `MediaPlayer.prepare()` and before `play()`
- EQ state (`gains`, `bassDb`, `trebleDb`, `activePreset`) lives in `_eqState: StateFlow` inside the `ViewModel` — survives song changes and recomposition
- On each new song, the full EQ state is reapplied to the new audio session immediately

---

## Known Limitations & Future Improvements

**Known Limitations:**

- Music files must be bundled in `assets/` — no device storage or streaming support yet
- `MediaPlayer` does not support gapless playback between tracks
- EQ hardware band count varies by device (3–10 bands) — mapping is best-effort
- Shuffle is UI-only — logic not yet implemented
- No `MediaSession` — lock screen controls and Bluetooth headset buttons not supported
- Notification artwork is bitmap from metadata only — no fallback image in notification

**Future Improvements:**

- [ ] Migrate from `MediaPlayer` → **ExoPlayer + Media3** for gapless, `MediaSession`, better background handling
- [ ] `MediaSession` integration for lock screen controls and Bluetooth headsets
- [ ] Device storage scanning via `MediaStore` to load songs from the phone
- [ ] Shuffle and repeat modes
- [ ] Sleep timer
- [ ] Home screen widget
- [ ] Streaming / online radio support
- [ ] Playlist creation and management

---

## Build & Run Instructions

### Prerequisites

| Requirement | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or later |
| Kotlin | 1.9+ |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |
| JDK | 17 |

### Notes
- A **physical device** is recommended for testing the Equalizer — audio effects behave differently on emulators
- Grant **notification permission** when prompted (required on Android 13+) for notification controls to appear
- The app requests microphone permission only if the record audio feature is used
