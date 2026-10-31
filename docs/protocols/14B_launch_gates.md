# Section 14-B — Owner-Executed Launch Gates: Harnesses & Protocols

The agent does NOT mark these as done. Each protocol below is delivered for the owner to run
and record. Nothing here is agent-verified physically.

## G1. Native language review (D6)
- Run: for each of the 10 packs (`app/src/main/assets/packs/*.json`), a native speaker with
  medical competence reviews lexicon, templates, TTS script, SMS template.
- Update `docs/signoff/<lang>.json`: reviewStatus UNREVIEWED → APPROVED, signOff.status
  Pending → Approved with reviewer + date.
- Gate: all 10 Approved.

## G2. Fall detection physical tests (D30)
- Harness: `adb shell am instrument` runs `FallDetectionHarnessTest` (androidTest) which can
  be driven by synthetic traces; physical protocol:
  - Typical falls: ≥ 1 m drops onto padded surface, device in pocket and in hand — ≥ 95%
    trigger rate at Medium = GATE.
  - Atypical (reported, not gated): soft collapse onto mattress, stair tumble, slow slide
    from chair — per-type trigger rates recorded.
- Report per-type rates and device model.

## G3. False-positive corpus
- Record ≥ 7 days continuous wear data from ≥ 3 participants (walking, stairs, car, train).
- Feed traces to `FallDetector` (unit harness `FallDetectorTest` patterns extended).
- Gate: < 1 false positive per week. Report must state sample size and its statistical
  weakness.

## G4. Battery (Section 11)
- Device: Pixel 6a, detection ON, 24 h.
- Protocol: `adb shell dumpsys batterystats` reset → 24 h → read; analyze with Battery
  Historian. Gate: ≤ 3% / 24 h.

## G5. Device matrix
- One minSdk-29 device/emulator + Android 14 + Android 15 devices.
- Samsung + Xiaomi: document OEM-killer survival (battery-optimization exemption flow,
  bundled per-OEM instructions text).
- Per device: verify alarm-stream TTS is audible during an off-hook call; devices that
  silence it are documented and responder view declared primary for that device.

## G6. Live-call intelligibility (D31)
- Protocol: place a real call to a helper phone (NEVER a real emergency number), quiet and
  noisy environments, while TTS read-aloud plays. Receiver transcribes key facts
  (name, blood type, allergies). Gate: ≥ 90% of key facts intelligible in quiet;
  noisy reported, not gated.

## G7. Play readiness
- SEND_SMS declaration submitted & approved (draft: docs/play/SEND_SMS_declaration.md).
- FGS type declarations accepted (docs/play/FGS_declaration.md).
- Data safety form published (docs/play/data_safety_form.md).
- Demo video recorded from docs/play/demo_video_script.md.
- DPIA completed; legal review completed (liability, fall-detection marketing claims,
  jurisdictional emergency-communication rules); trademark search on "Lifeline" completed.
