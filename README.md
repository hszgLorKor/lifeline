# Lifeline — offline multilingual emergency assistant (Build Prompt v3.1 "Lifeline")

Privacy-first, fully offline Android app (Kotlin + Jetpack Compose, minSdk 29, targetSdk 36)
for travelers/expats. Stores the medical profile locally, encrypted; in an emergency it dials
the resolved regional emergency number and communicates the profile via (1) TTS read-aloud on
the alarm stream, (2) a large-type responder view, (3) SMS to designated contacts in each
contact's language. All translation happens at profile-save time via bundled, versioned
language packs (lexicon + phrase templates, no neural MT). **No INTERNET permission** — the
offline promise is enforced by manifest inspection (`tools/verify_offline.sh`).

## Build
```
export JAVA_HOME=<jdk17> ANDROID_HOME=<sdk>
./gradlew :app:assembleDebug :app:assembleRelease :app:bundleRelease
./gradlew :app:testDebugUnitTest          # 30 unit tests
# instrumented: install app-debug.apk + app-debug-androidTest.apk on an emulator, then
adb shell am instrument -w org.lifeline.emergencyassistant.debug.test/androidx.test.runner.AndroidJUnitRunner
```
Release signing via env: LIFELINE_KEYSTORE, LIFELINE_KEYSTORE_PASSWORD, LIFELINE_KEY_ALIAS, LIFELINE_KEY_PASSWORD.

## Layout
- `app/src/main/java/org/lifeline/emergencyassistant/` — pack loader, translation pipeline,
  geo/resolution, crypto store, fall detector, SMS segmentation, E.164, services, UI, flows.
- `app/src/main/assets/packs/` — 10 language packs (en, zh-CN, hi, es, fr, ar, bn, pt, ru, ur),
  all `reviewStatus: UNREVIEWED`, sign-off `Pending` (owner gate G1).
- `app/src/main/assets/data/` — countries.json (languages + emergency numbers, cited),
  countries.geojson (Natural Earth 50m), places.json (GeoNames >= 50k).
- `tools/generate_packs.py` — regenerates packs/corpora/sign-off.
- `docs/` — TEST_REPORT.md, SIZE_BUDGET.md, DEPENDENCY_MANIFEST.md, PRIVACY_OFFLINE.md,
  `play/` (SEND_SMS declaration, FGS declaration, demo video script, data safety form),
  `protocols/14B_launch_gates.md` (owner-executed gates with harnesses).
- `docs/signoff/<lang>.json` — per-language sign-off records.

## Verified build-start facts (2026-10-31)
- Google Play target API for new apps: **API 36 (Android 16)** since 2026-08-31 → targetSdk 36.
- Play AAB base-module compressed download limit: **200 MB** (owner-resolved hard cap this
  session; the prompt's 400 MB cap treated as unreachable). Measured: 4.13 MB (arm64/API36).
