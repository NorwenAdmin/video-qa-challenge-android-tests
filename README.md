# Video QA Challenge (Android) — Test Automation Report

## Motivation

**Platform:** Android over iOS — more hands-on experience with the Android
toolchain, faster/more reliable environment setup.

**Framework:** Espresso / Compose UI Test — preferred tool, natural fit for a
Jetpack Compose app.

**Pattern:** Robot Pattern — my default for mobile testing, more practical
than Page Object for this kind of work.

**Locator strategy:** testTag-based matching. My default habit is usually
semantics-first (`hasText`/`hasContentDescription`), since in my past experience
team conventions generally discouraged testTag. Here I switched after inspecting
the source: the app explicitly exposes testTags as resource-ids
(`testTagsAsResourceId = true` in `VideoQAApp.kt`), intentional for UiAutomator/
Appium. That's the API this app was built to be tested with, so I matched the
approach it was designed for instead of forcing my usual habit.

---

## How to run

1. Clone https://github.com/NorwenAdmin/video-qa-challenge-android-tests
2. Ensure JDK 17–21 is used (not 25) — set via `gradle.properties`:
   `org.gradle.java.home=<path-to-jdk17-or-21>`
3. `export ANDROID_HOME=<your SDK path>`
4. `./gradlew connectedAndroidTest`
   (runs on a connected emulator/device — Pixel 6 / API 35 recommended)

---

## Test execution report

**Environment:** Local Android emulator (Pixel 6, API 35, per AUT README).

**Why:** No physical device on hand; for a single-run assignment, a device
cloud (BrowserStack App Automate — which does support native Espresso suites
via its own API, separate from the Appium flow documented in the AUT README)
adds upload/setup overhead without real benefit for a one-off run. Emulator
gives faster iteration and full control over launch args/animations.

**Execution time:** Full suite (`./gradlew connectedAndroidTest`) completes in 56s.

**Time tracked:**
- Planning and architecture (source reading, decisions, test design): ~1–2h
- Writing the suite and fixing tests against the real device: ~1h
- Report refinement (following session): ~30min–1h

---

## Project structure

```text
app/src/androidTest/java/com/videoqa/challenge/
├── base/                    Shared test infrastructure
│   ├── BaseComposeTest.kt   ComposeTestRule + launchApp() with intent extras
│   ├── BaseRobot.kt         click() with built-in wait-until-clickable
│   └── BaseVerification.kt  assertExists() with built-in wait-until-exists
├── robots/                  One Robot + Verification pair per screen
│   ├── ConsentRobot.kt
│   ├── OverviewRobot.kt
│   └── DetailRobot.kt       Covers detail metadata and the player
│                            (PlayerSection renders inline within DetailScreen)
├── util/                    Shared test utilities
│   ├── ComposeSyncUtils.kt  waitUntilExists / waitUntilClickable / waitUntilState
│   ├── TestTimeouts.kt      Named, justified timeout constants
│   └── TestFixtures.kt      Video content used across tests
└── tests/
    ├── HappyPathTest.kt     Mandatory flow + full player state machine
    └── RiskScenariosTest.kt Error, buffering, completion, and negative-path scenarios

TESTING_REPORT.md            Motivation, execution report, AI usage note, test plan
```

---

## AI usage note

**Tools used:** Claude, throughout planning and implementation. Claude Code
for running the final suite and formatting the output.

**How:** Used as a copilot for architecture decisions, code review, and
debugging stack traces — not as an autonomous agent generating the suite
end-to-end. I supplied the actual AUT source files and live-device output at
each step; decisions were made against what the code and the running app
actually showed, not assumptions.

**Representative prompts / examples:**

1. *"Сначало нужно дождпться окончания буфера а потом проверять"* ("The
   buffering has to finish before we check") — caught a race condition in a
   draft error-state test that asserted on error UI immediately after tapping
   play, without waiting for the Buffering → Error transition first.

2. *"Я не вижу смысла в val player"* ("I don't see the point of `val player`")
   — pushed back on an unnamed fluent chain that silently switched Robot types
   mid-test (DetailRobot → PlayerRobot after `.startPlayback()`). Led to
   merging PlayerRobot into DetailRobot entirely, since both represent the
   same physical screen in this app.

3. *"Нет я бы добавил желательно сделать другой механизм так как три секунды
   это просто простой"* ("I'd flag that a different mechanism is needed — 3
   seconds is just dead time") — after finding the `completeQuickly` mode
   still requires ~3s of real wait, rejected simply padding the test timeout.
   Reframed it as a product-level finding: this debug mode doesn't behave
   consistently with Error/Buffering, which render state instantly.

4. *"Я бы сначала спросил а потом фиксить, вдруг они специально оставили"*
   ("I'd ask first before fixing — it might be intentional") — when discussing
   the untagged metadata row, rejected the idea of silently patching the AUT
   even as a "senior fix," in favor of raising it with the team first — same
   principle later confirmed by the source comment marking it intentional.

**How I evaluated the output:** Rejected/corrected AI suggestions several
times — caught a case where a proposed fix increased a timeout without
addressing the root cause (missing scroll-to-node before querying a
not-yet-composed item); insisted on human-verified device behavior over AI's
initial guesses about internal timing (buffering duration, completion mode).

**Additionally:** Used Claude Code to run the final test suite and format
(beautify) the execution output for the test execution report.

---

## Test Plan & Next Steps

**Covered:**
- Mandatory flow (consent → overview → detail → playback → Playing)
- Full player state machine: Buffering, Playing, Paused, Error, Completed
- Navigation round-trip: detail → back → open a different video
- Consent reject-optional path
- Play button disabled during buffering (`enabled=` condition from source)
- Content-level error state on Overview
- Overview video card content (title, category, published date) via merged
  semantics matching

**Next scenarios, in priority order:**
1. Content empty state (`contentMode=empty`) — mirrors the error-state test,
   quick to add given existing infrastructure
2. Playback resume from saved position (README: pause saves position, reopen
   resumes) — meaningful behavior not yet covered
3. Content slow-loading state (`contentMode=slow`) — lower priority, same UI
   pattern as success, only differs by delay

**Risks / open questions:**
- Metadata row on Detail (date/duration) intentionally has no testTag (source
  comment); matched via merged-node text search rather than modifying the AUT.
- User-facing strings (e.g. consent copy) are hardcoded in composables rather
  than `strings.xml` — no shared source of truth between app and test matchers.
  Recommend moving to resources; kept substring matching to reduce fragility.
- `completeQuickly` video mode still requires ~3s of real wait (seeks near end
  of asset rather than skipping playback). Suggest a true instant-completion
  debug state for consistency with Error/Buffering, which render their state
  without waiting on real player mechanics.
- Preferences (analytics/personalisation) toggle values have no UI-visible
  confirmation path — expected, since no visual change should follow from a
  privacy choice. Recommend keeping UI coverage to a screenshot test on the
  save/transition flow, and covering the actual persistence logic with unit
  tests owned by the dev team rather than testing storage through the UI layer.
- Test locators (testTags like `"video_play_button"`, `"content_item_${id}"`)
  are hardcoded as string literals on both the app side (`Modifier.testTag(...)`)
  and the test side (`hasTestTag(...)`), with no shared source of truth. A
  rename on either side breaks the other silently at runtime, not at compile
  time. Would suggest the dev team maintain testTag values in a shared
  constants file/object (similar to how string resources centralize UI copy),
  so both app and test code reference the same source and a rename becomes a
  compile-time break instead of a runtime test failure.
