# Hearth

Hearth is a small-room chat app for Android, built with Jetpack Compose. A phone opens rooms from the top bar. A wide window keeps the room list beside the conversation. The profile is where you set the name on messages you send.

The screen structure follows the [Jetchat](https://github.com/android/compose-samples/tree/main/Jetchat) sample: a conversation, a drawer of rooms, and a profile. Jetchat ships a fixed script of messages. Hearth writes each message to Cloud Firestore once you add your own Firebase config.

## Firebase

This repository does not include a Firebase project, an API key, or a `google-services.json` file. Create a project in the [Firebase console](https://console.firebase.google.com/), add an Android app, and turn on Cloud Firestore.

Use this package name when Firebase asks for it:

```
com.hearth.chat
```

Download `google-services.json` from that Android app and place it here:

```
app/google-services.json
```

`app/google-services.json.example` shows the shape of that file. It is not a working project. Do not rename the example and try to ship it. The real file is listed in `.gitignore`, so it stays on your machine.

Rebuild after the file is in place. Gradle applies the Google services plugin only when `app/google-services.json` exists, so the project still builds without it.

Without the file, Hearth keeps the conversation on the phone and labels the room **Local**. Those messages are not uploaded. With the file, the open room listens to this collection and a send writes a new document:

```
rooms/{roomId}/messages
```

Each document stores `authorId`, `authorName`, `body`, and `sentAt` (epoch milliseconds). The room ids in the app are `studio`, `kitchen`, and `night-shift`.

## Build

Open the project in Android Studio (JDK 17, Android SDK 35) and run the `app` configuration.

```bash
./gradlew :app:assembleDebug
```
