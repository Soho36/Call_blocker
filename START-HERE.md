# Your first Android app: Contacts Only

The project, tools and build caches are on drive **I:**:

```
I:\AndroidProjects\ContactsOnly    Editable app project
I:\AndroidProjects\tools           Java, Gradle and Android SDK
I:\AndroidProjects\cache           Build downloads and temporary files
I:\AndroidProjects\Releases        APK, source ZIP and verification report
```

## Try it on your Nokia C21

1. Copy **ContactsOnly-0.1.0-debug.apk** to the phone, for example with a USB cable. An APK is an Android installation file.
2. Open the APK using the phone's Files app. Android may ask you to allow that file manager to install unknown apps. Allow it for this installation; you can switch that permission off again afterward. Keep Play Protect enabled.
3. Open **Contacts Only**. Tap **Set up call screening** and select Contacts Only in Android's dialog. This selects the call-screening app, not a replacement dialler.
4. Turn on **Block unsaved callers**.
5. Call the Nokia from a saved contact and then from an unsaved number. The saved number should ring; the unsaved number should be rejected.
6. Tap **Allow everyone for 30 minutes** and repeat the unsaved call. It should now ring. You can also switch blocking off entirely.

The app includes a fuller test guide. Hidden/private numbers are outside this screening API's coverage and may require the Phone app's own blocking setting. Carrier voicemail may still accept rejected calls.

**Real-call behaviour and the interface have not yet been tested on your Nokia.** The first phone test will tell us whether its Android Go installation supports the screening role and honours the rejection request. A status message in the app tells you if the role is unavailable. The local activity list records the app's requests, not proof that the network carried them out.

## Build it again

Open `I:\AndroidProjects\ContactsOnly` in File Explorer and double-click **build.cmd**. This builds the APK, runs the tests and checks Android lint. The window stays open so you can read the result.

The new APK will be at:
`I:\AndroidProjects\ContactsOnly\app\build\outputs\apk\debug\app-debug.apk`

Rebuilding does not automatically update the copy in Releases. Use the new APK from the build folder for subsequent versions. Keep the development signing key in `I:\AndroidProjects\cache\android` if you want later builds to update the installed app without uninstalling it first. It is a development key, not a production release key.

## Explore the code

Start with **README.md** inside the project. It explains the design and points to each source file. The app is written in **Java**, using Android's own UI and call-screening APIs. It does not need a server, subscription or Python environment.

Android Studio is not required for the prepared build command and has not been installed. If you later want a visual Android editor, the project is ready to open in Android Studio. Configure its SDK, caches and any emulator on I: to preserve C: space.

The supplied source ZIP contains the editable project and Gradle wrapper, not the SDK, compiler, caches or signing key. Those remain in the folders above.
