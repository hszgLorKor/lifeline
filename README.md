# Lifeline — offline multilingual emergency assistant

## What it is and what it's for

Lifeline is a **privacy-first, fully offline Android app** for travelers and expatriates whose
mother tongue differs from the official language of the country they are in.

It stores your critical medical data **locally on your device, encrypted**, and in an emergency
it helps you do three things:

1. **Call the right emergency number** for the country you are actually in (resolved offline
   from GPS → last-known location → SIM country → manual picker).
2. **Communicate your medical profile in the local language**, in three channels:
   - **TTS read-aloud** over the loudspeaker during the emergency call (alarm stream, so it is
     not ducked by the call),
   - a **large-type responder view** on the screen you can hand to a responder,
   - an **SMS to your designated emergency contacts** with a short medical summary, GPS
     coordinates, a local place name, and a timestamp — each contact gets it in their assigned
     language.
3. **Detect falls automatically**: a foreground service watches the accelerometer; on a detected
   fall it runs a full-screen countdown (alarm sound even in silent mode) that you can cancel,
   and on expiry it sends the SMS, opens the dialer pre-filled with the emergency number, and
   stages the responder view over the lock screen so a bystander can read it.

All translation happens **in advance, when you save your profile** — never at emergency time.
Translation is lexicon + sentence templates from bundled language packs (no neural MT, no
network). English is always installed as the fallback.

## The offline promise

- The app **does not hold the Internet permission** — it physically cannot open sockets.
  This is verified by manifest inspection in the build (`tools/verify_offline.sh`).
- No accounts, no sync, no server, no analytics, no ads, no tracking, no crash reporting to anyone.
- The **only** things that ever leave the device are the emergency phone call and the emergency
  SMS. The only user-initiated connectivity anywhere is the OS-level "download TTS voice" action
  in Android system settings.
- All data is encrypted at rest with AES-256-GCM keys in the Android Keystore (hardware-backed,
  StrongBox where available). App lock = biometric + app-specific PIN. Screenshots/Recents are
  blocked (FLAG_SECURE). No cloud backup. "Delete all data" is a crypto-erase (keys destroyed
  first) and is unrecoverable by design.
- Health data is GDPR Art. 9: you give explicit consent before a profile is created; declining
  is a supported degraded mode.

## Languages (v1)

English, Mandarin Chinese (zh-CN), Hindi, Spanish, French, Modern Standard Arabic, Bengali,
Portuguese, Russian, Urdu. All packs ship **UNREVIEWED** with sign-off **Pending** — native
speaker review with medical competence is a launch gate (see `docs/protocols/14B_launch_gates.md`).

## How to install / build it

### From source (developer)

Requirements: JDK 17, Android SDK with platform 36 + build-tools 36.0.0.

```bash
git clone https://github.com/hszgLorKor/lifeline.git
cd lifeline
git checkout vibe/lifeline-v31-ea5160
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug          # debug APK -> app/build/outputs/apk/debug/
./gradlew :app:assembleRelease        # release APK (unsigned unless signing env vars set)
./gradlew :app:bundleRelease          # AAB for Play -> app/build/outputs/bundle/release/
```

Release signing via environment variables: `LIFELINE_KEYSTORE`, `LIFELINE_KEYSTORE_PASSWORD`,
`LIFELINE_KEY_ALIAS`, `LIFELINE_KEY_PASSWORD`.

Install the debug APK on a device/emulator (min Android 10 / API 29):

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The two large data assets (country boundaries, place names) are regenerated from pinned public
sources if missing:

```bash
python3 tools/generate_geo_assets.py   # Natural Earth 50m + GeoNames >= 50k
python3 tools/generate_packs.py        # language packs, corpora, sign-off records
```

### From Google Play (end user, once published)

Lifeline is distributed **only via Google Play**. The store listing will appear after the owner
completes the launch gates (Play permission declarations for SEND_SMS / foreground service,
demo video, Data safety form, DPIA + legal review, trademark search) — see
`docs/protocols/14B_launch_gates.md`. Until then, install the debug APK as above.

### First run (onboarding)

1. Pick your **mother tongue** (the app UI and your profile input follow it).
2. Grant permissions with in-app explainers (location, SMS, phone state, notifications,
   full-screen intent, sensors, vibration).
3. Set up **app lock** (biometric, or app PIN if your device has no lock screen).
4. Enter your **medical profile** (identity, blood type, allergies, conditions, medications,
   implants, doctor, emergency contacts with per-contact SMS language, notes). Saving encrypts
   and pre-translates into all installed packs.
5. Check **TTS voice availability** — the app deep-links to system TTS settings so you can
   pre-download offline voices (the only connectivity the app ever points you to).
6. Read the emergency-number disclaimer and grant (or decline) **GDPR consent**.

## How to test it

### Automated (already run by the agent, all passing)

```bash
./gradlew :app:testDebugUnitTest      # 30 JVM unit tests
# instrumented (needs an emulator or device):
./gradlew :app:installDebug :app:installDebugAndroidTest
adb shell am instrument -w org.lifeline.emergencyassistant.debug.test/androidx.test.runner.AndroidJUnitRunner
# -> OK (9 tests): fall signatures, daily-life negatives, SMS rate limit, dialer prefill,
#    encryption-at-rest, geo/place lookup
```

Full results and environment: `docs/TEST_REPORT.md`.

### Manually on an emulator or test phone

1. Install the debug APK, complete onboarding with a **test profile only** (no real personal
   data; use a reserved test number for the emergency contact — never a real emergency number).
2. **Home screen**: check resolved country + emergency number (set via Settings → country
   override) and the active regional language chip.
3. **Manual emergency flow**: press-and-hold the red button 3 s → the dialer opens pre-filled.
   On an emulator you can simulate the call going off-hook; the app then starts the read-aloud
   (alarm stream) with Stop/Replay in the heads-up notification, and sends the contact SMS
   (watch for the "SMS sent" notification and the message in the Messages app).
4. **Fall flow**: trigger a synthetic fall (instrumented harness) or drop the test device from
   ≥ 1 m onto a padded surface → full-screen countdown → either tap "I'm OK — cancel" or let
   it expire (SMS + dialer + responder view over the lock screen).
5. **Responder view**: open from the home card; switch language in one tap; tap "Read aloud".
6. **Settings**: toggle fall detection / per-step / manual SMS, change countdown, app lock,
   delete-all (crypto-erase), event log, crash log export, data sources.
7. **Offline check**: enable airplane mode and repeat 2–5 — everything must still work.

### Physical / owner-run gates (NOT done by the agent)

Native pack review, physical fall tests (≥95% typical-fall trigger rate at Medium), 7-day
false-positive corpus, battery ≤3%/24h on Pixel 6a, device matrix (API 29 + Android 14/15 +
Samsung/Xiaomi), live-call intelligibility with a helper phone (≥90% key facts in quiet),
Play readiness. Protocols and harnesses: `docs/protocols/14B_launch_gates.md`.

## Repository layout

```
app/src/main/java/org/lifeline/emergencyassistant/   # all Kotlin sources (pack, translate, geo, crypto, fall, sms, tts, service, ui, flow)
app/src/main/assets/packs/                           # 10 language packs (JSON, versioned schema)
app/src/main/assets/data/                            # countries.json, countries.geojson, places.json
app/src/main/assets/corpus/                          # per-language self-check corpora
app/src/test/                                        # 30 JVM unit tests
app/src/androidTest/                                 # 9 instrumented tests (emulator)
tools/                                               # pack + geo asset generators, offline manifest check
docs/                                                # TEST_REPORT.md, SIZE_BUDGET.md, DEPENDENCY_MANIFEST.md,
                                                     # PRIVACY_OFFLINE.md, play/*, protocols/14B_launch_gates.md, signoff/
```

## Verified build-start facts (2026-10-31)

- Google Play target API for new apps: **API 36 (Android 16)** — in effect since 2026-08-31.
- Play AAB base-module compressed download limit: **200 MB** (owner-resolved hard cap); measured
  build: **4.13 MB**.

## Disclaimers

Lifeline is **not a medical device**. Translations are machine-assisted and may be inaccurate —
never rely on them as the sole source. Emergency numbers are bundled best-effort data; verify
locally when traveling. Near borders the resolved country may be wrong — confirm the displayed
country. The app cannot guarantee rescue response.
