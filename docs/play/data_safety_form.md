# Play Data Safety Form — drafted content (org.lifeline.emergencyassistant)

## Data collected: YES — but only stored locally, never transmitted by the app
The app declares NO internet permission and contains no network code. Data never leaves the
device except (a) the emergency SMS to the user's own designated contacts and (b) the
emergency phone call, both user-initiated flows.

| Data type | Collected | Shared/transmitted | Purpose | Retention |
|---|---|---|---|---|
| Personal info: name, date of birth | Yes | Only inside emergency SMS/call flow | Emergency identification | Until user deletes |
| Health & fitness: allergies, conditions, medications, implants, blood type, notes | Yes (Art. 9 special category) | Only inside emergency SMS/call flow | Emergency medical communication | Until user deletes; delete-all = crypto-erase |
| Contacts: emergency contact names, phone numbers, doctor | Yes | Only inside emergency SMS | Emergency notification | Until user deletes |
| Location (GPS, coarse) | Yes, only at app open and at trigger | Only inside emergency SMS as coordinates | Emergency location | Not stored beyond last-known use |
| Diagnostics: local encrypted crash log (stack traces, flow flags only, no medical/personal content) | Yes | Never transmitted; user-exportable via Settings | App stability | Until user clears |

## Security
- Data encrypted at rest with AES-256-GCM via Android Keystore (hardware-backed, StrongBox
  where available). Two keys: data-encryption key (survives biometric re-enrollment by
  design — protects data at rest; app lock is the runtime gate) and biometric-bound
  app-lock key (invalidated on new enrollment; PIN fallback persists).
- App lock (biometric strong + app-specific PIN) on profile/settings; emergency surfaces exempt.
- FLAG_SECURE on all app screens; allowBackup=false; no cloud backup; networkSecurityConfig
  cleartextTrafficPermitted=false; no INTERNET permission.
- Explicit GDPR Art. 9 consent before profile creation; declining = supported degraded mode.

## Data deletion
Per-field deletion, per-entry deletion, and "Delete all data" with typed DELETE confirmation
performing crypto-erase (Keystore keys destroyed first, then all stores deleted; unrecoverable).
Local plain-text export available to the user via the system save dialog.

## Other declarations
- No ads. No third-party SDKs that share data. No analytics. App is not "primarily directed
  at children". Not a health/fitness tracking app (fall detection is an emergency trigger,
  not activity tracking).
