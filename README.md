# ProPodcast KMM

Kotlin Multiplatform port of [ProPodcastN](https://github.com/PrimakPavel/ProPodcastN): one Compose Multiplatform
UI for Android and iOS. Work in progress, migrated step by step.

## Structure

```
shared/      Kotlin Multiplatform library: common code and Compose UI (commonMain, androidMain, iosMain)
androidApp/  Android application: MainActivity hosting the shared UI
iosApp/      Xcode project: SwiftUI app hosting the shared UI
```

## Build and run

Requirements: JDK 17, Android SDK with platform 37, Xcode 27.

```bash
./gradlew :androidApp:installDebug
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme. The build phase calls
`./gradlew :shared:embedAndSignAppleFrameworkForXcode` to compile the shared framework.

The Android application id is `com.propodcast.app` plus `propodcast.applicationIdSuffix` from
`gradle.properties` (`.kmm` by default), so it installs next to the original app.
