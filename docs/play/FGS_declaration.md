# Play Foreground Service Type Declaration (draft)

**Service:** FallDetectionService
**Declared types:** health | specialUse (AndroidManifest), with
PROPERTY_SPECIAL_USE_FGS_SUBTYPE property carrying the written justification:
"Fall detection: continuously samples the accelerometer to detect a fall and, on
detection, runs a countdown that may dispatch emergency SMS, open the dialer and read the
medical profile aloud. No data leaves the device except the emergency SMS/call initiated
by the user flow."

Rationale: Android 14+ health FGS type requires a Play health-apps declaration. If the
health declaration is not accepted for this use case, the specialUse type with the written
justification above is used (per build prompt Section 1). The short-lived
EmergencyFlowService (manual call flow, hold-completion to call-idle) declares specialUse
with its own justification property.

**Service:** EmergencyFlowService — specialUse:
"Short-lived emergency flow: observes call state during a manual emergency call and
performs post-call actions. No network use."

Both services post a persistent, user-visible notification while running and stop when the
flow ends (fall service persists only while fall detection is enabled).
