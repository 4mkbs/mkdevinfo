# DevInfo - Android Device Information App

A comprehensive Android application that provides detailed device information similar to DevCheck, built with Kotlin and Material Design 3.

## Features

### 📊 Dashboard

- Real-time CPU usage and frequency monitoring
- RAM usage tracking with live updates
- Storage space analysis
- Battery level, temperature, and voltage monitoring
- Network connectivity status
- System uptime display

### 🔧 Hardware Information

- Processor details (cores, frequencies, architecture)
- GPU specifications
- Camera specifications and capabilities
- Sensor information and live readings

### 📱 System Information

- Android version and build details
- Kernel version and architecture
- Security patch information
- ABI support details

### 🔋 Battery Monitor

- Battery health and capacity monitoring
- Charging status and type detection
- Temperature and voltage readings
- Background battery monitoring service

### 📡 Network Information

- Wi-Fi SSID and signal strength
- IP address and MAC address
- Mobile network details (SIM, LTE/5G)
- Data speed monitoring

### 📦 Apps Information

- Installed apps list (system + user)
- Package information and versions
- App storage size analysis

### 📷 Camera Details

- Camera count and specifications
- Supported resolutions and frame rates
- Aperture and feature information

### 🎯 Sensors

- Comprehensive sensor list
- Live sensor readings
- Sensor capabilities and ranges

### 🎈 Extra Features

- **Floating Overlay Monitor**: Real-time CPU, RAM, and battery monitoring overlay
- **Battery Monitor Service**: Background monitoring with notifications
- **Benchmarking Tools**: CPU, GPU, and storage performance tests
- **Utility Tools**: Flashlight, vibration test, screen test
- **Theme Support**: Light, dark, and system-adaptive themes

## Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture pattern with:

- **UI Layer**: Fragments with ViewBinding for each feature
- **Services**: Background services for monitoring and benchmarking
- **Utils**: System information fetchers and utilities
- **Themes**: Dynamic theme switching support

## Project Structure

```
app/
├── src/main/java/com/sakib/devinfo/
│   ├── ui/                     # UI components (Activities, Fragments)
│   │   ├── dashboard/         # Dashboard with real-time monitoring
│   │   ├── hardware/          # Hardware information display
│   │   ├── system/            # System information display
│   │   ├── battery/           # Battery information display
│   │   ├── network/           # Network information display
│   │   ├── apps/              # Apps information display
│   │   ├── camera/            # Camera information display
│   │   └── sensors/           # Sensors information display
│   ├── services/              # Background services
│   │   ├── FloatingOverlayService.kt    # Floating overlay monitor
│   │   ├── BatteryMonitorService.kt     # Battery monitoring
│   │   └── BenchmarkService.kt          # Performance benchmarking
│   ├── utils/                 # Utility classes
│   │   ├── SystemInfoUtils.kt           # System information fetchers
│   │   ├── PreferenceManager.kt         # App preferences
│   │   └── NotificationReceiver.kt      # Notification handling
│   ├── DevInfoApplication.kt  # Application class
│   └── MainActivity.kt        # Main activity with navigation
└── src/main/res/              # Resources (layouts, drawables, etc.)
```

## Permissions

The app requires the following permissions for full functionality:

- `ACCESS_NETWORK_STATE` - Network information
- `ACCESS_WIFI_STATE` - Wi-Fi details
- `READ_PHONE_STATE` - Device information
- `CAMERA` - Camera specifications
- `SYSTEM_ALERT_WINDOW` - Floating overlay
- `VIBRATE` - Vibration test
- `FLASHLIGHT` - Flashlight control
- `WAKE_LOCK` - Background services

## Building the Project

### Prerequisites

- Android Studio Giraffe or later
- JDK 17 or later
- Android SDK 34
- Gradle 8.2.1 (wrapper included)

### Build Instructions

1. **Clone the repository**

   ```bash
   git clone https://github.com/4mkbs/mkdevinfo.git
   cd mkdevinfo
   ```

2. **Open in Android Studio**

   - Open Android Studio
   - File → Open → Select the project directory

3. **Build the project (first time)**

   ```bash
   ./gradlew build
   ```

4. **Build APKs**

   ```bash
   # Debug APK (fast iteration)
   ./gradlew :app:assembleDebug

   # Release APK (optimized, shrinking enabled)
   ./gradlew :app:assembleRelease
   ```

## Run Locally (Device / Emulator)

### Prerequisites

Ensure you have:

- SDK Platform 34 installed (Android Studio > SDK Manager)
- JDK 17 (Android Studio bundles it; for CLI ensure JAVA_HOME points to JDK 17)
- An emulator (API 30+) or physical device with USB debugging enabled

### Android Studio Run

1. Open the project root (folder containing `settings.gradle`).
2. Let Gradle sync and download dependencies.
3. Select a device/emulator.
4. Press Run. The dashboard fragment should appear; if blank see Logcat steps below.

### Command Line Run

```bash
./gradlew :app:installDebug
adb shell am start -n com.sakib.devinfo/.ui.MainActivity
```

### VS Code Workflow (No Android Studio)

1. Run environment check:
   ```bash
   ./scripts/setup_env.sh
   ```
2. Open the Command Palette (Ctrl+Shift+P) → "Tasks: Run Task".
3. Choose:
   - `Gradle Assemble Debug APK` to compile
   - `Gradle Install Debug APK` to push to the connected device
   - `ADB Launch App` to start the activity
   - `Logcat DevInfo Tag` to view runtime logs
4. Modify code, re-run `Gradle Assemble Debug APK`, then `Gradle Install Debug APK`.

Artifacts:

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

If `adb` cannot see your device: enable USB debugging, run `adb kill-server && adb start-server`, and check `adb devices` again.

If multiple devices: `adb devices` then `adb -s <serial> shell am start -n ...`.

### Logs & Diagnostics

Filter logs by our tag:

```bash
adb logcat -s DevInfo
```

Expect lines:

```
DevInfo  MainActivity onCreate start
DevInfo  MainActivity layout set
DevInfo  Bottom navigation setup complete
DevInfo  Obtained NavController: ...
DevInfo  NavController + BottomNav wired
DevInfo  Destination changed -> ...navigation_dashboard
```

If `Destination changed` does not appear in 2s, open an issue with the full `DevInfo` log output.

### Unit Tests

```bash
./gradlew testDebugUnitTest
```

### Clean & Rebuild

```bash
./gradlew clean build --refresh-dependencies
```

### Troubleshooting

| Symptom           | Cause                          | Fix                                                        |
| ----------------- | ------------------------------ | ---------------------------------------------------------- |
| JAVA_HOME not set | Missing JDK path               | Install JDK 17 or run via Android Studio                   |
| Blank screen      | Nav destination not attached   | Check `DevInfo` logs; report absence of destination change |
| Slow first build  | Dependency download            | Subsequent builds faster (cached)                          |
| Permission denial | Runtime permission not granted | Grant requested permission in system dialog/settings       |

### Faster Iteration

Use continuous build (Ctrl+C to stop):

```bash
./gradlew :app:assembleDebug -t
```

## GitHub Actions CI/CD

This project includes a comprehensive GitHub Actions workflow that:

### 🧪 Testing

- Runs unit tests on every push and PR
- Uploads test results as artifacts

### 🔨 Building

- Builds both debug and release APKs
- Caches Gradle dependencies for faster builds
- Supports APK signing for releases

### 🚀 Releasing (Updated for v1.0.1+)

The repository includes `.github/workflows/android-release.yml` which triggers on pushing a tag that starts with `v`.

1. Ensure you have created secrets:
   - `RELEASE_KEYSTORE_BASE64` (Base64 of your `release.keystore`)
   - `RELEASE_KEYSTORE_PASSWORD`
   - `RELEASE_KEY_ALIAS`
   - `RELEASE_KEY_PASSWORD`
2. Bump `versionCode` and `versionName` in `app/build.gradle`.
3. Update `CHANGELOG.md`.
4. Commit and tag:

```bash
git add app/build.gradle CHANGELOG.md
git commit -m "chore(release): v1.0.1"
git tag v1.0.1
git push origin main --follow-tags
# or push tag separately
git push origin v1.0.1
```

5. Workflow builds:
   - Release APK & AAB
   - Uploads `mapping.txt`
   - Creates GitHub Release with artifacts

If you lack a keystore, the build falls back to debug signing (NOT for production distribution).

### Local Keystore (Optional)

```bash
keytool -genkeypair -v -keystore devinfo-release.keystore -alias devinfo \
  -keyalg RSA -keysize 2048 -validity 3650
base64 devinfo-release.keystore > keystore.b64
# Copy contents of keystore.b64 into GitHub secret RELEASE_KEYSTORE_BASE64
```

## Usage

### Installing the App

1. Download the APK from the GitHub releases
2. Enable "Install from unknown sources" in your device settings
3. Install the APK
4. Grant necessary permissions for full functionality

### Using Features

- **Dashboard**: View real-time system monitoring
- **Floating Overlay**: Enable in settings for persistent monitoring
- **Battery Monitor**: Enable for background battery monitoring
- **Benchmarks**: Run performance tests from the tools section
- **Theme**: Switch between light/dark themes in settings

## Technologies Used

- **Language**: Kotlin
- **UI Framework**: Material Design 3
- **Architecture**: MVVM with ViewBinding
- **Navigation**: Navigation Component
- **Background Processing**: Services and WorkManager
- **Charts**: MPAndroidChart
- **Permissions**: Dexter
- **Build System**: Gradle with Kotlin DSL

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Minimum Requirements

- **Android Version**: 5.0 (API level 21)
- **Target Version**: Android 14 (API level 34)
- **RAM**: 2GB recommended
- **Storage**: 50MB free space

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Material Design 3 guidelines
- Android Architecture Components
- Open source libraries used in this project

## Screenshots

_(Add screenshots of your app here)_

## Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/4mkbs/mkdevinfo/issues) page
2. Create a new issue with detailed information
3. Include device information and Android version

---

**Built with ❤️ for Android developers and enthusiasts**
