#!/usr/bin/env bash
# Build, install, and launch DevInfo debug build on a connected device/emulator.
# Usage:
#   ./scripts/run_debug.sh            # build, install, launch, tail logs
#   ./scripts/run_debug.sh --no-logs  # skip logcat tail
set -euo pipefail

SKIP_LOGS=false
if [[ ${1:-} == "--no-logs" ]]; then
  SKIP_LOGS=true
fi

need() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERROR] Required command '$1' not found in PATH" >&2
    exit 1
  fi
}

need java
need adb

# Show java version for sanity
java -version 2>&1 | head -n1

# Ensure at least one device/emulator is connected
if ! adb get-state >/dev/null 2>&1; then
  echo "[INFO] No active device state yet. Listing devices:" >&2
  adb devices
  echo "[ERROR] No device/emulator detected. Start an emulator or plug in a device with USB debugging enabled." >&2
  exit 1
fi

echo "[STEP] Assembling debug APK"
./gradlew :app:assembleDebug

echo "[STEP] Installing debug APK"
./gradlew :app:installDebug

echo "[STEP] Launching main activity"
adb shell am start -n com.sakib.devinfo/.ui.MainActivity || true

echo "[INFO] APK path: app/build/outputs/apk/debug/app-debug.apk"

if ! $SKIP_LOGS; then
  echo "[STEP] Tailing DevInfo logs (Ctrl+C to exit)"
  adb logcat -s DevInfo
else
  echo "[DONE] Skipped log tail. Use: adb logcat -s DevInfo"
fi
