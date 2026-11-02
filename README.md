# Worknest Studio

Worknest Studio is an offline-first Android workspace for solo creators and small teams. Organize projects, tasks, client records and notes with a focused home dashboard and highly configurable appearance, currency, week start, layout density, privacy preferences and local data settings.

- Android app built with Kotlin and Jetpack Compose
- Android 6+ install support, target API 33 for Android 13 compatibility
- Local-only storage. No account, cloud sync, payment processing or SaaS service is claimed
- Data is stored in app-private preferences on the device
- GitHub Actions builds a debug-installable APK on pushes to main and publishes tagged APK releases

## Build

Open this project in Android Studio, or run `./gradlew assembleDebug test` with JDK 17 and Android SDK platform 35 installed.

## Roadmap

Part of a 20-app series of separate repositories and separate APKs. Worknest Studio is app 1. Planned categories include a browser, video player, gallery, camera and calendar. Each app will be built and released separately after verification.
