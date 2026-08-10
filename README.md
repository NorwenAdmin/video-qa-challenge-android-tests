# Video QA Challenge (Android)

The Android twin of the iOS Video QA Challenge app: a small, deterministic demo app used as the technical assignment for Senior Mobile Test Automation Engineer candidates. It simulates a simplified media product with a consent screen, a video content overview, content detail pages, and a video player with an explicit, inspectable state machine.

- App name: **Video QA Challenge**
- Application id: `com.videoqa.challenge`
- No login, no network, no external services. All content and video are bundled.
- Kotlin, Jetpack Compose, Media3 (ExoPlayer).

## Candidate assignment

Write automated mobile tests for **one** platform of your choice, using **any technology and any programming language**:

- **iOS**: https://github.com/tchumakina/video-qa-challenge-ios
- **Android**: https://github.com/tchumakina/video-qa-challenge-android

Build instructions are in each repository's README, and a prebuilt example binary is available in the `bin/` directory of each repository.

Suggested minimum scope:

1. Launch the application and handle the consent screen.
2. Open `Amsterdam from above` and verify the correct detail page is displayed.
3. Start video playback and verify the player reaches the `Playing` state.
4. Add at least one additional risk-based scenario of your choice.

What to deliver:

- A **public GitHub/GitLab repository** with your tests.
- A clear description of the solution and the **motivation** behind the chosen tools and approach.
- Instructions for **how to run** the tests.
- A **test execution report** stating on which environment the tests were executed (simulator/emulator, real device, device cloud, etc.) and **why that environment was chosen**.

## App overview

| Screen | Purpose |
|---|---|
| Consent | Shown on first launch only. Accept all, reject optional, or manage preferences. |
| Preferences | Analytics and personalised-content toggles, saved locally. |
| Content overview | Deterministic list of six videos with loading, empty, and error states. |
| Content detail | Title, category, description, published date, and video preview with play button. |
| Video player | Custom ExoPlayer UI with play/pause/resume, progress, and a testable state label (`Idle`, `Buffering`, `Playing`, `Paused`, `Error`, `Completed`). |
| Debug options | Gear icon on the overview. Switches content/video response modes and resets app state. |

## Requirements

- JDK 17
- Android SDK with platform 35 (Gradle downloads it automatically if licenses are accepted)
- Min SDK 26 (Android 8.0), target/compile SDK 35
- Recommended emulator: Pixel 6, API 35
- Portrait orientation only

No Android Studio installation is required to build; the Gradle wrapper is included.

## Build instructions

### Prebuilt example binary

A ready-to-use debug APK is committed at `bin/VideoQAChallenge-debug.apk`. You do not need the Android SDK to try the app:

```bash
adb install -r bin/VideoQAChallenge-debug.apk
adb shell am start -n com.videoqa.challenge/.MainActivity
```

The same APK can be used directly as the `app` capability for Appium or uploaded to a device cloud.

### Build a debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Install and launch on a device or emulator

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.videoqa.challenge/.MainActivity
```

### Run the bundled smoke tests (optional)

The project contains a small instrumented test class (`SmokeTest`) used to verify the build. It is not part of the candidate assignment. With an emulator or device connected:

```bash
./gradlew connectedDebugAndroidTest
```

### Build a release APK

```bash
./gradlew assembleRelease
```

The release APK is unsigned by default. For BrowserStack, a debug APK works fine; if you need a signed release build, configure a signing config or sign with `apksigner` using your own keystore. No keystores are included in this repository.

## Testable flows

The behaviour mirrors the iOS app exactly:

- **Consent** appears on first launch only; any selection persists across launches until reset.
- **Content overview** shows a loading indicator for a random 500–1500 ms, then always the same six items in the same order (first: `Amsterdam from above`, Travel, 02:30). At least one item is below the fold, so scrolling is required. Cards share the same visual structure; identify them by content id, never by list position.
- **Content detail** shows title, category, description, published date, and a video preview with a play button. The content id is exposed through the `detail_title` element's state description.
- **Video playback** transitions `Idle → Buffering → Playing` with deterministic simulated buffering (~800 ms normal, ~6 s in Long buffering mode). The state is always readable from `video_state_label`. Pausing saves the position; reopening the same content resumes from it.
- **Empty / error states** for content ("No videos are available" / "Something went wrong") and video ("Video could not be played" with retry), all switchable from debug options.
- **Debug options** persist until changed or reset.

## Test identifiers (resource-id)

All identifiers are Compose test tags exposed as `resource-id` to UiAutomator, Appium, and BrowserStack (via `testTagsAsResourceId`). They are identical to the iOS accessibility identifiers, so test suites can share locators across platforms. Find them with e.g. Appium's `accessibility id`-equivalent for Android: `id=consent_accept_button` (no package prefix).

| Screen | Element | Identifier |
|---|---|---|
| Consent | Screen container | `consent_screen` |
| Consent | Accept all / Reject optional / Manage preferences | `consent_accept_button`, `consent_reject_button`, `consent_manage_preferences_button` |
| Preferences | Screen, toggles, save | `preferences_screen`, `analytics_toggle`, `personalisation_toggle`, `preferences_save_button` |
| Overview | Screen, list, loading | `content_overview_screen`, `content_list`, `content_loading_indicator` |
| Overview | Toolbar buttons | `content_refresh_button`, `debug_options_button` |
| Overview | Card / title per item | `content_item_<contentId>`, `content_title_<contentId>` (e.g. `content_item_amsterdam`) |
| Overview | Empty state | `content_empty_state`, `content_empty_retry_button` |
| Overview | Error state | `content_error_state`, `content_error_message`, `content_error_retry_button` |
| Detail | Fields | `detail_title`, `detail_category`, `detail_description`, `detail_back_button`, `content_detail_screen` |
| Player | Surface and controls | `video_player`, `video_play_button`, `video_pause_button`, `video_buffering_indicator`, `video_progress`, `video_current_position`, `video_duration`, `video_state_label`, `video_error_message`, `video_retry_button` |
| Debug | Content modes | `debug_content_success`, `debug_content_empty`, `debug_content_error`, `debug_content_slow` |
| Debug | Video modes | `debug_video_normal`, `debug_video_buffering`, `debug_video_error`, `debug_video_complete_quickly` |
| Debug | State controls | `debug_reset_consent`, `debug_clear_progress`, `debug_reset_all`, `debug_restore_defaults`, `debug_done_button`, `debug_screen` |

Intentional exception (discussion point): the published date / duration metadata row on the detail screen is a single merged element without an identifier. It is non-critical and not needed for any mandatory flow.

## State reset and launch configuration

### In-app

Debug options → `Reset consent`, `Clear playback progress`, `Restore default settings`, `Reset all app state`.

### Intent extras (equivalent of iOS launch arguments)

Extras override persisted debug settings for that run only (they are not written back to storage). Use `-S` to force-stop the app first so the extras apply to a fresh process:

```bash
adb shell am start -S -n com.videoqa.challenge/.MainActivity \
  --ez resetAllState true          # clear all persisted state
  --ez resetConsent true           # clear only the consent selection
  --es contentMode success|empty|error|slow
  --es videoMode normal|buffering|error|completeQuickly
  --ei contentDelayMs 1000         # fixed content loading delay (replaces the random delay)
  --ei videoBufferingMs 1500       # fixed buffering duration (replaces the mode default)
```

With Appium (UiAutomator2 driver):

```json
"appium:optionalIntentArguments": "--ez resetAllState true --es videoMode buffering"
```

### Removing app data

```bash
adb shell pm clear com.videoqa.challenge   # clear app data (consent shows again)
adb uninstall com.videoqa.challenge        # remove the app
```

## Logging

Consistent logs via logcat with one tag per category: `VQC.app`, `VQC.content`, `VQC.consent`, `VQC.player`, `VQC.debug`. Events mirror the iOS app (launch, consent selection, content load start/complete/fail, item opened, playback requested/buffering/started/paused/failed, retry, debug mode changes).

```bash
adb logcat -s VQC.app VQC.content VQC.consent VQC.player VQC.debug
```

## BrowserStack

Upload the debug APK to App Automate (replace the credential placeholders; never commit real credentials):

```bash
curl -u "YOUR_USERNAME:YOUR_ACCESS_KEY" \
  -X POST "https://api-cloud.browserstack.com/app-automate/upload" \
  -F "file=@app/build/outputs/apk/debug/app-debug.apk"
```

Use the returned `app_url` (`bs://...`) as the `appium:app` capability. The app has no login and no network dependency.

## Known limitations

- All six content items share one bundled 30-second sample video; the durations shown in the list are catalogue metadata, not the real asset length. The player shows the real asset duration (00:30).
- Buffering and playback errors are simulated deterministically in the app (a local file never buffers); this is intentional so tests are reliable.
- Thumbnails are generated gradients, not real imagery.
- Portrait only; no tablet layout, localization, offline downloads, PiP, or DRM.
- If the video mode is changed in debug options while a detail page is already open, the new mode applies the next time a detail page is opened.
- Process death resets in-app navigation to the overview (persisted state such as consent, debug modes, and playback progress is unaffected).

## Project structure

```text
bin/                                  Prebuilt example debug APK
app/src/main/java/com/videoqa/challenge/
├── MainActivity.kt         Entry point, reads intent extras
├── AppContainer.kt         Root state and shared services
├── model/                  Content item, player state, debug mode enums
├── data/                   Persistence, repository, debug configuration, launch arguments
├── viewmodel/              Content list and player state machines
├── ui/                     Compose screens (consent, overview, detail, player, debug)
└── util/                   Logging
app/src/main/assets/content.json      Bundled catalogue (identical to iOS)
app/src/main/res/raw/sample_video.mp4 Bundled sample video (identical to iOS)
app/src/androidTest/                  Build-verification smoke tests (not part of the assignment)
```
