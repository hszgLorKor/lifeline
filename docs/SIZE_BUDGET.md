# Lifeline — Per-Asset Size Budget (build-start, 2026-10-31)

Owner decision (this session): hard cap = **200 MB compressed download of the AAB base
module** (Google Play verified limit for new submissions, checked 2026-10-31). The
prompt's 400 MB absolute cap is treated as unreachable; everything ships in the base module.
Optimization target: <= 200 MB. Current measured state: **base-module compressed download
≈ 4.13 MB (arm64-v8a, API 36 device spec), universal ≈ 8.69 MB** — far under target.

| Asset / component | Raw size | Compressed contribution | Notes |
|---|---|---|---|
| countries.geojson (Natural Earth 50m, ISO_A2-only props) | 2.20 MB | ~0.35 MB | point-in-polygon geodata |
| places.json (GeoNames >= 50k pop, 12,396 places) | 1.39 MB | ~0.25 MB | reverse geocoding for SMS place line |
| countries.json (252 entries, languages + emergency numbers) | 96 KB | ~20 KB | resolution DB |
| 10 language packs (packs/*.json) | ~115 KB total | ~30 KB | lexicon + templates + SMS + TTS |
| 10 corpora (corpus/*.json) | ~120 KB | ~30 KB | self-consistency test data (dev-only? shipped for self-check) |
| Kotlin/Compose code + AndroidX + SQLCipher native (arm64) | — | ~3.2 MB | R8-shrunk release |
| **Total measured (bundletool get-size, device spec arm64/420dpi/API36)** | | **4.13 MB** | |
| **Total measured (universal APK)** | | **8.69 MB** | |

Cut ladder (pre-authorized, Section 11 [D24]) — only if the 200 MB base-module cap is ever
threatened: (1) R8/resource shrinking + ABI splits (already on); (2) place-name cutoff
50k → 100k → 250k → 500k population; (3) reduce per-pack transliteration coverage;
(4) last resort: drop language packs in reverse speaker order, never English.
Nothing was cut: current build is ~2% of the 200 MB budget.
