# Contacts Only — Android experiment

A small native Java app for Android 10 and later, initially intended for a Nokia C21 running Android 11 Go. This is a personal sideloadable prototype, not a Google Play release.

## Install and try

1. Copy `ContactsOnly-0.1.0-debug.apk` to the phone (a USB cable is sufficient).
2. Open it in the phone's Files app. If Android asks, allow that file manager to install unknown apps for this installation. You can turn that permission off again afterward. Keep Play Protect enabled.
3. Open **Contacts Only** and tap **Set up call screening**. Select this app in Android's system dialog. This replaces the selected caller-ID/spam-screening app, not your normal Phone app.
4. Turn on **Block unsaved callers**. Blocking is off initially.
5. Use **How to test on my phone** in the app, or follow `TESTING.md`.

If the app says screening is unavailable, the device does not expose the necessary role. If Android refuses the role, leave blocking off and note the exact message. Do not assume protection based solely on a successful APK install.

To stop: switch blocking off or uninstall the app. Selecting a different call-screening app also removes this app from the screening role. Switching this app off leaves it selected but allows calls through; to restore a previous spam filter, select that app again in Android's default-app settings.

## What it does

- Rejects eligible incoming telephone calls from unsaved numbers while enabled.
- Lets saved contacts through using Android's own contact filtering.
- Allows all screened calls while off or during a 30-minute pause.
- Resumes after the pause without needing an alarm or a running activity.
- Keeps the last 30 screening decisions in private app storage, with a clear button.
- Shows whether Android offers the screening role and whether this app holds it.

The pause uses the phone's wall clock so it survives process restarts. Manually changing the date or time can shorten or lengthen it. After a reboot, unlock the phone once before expecting this prototype to work; pre-unlock screening has not been implemented or tested.

## Limits

- Android does not deliver restricted, unknown, unavailable or payphone caller-ID presentations to this API. Hidden-number blocking needs the Phone app's own setting, if present.
- A saved contact is allowed regardless of who actually owns the calling number. Caller-ID spoofing can bypass a contacts-only rule.
- Rejection is requested from Android; voicemail and what the caller hears depend on the carrier. The history is a record of app decisions, not proof of network rejection.
- No filtering of WhatsApp, Signal, other apps' calls or text messages.
- Incoming legitimate unsaved callers are rejected too. Pause for expected deliveries or callbacks.
- Android controls emergency-call handling and any screening bypasses. This app does not intercept outgoing calls. Never call emergency services just to test it.
- Role availability and actual call handling must be verified on the physical Nokia. Unit tests do not establish manufacturer or carrier compatibility.

## Privacy

No Internet permission, analytics, advertising, accounts, contact access, call-log access, overlays or accessibility service. The app records only screened incoming numbers, timestamps and its decisions in private local storage. Android backup is disabled. Clearing activity removes these records; uninstalling clears app data. This APK is signed with a development key and is intended for local testing.

## Build on this computer

The prepared layout is:

```
I:\AndroidProjects\
  ContactsOnly\       source code, Gradle wrapper, build.cmd
  tools\              JDK 17, Gradle 8.11.1, Android SDK 35
  cache\              Gradle, Android and temporary build files
```

Double-click `build.cmd`, or run `I:\AndroidProjects\ContactsOnly\build.cmd` from PowerShell. It compiles the debug APK, runs unit tests and runs Android lint. All tool environment variables are scoped to that process; the script does not alter your global Java or Python setup. Build caches and temporary files are directed to I:.

APK output: `app\build\outputs\apk\debug\app-debug.apk`.

The first build needs Internet access to download Gradle plugin/test dependencies. Subsequent builds can reuse the cache. SDK components, JDK and Gradle archives were fetched from their official distribution endpoints and their published checksums checked during setup. The Windows build script uses Windows' existing trusted certificate store for HTTPS because this machine's connection is not trusted by the portable JDK's bundled store. Certificate verification remains enabled; no certificates or system security settings are changed.

## Android Studio or another computer

Open this folder as a project in Android Studio. Use JDK 17, Android SDK platform 35 and build tools 35.0.0. The wrapper pins Gradle 8.11.1; the Android Gradle plugin is 8.9.2. Android Studio is optional and is not installed as part of this setup. If you install it later, point its SDK and caches to I: too.

From a configured shell: `gradlew.bat assembleDebug testDebugUnitTest lintDebug` (Windows), or `./gradlew assembleDebug testDebugUnitTest lintDebug` (macOS/Linux).

## Source map

- `ContactsScreeningService.java`: Android callback; sends the rejection/allow response before recording activity.
- `ScreeningPolicy.java`: pure blocking and pause decision, tested without a phone.
- `AppStore.java`: switch, pause deadline and bounded local history.
- `MainActivity.java`: role setup, status, controls and activity display.
- `app/src/main/res/`: interface strings, layout, colours and icon.
- `app/src/test/`: decision tests.

**Design invariant:** do not add `READ_CONTACTS` without changing the screening logic. Android normally excludes contacts from callbacks, but granting this permission includes them. The current app relies on not requesting it.

Official references:
- https://developer.android.com/reference/android/telecom/CallScreeningService
- https://developer.android.com/reference/android/app/role/RoleManager
- https://developer.android.com/reference/android/telecom/CallScreeningService.CallResponse.Builder
