# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FloatPicture is an Android application written in Java that allows users to display floating images on their screen. The app creates overlay windows that can display custom pictures with configurable settings like position, size, and transparency.

## Build Commands

### Building the Project
- **Clean and build**: `./gradlew clean build`
- **Build debug APK**: `./gradlew assembleDebug`
- **Build release APK**: `./gradlew assembleRelease`
- **Install debug APK**: `./gradlew installDebug`

### Lint and Code Quality
- **Run lint checks**: `./gradlew lint`
- **Generate lint report**: `./gradlew lintDebug`

### Project Structure and Dependencies
- **Display dependencies**: `./gradlew dependencies`
- **Check for dependency updates**: `./gradlew dependencyUpdates` (if plugin available)

## Architecture Overview

### Core Components

**MainApplication** (`MainApplication.java`):
- Central application state manager
- Maintains a HashMap registry of active floating views
- Manages global visibility state and safe window alpha values
- Handles crash reporting in release builds

**View Management**:
- `FloatImageView`: Custom floating overlay view that handles touch events and positioning
- `ManageListAdapter`: RecyclerView adapter for managing picture list in main activity
- Views are registered/unregistered through MainApplication's ViewRegister

**Activity Flow**:
1. `PermissionRequestActivity`: Entry point, handles permission requests
2. `MainActivity`: Main management interface with drawer navigation
3. `PictureSettingsActivity`: Individual picture configuration
4. `GlobalSettingsActivity`: App-wide settings

**Services and Background Components**:
- `NotificationService`: Foreground service managing floating windows
- `BootCompleteReceiver`: Auto-starts service after device boot

### Key Permissions
- `SYSTEM_ALERT_WINDOW`: Required for floating overlay windows
- `PACKAGE_USAGE_STATS`: For detecting foreground app
- `MANAGE_EXTERNAL_STORAGE`: For accessing user images
- `RECEIVE_BOOT_COMPLETED`: For auto-start functionality

### Configuration and Data
- Settings stored via SharedPreferences
- Picture metadata managed through `PictureData` utility class
- Configuration constants defined in `Config.java`
- Image processing handled by `ImageMethods.java`

## Development Environment

### Requirements
- **JDK**: Version 17 (configured in gradle.properties)
- **Android SDK**: Compile SDK 35, Min SDK 21, Target SDK 34
- **Gradle**: Uses wrapper (gradlew/gradlew.bat)

### Code Conventions
- Java 17 source/target compatibility
- Material Design Components for UI
- AndroidX libraries throughout
- ProGuard/R8 enabled for release builds with resource shrinking

### Important Notes
- Debug keystore included (`debug.keystore`) for consistent signing
- Lint configured to not abort on errors (`abortOnError false`)
- Large heap enabled for image processing
- Legacy external storage support enabled
- Chinese localization included (`values-zh/`)