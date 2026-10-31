# Lifeline — Demo Video Script & Recording Checklist (Play SEND_SMS declaration)

## Script (target ~90 seconds, 1080p, no real personal data — use test profile)
1. [0:00] Launch app, onboarding: mother-tongue selection (show English + one more).
2. [0:08] Permission explainers (explain-first UI), grant SMS/phone/location/notifications.
3. [0:20] App-lock setup (biometric/PIN).
4. [0:25] Enter test medical profile (name "Demo User", blood type A+, allergy: peanut,
   condition: asthma, medication: metformin). Save -> "Saved + translated".
5. [0:40] Add one emergency contact "Helper" +1 555 0100, language = mother tongue.
6. [0:48] Home screen: resolved country + number (set via country override to a demo
   country), language chip.
7. [0:55] Press-and-hold the red EMERGENCY button 3 s -> dialer opens pre-filled with the
   resolved emergency number. (Do NOT tap call on a real number; use the emulator's
   simulated telephony or stop recording before tapping.)
8. [1:10] Simulated off-hook: read-aloud starts on the alarm stream (audible in recording);
   heads-up notification shows Stop read-aloud / Replay.
9. [1:25] SMS dispatched to the test contact: show the notification "SMS sent" and the
   message content in the messaging app (medical summary + coordinates + place + time).
10. [1:40] Fall flow: trigger synthetic fall (or settings test hook) -> full-screen overlay
    with 30 s countdown -> tap "I'm OK — cancel" (shows event log entry). Then re-trigger
    and let it expire -> SMS + dialer + responder view over lock screen.
11. [1:55] Responder view in a second language (language switch in one tap).
12. [2:05] End card: offline privacy statement ("No internet permission. All data on-device,
    encrypted.").

## Recording checklist
- [ ] Emulator or test device, airplane mode ON for the whole recording except the
      OS-level TTS voice download step if shown (label it as the only user-initiated
      connectivity exception).
- [ ] Test profile only; no real names/numbers. Contact number is a reserved test number.
- [ ] Never dial a real emergency number.
- [ ] Show the in-app SMS justification explainer before the system dialog.
- [ ] Show the "SMS sent" notification and the actual SMS in the Messages app.
- [ ] Show countdown cancel and expiry paths.
- [ ] 1080p, portrait, system UI visible (status bar), no personal data in notifications.
- [ ] Upload as the SEND_SMS demo video in the Play permission declaration form.
