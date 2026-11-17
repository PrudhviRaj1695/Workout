# IRON WILL

A native Android health and fitness command center built with Kotlin and Jetpack Compose.

## Build locally

Requires JDK 17 and Android SDK Platform 36.

```bash
gradle :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Automated APK

Every push to `main` runs **Build IRON WILL APK**. Download the `iron-will-debug-apk` artifact from the successful GitHub Actions run.

## Current build scope

- Premium dark command-center dashboard
- Home, Missions, AI Coach, Insights and Profile tabs
- Local workout session timer
- Personality and language selectors
- Honest empty states for health data not yet connected

Health Connect, background step tracking, floating overlay service, live LLM provider, and persistent analytics are not enabled in this initial build. Health metrics are intentionally not fabricated.
