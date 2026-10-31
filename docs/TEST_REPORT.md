# Lifeline v3.1 — Test Report (agent-executed, 2026-10-31)

Environment: Temurin JDK 17.0.21, AGP 9.5.0, Gradle 9.6.0, compileSdk/targetSdk 36, minSdk 29.
Unit tests: JVM (`:app:testDebugUnitTest`). Instrumented tests: Android emulator API 29
(google_apis x86_64), `am instrument` via androidx.test.runner.AndroidJUnitRunner.

## Unit tests — 30 run, 30 passed, 0 failed (BUILD SUCCESSFUL, :app:testDebugUnitTest)
- CountryResolverTest (6): GPS-fresh precedence, stale/fresh last-known 24h rule, MCC fallback,
  manual override precedence, dual-SIM low-confidence, point-in-polygon ray casting.
- TranslationPipelineTest (6): lexicon template coverage, custom-entry Needs-review flagging and
  never-dropped, TTS script skips empty fields, SMS placeholder rendering, pack schema
  rejection on higher version, English identity.
- SmsSegmentsTest (5): GSM-7 160/153 segmentation, ASCII blood types stay GSM-7,
  UCS-2 70/67 for non-Latin, default EN template <= 2 segments.
- E164Test (4): US normalize, E164 stable, malformed rejected, country-picker default.
- FallDetectorTest (7): free-fall+impact triggers at Medium, impact+15s-stillness triggers at
  Medium, walking/stairs/train/car-bump daily-life traces do NOT trigger, sensitivity ±20%.
- ScrubbingTest (2): medical values detected in log messages, sensitive markers detected.

## Instrumented tests — 9 run, 9 passed (OK (9 tests)) on emulator API 29
- FallDetectionHarnessTest (3): staged free-fall-then-impact triggers at Medium;
  staged impact-then-stillness triggers at Medium; 21 daily-life seeds do not trigger.
- EmergencyFlowHarnessTest (6): manual-SMS 10-min rate limit enforced; manual-SMS toggle off
  blocks; dialer prefill uses resolved number (US=911, DE=112); encryption round-trip with
  no plaintext at rest (file bytes contain no profile values); geo lookup Paris=FR,
  Chicago=US, Tokyo=JP, Cairo=EG; place index finds Cairo/EG.

## Corpus self-check (SELF-AUTHORED — self-consistency only, NOT independent validation)
Per-language 500-term corpora under app/src/main/assets/corpus/*.json are self-authored by
the build agent (D6). Lexicon-backed coverage: en 100%; drafted packs (zh-CN, hi) cover the
drafted subset, remaining keys fall back to romanized pass-through and are flagged
UNREVIEWED — native medical review is owner launch gate G1 (docs/protocols/14B_launch_gates.md).
Corpus match rate vs lexicon keys is reported as a self-consistency check only.

## Offline proof (14-A)
- Merged manifest inspection (aapt2 dump xmltree, app-debug.apk): ZERO android.permission.INTERNET
  entries. Enforced by tools/verify_offline.sh. networkSecurityConfig cleartextTrafficPermitted=false.

## Encryption proof (14-A, emulator)
- encryptionRoundTrip_noPlaintextAtRest: stored file bytes contain no profile values; round-trip
  decrypts correctly. AES-256-GCM via Android Keystore (StrongBox attempted, TEE fallback).

## Size (14-A / Section 11)
- AAB app-release.aab = 11.1 MB. bundletool get-size: universal 8.69 MB compressed download;
  arm64-v8a/API36 device spec 4.13 MB. Both far under the 200 MB Play base-module cap
  (owner-resolved this session) and the 200 MB target. See docs/SIZE_BUDGET.md.

## NOT agent-verified (owner launch gates, harnesses delivered — docs/protocols/14B_launch_gates.md)
Native pack review; physical fall tests; false-positive corpus; battery 3%/24h on Pixel 6a;
device matrix incl. Android 14/15 + Samsung/Xiaomi; live-call intelligibility; Play readiness.
