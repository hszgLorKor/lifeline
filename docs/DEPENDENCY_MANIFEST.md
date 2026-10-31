# Lifeline — Dependency & License Manifest (build-start snapshot 2026-10-31)

Offline-only policy: the app does NOT declare `android.permission.INTERNET`. Verified by
manifest-merger inspection of the merged debug manifest (aapt2 dump xmltree): zero
`INTERNET` entries. No dependency in this list declares INTERNET or performs network I/O.
The build also enforces this via a CI check (see `tools/verify_offline.sh`).

| Dependency | Version | License | Network-inertness note |
|---|---|---|---|
| AGP (com.android.application) | 9.5.0 | Apache-2.0 | Build-time only; not shipped |
| Kotlin Gradle Plugin + Compose compiler plugin | 2.4.21 | Apache-2.0 | Build-time only; not shipped |
| Gradle wrapper | 9.6.0 | Apache-2.0 | Build-time only |
| androidx.core:core-ktx | 1.17.0 | Apache-2.0 | No INTERNET; no network APIs used |
| androidx.lifecycle:lifecycle-runtime-compose / viewmodel-compose | 2.10.0 | Apache-2.0 | Lifecycle only; inert |
| androidx.activity:activity-compose | 1.13.0 | Apache-2.0 | Inert |
| androidx.navigation:navigation-compose | 2.9.8 | Apache-2.0 | In-app navigation only; inert |
| androidx.compose BOM (ui, material3, foundation, icons-extended) | 2025.12.01 | Apache-2.0 | UI toolkit; inert |
| androidx.biometric:biometric | 1.1.0 | Apache-2.0 | Local biometric prompt; inert |
| androidx.security:security-crypto | 1.1.0 | Apache-2.0 | Local crypto helpers; inert |
| net.zetetic:android-database-sqlcipher | 4.5.4 | BSD-style (Zetetic LLC) | Local encrypted SQLite; bundles SQLCipher (BSD-3-Clause) — no network |
| androidx.sqlite:sqlite-ktx | 2.6.1 | Apache-2.0 | Local SQLite bindings; inert |
| com.googlecode.libphonenumber:libphonenumber | 9.0.42 | Apache-2.0 | Pure parsing library; inert |
| org.jetbrains.kotlinx:kotlinx-coroutines-core/android | 1.11.0 | Apache-2.0 | Inert |
| org.jetbrains.kotlinx:kotlinx-serialization-json | 1.9.0 | Apache-2.0 | Inert |
| junit:junit (test) | 4.13.2 | EPL-2.0 | Test only |
| org.robolectric:robolectric (test) | 4.17 | Apache-2.0 | Test only |
| androidx.test core/ext-junit/espresso/uiautomator/runner (androidTest) | 1.7.0 / 1.3.0 / 3.7.0 / 2.4.0 / 1.7.0 | Apache-2.0 | Test only |
| app.cash.turbine:turbine (test) | 1.2.1 | Apache-2.0 | Test only |

Data assets (bundled, cited in-app on "Data sources" screen):
- Natural Earth admin-0 countries 50m (ne_50m_admin_0_countries), public domain / ODbL-compatible, pinned 2026-10-31.
- GeoNames cities5000 dump (CC BY 4.0, https://www.geonames.org), filtered to population >= 50,000, pinned 2026-10-31.
- GeoNames countryInfo.txt (CC BY 4.0), pinned 2026-10-31.
- Country official-language mapping: CIA World Factbook country listings, accessed 2026-10-31 (public domain, US Gov work) — best-effort, data caveat shown in-app.
- Emergency numbers: curated from EU 112 register + national regulator lists, last_verified 2026-10-31.

No analytics, ads, crash-reporting, or tracking SDKs of any kind. No INTERNET permission.
