# Play Permission Declaration — SEND_SMS (draft for Play Console)

**App:** Lifeline (org.lifeline.emergencyassistant)
**Permission:** android.permission.SEND_SMS (restricted)

## Core app functionality that requires SEND_SMS
Lifeline is an offline emergency assistant for travelers. Its core function is to notify
the user's designated emergency contacts by SMS when the user triggers an emergency flow
(manually via a 3-second hold on the emergency button, or automatically after a detected
fall and countdown expiry). The SMS contains a short medical summary, GPS coordinates, an
offline place name, and a timestamp, rendered in each contact's assigned language.

## Why this is a core use case (Play "emergency" allowance)
The SMS is sent only in an emergency situation to contacts the user explicitly designated,
solely to enable help to reach the user. It is not used for marketing, authentication OTPs,
or any non-emergency messaging.

## In-app justification flow (implemented)
Before the first SMS dispatch, the app shows an in-app explainer (in the user's language):
"Lifeline sends an SMS only in an emergency, only to your designated emergency contacts,
with your medical summary and location. Standard SMS rates of your carrier apply."
The system permission dialog follows. The user can disable emergency SMS in Settings
(manual flow) and per-step in the fall flow; a cancelled call sends nothing.

## Demo video script (see docs/play/demo_video_script.md)
Records: onboarding -> profile entry -> contacts -> manual emergency trigger -> off-hook
observation -> SMS dispatched to a test contact -> SMS content shown. Also shows the fall
flow: synthetic fall -> countdown -> cancel, and countdown expiry -> SMS + dialer.
