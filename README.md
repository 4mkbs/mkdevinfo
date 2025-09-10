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

- Android Studio Arctic Fox or later
- JDK 17 or later
- Android SDK 34
- Gradle 8.3

### Build Instructions

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/devinfo.git
   cd devinfo
   ```

2. **Open in Android Studio**

   - Open Android Studio
   - File → Open → Select the project directory

3. **Build the project**

   ```bash
   ./gradlew build
   ```

4. **Build APK**

   ```bash
   # Debug APK
   ./gradlew assembleDebug

   # Release APK
   ./gradlew assembleRelease
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

### 🚀 Releasing

- Automatically creates GitHub releases for version tags
- Uploads signed APKs to releases
- Generates detailed release notes

### Setting up GitHub Actions

1. **Create the following secrets in your GitHub repository:**

   - `SIGNING_KEY` - Base64 encoded keystore file
   - `ALIAS` - Keystore alias
   - `KEY_STORE_PASSWORD` - Keystore password
   - `KEY_PASSWORD` - Key password

2. **To create a release:**

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

3. **The workflow will automatically:**
   - Run tests
   - Build APKs
   - Create a GitHub release
   - Upload the APK to the release

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

1. Check the [Issues](https://github.com/yourusername/devinfo/issues) page
2. Create a new issue with detailed information
3. Include device information and Android version

---

**Built with ❤️ for Android developers and enthusiasts**
