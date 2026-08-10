# Video QA Challenge (Android)

A small, deterministic Android demo app that simulates a simplified media product with a consent screen, a video content overview, content detail pages, and a video player with an explicit, inspectable state machine.

- App name: **Video QA Challenge**
- Application id: `com.videoqa.challenge`
- No login, no network, no external services. All content and video are bundled.
- Kotlin, Jetpack Compose, Media3 (ExoPlayer).

## App overview

| Screen | Purpose |
|---|---|
| Consent | Shown on first launch only. Accept all, reject optional, or manage preferences. |
| Preferences | Analytics and personalised-content toggles, saved locally. |
| Content overview | Deterministic list of six videos with loading, empty, and error states. |
| Content detail | Title, category, description, published date, and video preview with play button. |
| Video player | Custom ExoPlayer UI with play/pause/resume, progress, and a testable state label (`Buffering`, `Playing`, `Paused`, `Error`, `Completed`). |
| Debug options | Gear icon on the overview. Switches content/video response modes and resets app state. |

## Requirements

- JDK 17–24 (the bundled Gradle 8.14.2 wrapper does not run on newer JDKs)
- Android SDK with platform 35
- Min SDK 26 (Android 8.0), target/compile SDK 35
- Recommended emulator: Pixel 6, API 35
- Portrait orientation only

No Android Studio installation is required to build; the Gradle wrapper is included. Gradle downloads the required SDK platform automatically, but two things must be in place first:

1. Gradle must be able to locate your SDK, otherwise the build fails with `SDK location not found`. Point `ANDROID_HOME` at the SDK or set `sdk.dir` in `local.properties`:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"   # macOS default
export ANDROID_HOME="$HOME/Android/Sdk"           # Linux default
```

2. The SDK licenses must be accepted, otherwise the build fails with a licence error listing the unaccepted packages. Accept them once with `sdkmanager` (part of the `cmdline-tools` SDK package — install it via Android Studio's SDK Manager or from [developer.android.com](https://developer.android.com/studio#command-line-tools-only) if missing):

```bash
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses
```

## Build instructions

### Prebuilt example binary

A ready-to-use debug APK is committed at `bin/VideoQAChallenge-debug.apk`. To try the app you only need `adb` (part of the Android platform-tools) — no build setup required:

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

### Build a release APK

```bash
./gradlew assembleRelease
```

The release APK is unsigned by default. For BrowserStack, a debug APK works fine; if you need a signed release build, configure a signing config or sign with `apksigner` using your own keystore. No keystores are included in this repository.

## Testable flows

- **Consent** appears on first launch only; any selection persists across launches until reset.
- **Content overview** shows a loading indicator for a random 500–1500 ms, then always the same six items in the same order (first: `Amsterdam from above`, Travel, 02:30). At least one item is below the fold, so scrolling is required. Cards share the same visual structure; identify them by content id, never by list position.
- **Content detail** shows title, category, description, published date, and a video preview with a play button. The content id is exposed on the `detail_title` element twice: as its state description (visible to Compose UI tests) and as its content description (visible to UiAutomator/Appium, findable with the `accessibility id` strategy).
- **Video playback** transitions `Buffering → Playing` after the play button is tapped, with deterministic simulated buffering (~800 ms normal, ~6 s in Long buffering mode). The state is readable from `video_state_label`, which appears together with the player; the first observable state is `Buffering` (`Idle` exists in the state machine but is never shown, so do not assert on it). Pausing saves the position; reopening the same content resumes from it.
- **Empty / error states** switchable from debug options. Content empty state: "No videos are available". Content error state: the heading "Something went wrong" is plain text without a test id; the element `content_error_message` contains "We could not load the videos". Video error: `video_error_message` contains "Video could not be played", with a retry button.
- **Debug options** persist until changed or reset.

## Test identifiers (resource-id)

All identifiers are Compose test tags exposed as `resource-id` to UiAutomator, Appium, and BrowserStack (via `testTagsAsResourceId`). They have **no package prefix**: the resource-id is `consent_accept_button`, not `com.videoqa.challenge:id/consent_accept_button`.

**Important for Appium (UiAutomator2 driver):** by default the driver autocompletes bare ids with the package name, so `id=consent_accept_button` finds nothing. Either disable the autocompletion in your capabilities:

```json
"appium:disableIdLocatorAutocompletion": true
```

after which the plain `id` strategy works as documented (`$('id=consent_accept_button')` in WebdriverIO), or use a UiSelector locator, which is unaffected:

```js
$('android=new UiSelector().resourceId("consent_accept_button")')
```

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
| Detail | Preview play button | `video_play_button` (the same tag is reused by the player's play control) |
| Player | Surface and controls | `video_player`, `video_play_button`, `video_pause_button`, `video_buffering_indicator`, `video_progress`, `video_current_position`, `video_duration`, `video_state_label`, `video_error_message`, `video_retry_button` |
| Debug | Content modes | `debug_content_success`, `debug_content_empty`, `debug_content_error`, `debug_content_slow` |
| Debug | Video modes | `debug_video_normal`, `debug_video_buffering`, `debug_video_error`, `debug_video_complete_quickly` |
| Debug | State controls | `debug_reset_consent`, `debug_clear_progress`, `debug_reset_all`, `debug_restore_defaults`, `debug_done_button`, `debug_screen` |

Intentional exception (discussion point): the published date / duration metadata row on the detail screen is a single merged element without an identifier. It is non-critical and not needed for any mandatory flow.

## State reset and launch configuration

### In-app

Debug options → `Reset consent`, `Clear playback progress`, `Restore default settings`, `Reset all app state`.

### Intent extras

| Extra | Type | Effect |
|---|---|---|
| `resetAllState` | boolean (`--ez`) | Permanently clears **all** persisted state at launch. |
| `resetConsent` | boolean (`--ez`) | Permanently clears the persisted consent selection at launch. |
| `contentMode` | string (`--es`) | One of `success`, `empty`, `error`, `slow`. |
| `videoMode` | string (`--es`) | One of `normal`, `buffering`, `error`, `completeQuickly`. |
| `contentDelayMs` | int (`--ei`) | Fixed content loading delay in ms. Replaces the delay of **any** content mode, including the 5 s `slow` delay. |
| `videoBufferingMs` | int (`--ei`) | Fixed simulated buffering duration in ms. Replaces the mode default. |

The mode and delay extras apply to that application run only and are not written back to storage. The reset extras are destructive: they delete the corresponding persisted state.

Use `-S` to force-stop the app first so the extras apply to a fresh process. A complete, runnable example:

```bash
adb shell am start -S -n com.videoqa.challenge/.MainActivity --ez resetAllState true --es contentMode error --ei contentDelayMs 1000
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

Consistent logs via logcat with one tag per category. Logged events:

- `VQC.app` — app launch (with consent status), applied reset launch extras.
- `VQC.consent` — consent selection.
- `VQC.content` — content load started/completed/failed (failure at error level), content item opened.
- `VQC.player` — playback requested, buffering entered, started, paused (with position), resumed, completed, failed (error level), retry selected.
- `VQC.debug` — content/video mode changes, consent reset, playback progress cleared, all state reset.

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
- The debug video mode is captured when the player is created (the first play tap on a detail page). Changing the mode while a player is already active applies to the next player instance, not the current one.
- A saved playback position within the last second of the asset is ignored; the next playback starts from the beginning. Progress is also cleared when the video completes.
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
app/src/main/assets/content.json      Bundled content catalogue
app/src/main/res/raw/sample_video.mp4 Bundled sample video
```
