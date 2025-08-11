# FloatPicture Project Overview

This is an Android application project, primarily developed in Java, using Gradle as its build system. The project appears to be designed for managing and displaying floating pictures or images, based on its name and the presence of `FloatImageView` and related components.

## Technologies Used

*   **Language:** Java
*   **Platform:** Android
*   **Build System:** Gradle
*   **JDK Version:** 17
*   **Android SDK:**
    *   `compileSdk`: 35
    *   `minSdkVersion`: 21 (Android 5.0 Lollipop)
    *   `targetSdkVersion`: 34 (Android 14)
*   **Gradle Version:** 8.14.1
*   **Android Gradle Plugin Version:** 8.10.1

## Building and Running

### Prerequisites

To build and run this project, you will need:

1.  **Java Development Kit (JDK) 17**: Ensure JDK 17 is installed and configured on your system. The project's `gradle.properties` explicitly sets `org.gradle.java.home=/opt/homebrew/opt/openjdk@17`.
2.  **Android SDK**: Install Android SDK Platform 35 and Android SDK Build-Tools (a version compatible with API 34/35, e.g., 34.0.0 or higher).
3.  **Android Studio (Recommended IDE)**: Android Studio provides the best development experience for Android projects, including SDK management, emulators, and debugging tools.

### Build Commands

You can build the project using the Gradle wrapper:

*   **Clean build:**
    ```bash
    ./gradlew clean build
    ```
*   **Assemble debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
*   **Install debug APK on a connected device/emulator:**
    ```bash
    ./gradlew installDebug
    ```

### Running the Application

*   **From Android Studio:** The easiest way to run the application is directly from Android Studio by selecting a connected device or emulator and clicking the "Run" button.
*   **Manual Installation:** After building, you can find the generated APK in `app/build/outputs/apk/debug/` (for debug builds). You can install it manually using ADB:
    ```bash
    adb install app/build/outputs/apk/debug/app-debug.apk
    ```

## Development Conventions

*   **Project Structure:** Follows a standard Android project structure with `app` module containing source code (`src/main/java`) and resources (`src/main/res`).
*   **Dependencies:** Project dependencies are managed in `app/build.gradle`.
*   **Permissions:** The `AndroidManifest.xml` defines necessary permissions such as `SYSTEM_ALERT_WINDOW`, `READ_EXTERNAL_STORAGE`, and `RECEIVE_BOOT_COMPLETED`.
*   **ProGuard/R8:** Release builds are configured with `minifyEnabled true` and `shrinkResources true`, using `proguard-rules.pro` for code obfuscation and optimization.
