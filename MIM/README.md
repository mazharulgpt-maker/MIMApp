# MIM — Personal Virtual Hardware Button Controller for Android

MIM is a lightweight, 100% offline native Android application written in **Kotlin**, **Jetpack Compose**, and **Material 3**. It replaces three damaged or missing physical smartphone hardware buttons:

- **Left Button**: Volume Up (`AudioManager.STREAM_MUSIC`, +1 step per tap)
- **Center Button**: Screen Power / Lock (`DevicePolicyManager.lockNow()` or Android 9+ `AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN`)
- **Right Button**: Volume Down (`AudioManager.STREAM_MUSIC`, -1 step per tap)

## Privacy & Security Guarantees
- Zero `INTERNET` permission in `AndroidManifest.xml`
- No accounts, login, ads, analytics, or cloud uploads
- No root required
- Device Admin policy requests `<force-lock />` **only**
- Optional Accessibility lock sets `canRetrieveWindowContent="false"`
- Optional Floating Controller is disabled by default and only asks for `SYSTEM_ALERT_WINDOW` when enabled

## Building & Installing the APK

### Option 1: Android Studio (Recommended)
1. Open the `MIM` folder in **Android Studio** (Ladybug / 2024.2+ with JDK 17).
2. Wait for Gradle sync to finish.
3. Connect your Android phone via USB (with **USB Debugging** enabled) and press **Run (`▶`)**, OR go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. Locate `app/build/outputs/apk/debug/app-debug.apk`, copy it to your phone, and tap to install.

### Option 2: Command Line (Gradle + ADB)
```bash
# If Gradle 8.9 is installed locally:
gradle assembleDebug

# Install directly to connected Android device:
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Build APK in the Cloud (No PC / Android Studio Required)
1. Upload the contents of this project folder to a GitHub repository.
2. Open the **Actions** tab and run **Build MIM Android APK**.
3. Download the `MIM-debug-apk` artifact directly on your Android phone, unzip, and install `app-debug.apk`.

## About Waking a Sleeping Screen on Android
Android does not allow any ordinary third-party app (or floating overlay) to wake a device from deep sleep or power on a completely shut-down phone. To wake your phone without a physical power button, open **MIM → Settings → Screen Wake Guide** to jump directly to your phone's built-in display settings for:
- **Double Tap to Wake**
- **Lift to Wake**
- **Tap to Wake / Always-On Display**
- **Fingerprint Sensor Wake**

## Physical Device Test Checklist
1. Volume Up raises actual Android media volume by 1 step per tap.
2. Volume Down lowers actual Android media volume by 1 step per tap.
3. Repeated taps work smoothly up to max and down to min without lag.
4. Center Power button immediately turns display OFF and locks the phone (does NOT shut down or restart).
5. Device Admin activation flow works cleanly; Accessibility Lock option preserves biometric unlock on Android 9+.
6. Settings (Power icon style, button size, spacing, theme) persist via DataStore after closing and reopening MIM.
7. MIM does not crash after locking and waking the phone.
8. Optional Floating Controller drags smoothly, expands into Vol+ / Lock / Vol-, and closes cleanly.
