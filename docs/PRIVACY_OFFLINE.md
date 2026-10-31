# Lifeline — Offline Privacy Document (in-app "About / privacy")

## What Lifeline is
An offline emergency assistant. It stores your critical medical data on this device only,
encrypted, and helps you reach the right emergency number and communicate your medical
profile in three channels: read-aloud during the call, a large-type responder view, and an
SMS to your designated emergency contacts.

## The offline promise
- The app does **not** hold the Internet permission. It cannot open sockets, so it cannot
  leak data over the network. The emergency dialer, SMS, and OS-level GPS work without it.
- There is no account, no sync, no server, no analytics, no ads, no tracking, no crash
  reporting to anyone.
- The **only** things that ever leave the device are the emergency phone call and the
  emergency SMS you trigger (or that the fall countdown triggers for you).
- The only user-initiated connectivity anywhere is the OS-level "download TTS voice" action
  you can start from system settings; that is an Android system feature, not an app network
  call.

## Where your data lives and how it is protected
- Everything is stored locally, encrypted at rest with AES-256-GCM keys in the Android
  Keystore (hardware-backed; StrongBox where available). Two separate keys:
  1. A **data-encryption key** that protects all stored data. By design it is *not*
     invalidated when you enroll a new fingerprint, and it does not require authentication
     for the app to use it. Its purpose is protection of data at rest against extraction
     from the device. The app lock is the runtime gate on human access. This consequence is
     deliberate.
  2. An **app-lock key** bound to your biometrics; enrolling a new fingerprint invalidates
     it and forces re-authentication (your app PIN remains available as fallback).
- App lock (biometric or app-specific PIN) protects the profile and settings screens. The
  responder view, the emergency call flow, and the fall overlay are always reachable
  without unlocking, so a responder can help you.
- All app screens use FLAG_SECURE: nothing medical appears in screenshots or Recents.
  Exception: if you enable the opt-in "include medical summary in the alert notification"
  toggle, that summary is then visible on the lock screen without unlocking — the toggle
  says so.
- No cloud backup (allowBackup=false). Cleartext traffic is disabled at the network
  security config level as belt-and-braces.

## What we collect and why (data minimization)
Only the fields you enter: identity, medical entries, contacts, notes. Health data is a
special category under GDPR Art. 9: we ask for your **explicit consent** before creating a
profile. Declining is fully supported — the app then runs in a degraded mode with a banner,
and you can consent later in Settings. Your emergency contacts' phone numbers are
third-party personal data and are treated as such.

## Your rights, in-app
View your data; export a plain-text UTF-8 copy via the system save dialog; delete
individual fields or entries; delete everything (crypto-erase: keys destroyed first, then
all stores deleted — unrecoverable by design).

## Crash log
A local, encrypted crash log exists for stability. It contains only stack traces,
timestamps and flow-step flags — never any medical or personal values (enforced by tests).
It is never transmitted; you can view and export it from Settings.

## Device-lock boundary
The app cannot bypass the Android device lock screen. After a fall countdown expiry, the
responder view is staged over the device lock screen via a full-screen-intent notification
plus setShowWhenLocked, so a bystander can read it on a PIN-locked phone. In the manual flow,
the responder view is reachable only when the device is unlocked.

## Data sources
- Country boundaries: Natural Earth admin-0, 50 m (public domain), pinned at build time.
- Place names: GeoNames (CC BY 4.0), cities >= 50,000 population, pinned at build time.
- Country → official/default regional language: CIA World Factbook listings, accessed
  2026-10-31; best-effort, largest listed speaker share, listing-order tie-break. This
  mapping carries a data caveat: near borders the resolved country may be wrong — confirm
  the displayed country when traveling.
- Emergency numbers: curated from the EU 112 register and national regulator lists;
  bundled best-effort with per-entry last-verified dates. Verify locally when traveling.

## Disclaimers
Lifeline is **not a medical device**. Translations are machine-assisted and may be
inaccurate — never rely on them as the sole source. Emergency numbers are bundled
best-effort data. The app cannot guarantee rescue response.
