#!/usr/bin/env bash
# Fails if the merged manifest (app + all dependency AARs) declares INTERNET.
set -euo pipefail
APK="${1:-app/build/outputs/apk/debug/app-universal-debug.apk}"
AAPT="${AAPT2:-$(ls -d "$ANDROID_HOME"/build-tools/*/ | sort -V | tail -1)}aapt2"
COUNT=$("$AAPT" dump xmltree "$APK" AndroidManifest.xml | grep -c "android.permission.INTERNET" || true)
if [ "$COUNT" != "0" ]; then
  echo "OFFLINE VIOLATION: merged manifest declares INTERNET ($COUNT)" >&2
  exit 1
fi
echo "OK: no INTERNET permission in merged manifest ($APK)"
