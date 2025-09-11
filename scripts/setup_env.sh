#!/usr/bin/env bash
set -euo pipefail

# Simple environment check script for building DevInfo without Android Studio.

if ! command -v java &>/dev/null; then
  echo "[ERROR] 'java' not found in PATH. Install JDK 17 and/or export JAVA_HOME." >&2
  exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -n1)
echo "Java: $JAVA_VER"

if ! command -v adb &>/dev/null; then
  echo "[ERROR] 'adb' not found. Install Android Platform Tools and add to PATH." >&2
  exit 1
fi

if ! command -v unzip &>/dev/null; then
  echo "[WARN] 'unzip' not found. Some Gradle dependency extractions may fail." >&2
fi

echo "Checking connected devices..."
adb devices

echo "Environment looks OK. Use VS Code tasks or run: ./gradlew :app:assembleDebug"
