# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres to [Semantic Versioning](https://semver.org/).

## [1.1.1] - 2025-09-20

### Added
- Continuous Integration workflow (`ci.yml`) for build + unit tests on pushes and PRs.
- Automated release workflow (`android-release.yml`) attaching APK, AAB, and `mapping.txt` to tagged releases.
- New adapters and data models for sensors and apps (ListAdapter + DiffUtil) improving UI efficiency.

### Changed
- Version bump: `versionCode` 3 / `versionName` 1.1.1.
- Refined README with release/versioning and tagging instructions.

### Fixed
- Improved stability of sensor list updates (eliminated prior ANR conditions).

### Internal
- Ignored `keystore.b64` and reinforced shrink configuration.
- ProGuard/R8 rules expanded (coroutines, sensor classes, annotations).


## [1.0.1] - 2025-09-20

### Added

- GitHub release automation workflow (planned) for building signed artifacts on tag.
- Comprehensive sensor list performance refactor using `ListAdapter` + `DiffUtil`.

### Changed

- Reduced sensor event UI pressure via throttling (400ms) and batching on background threads.
- Enabled R8 code shrinking & resource shrinking for release builds.
- Upgraded release versionCode to 2 / versionName to 1.0.1.

### Fixed

- ANR when opening Sensors tab (main thread overload due to per-event adapter churn).
- Crash caused by invalid `@color/material_dynamic_primary60` (replaced with theme attribute).

### Internal

- Added ProGuard rules for coroutines, sensors, and annotation retention.
- Conditional signing config logic to allow local release builds without real keystore.

## [1.0.0] - 2025-09-??

### Added

- Initial project structure and core system/device info features.

[1.0.1]: https://github.com/4mkbs/mkdevinfo/releases/tag/v1.0.1
