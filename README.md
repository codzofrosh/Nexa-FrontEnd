# Nexa Android

This repository now contains a native Android authentication app for Nexa rather than a browser SPA.

## What the app does

- provides **Login** and **Sign up** flows in a native Android UI;
- lets you edit the **API base URL**, **login endpoint**, and **signup endpoint** inside the app;
- sends authentication requests directly to the backend using `HttpURLConnection`;
- stores the latest **session token**, **refresh token**, and **user payload** locally for inspection;
- keeps an **activity log** of recent auth attempts for debugging.

## Backend integration

The backend repository you referenced is:

- `https://github.com/codzofrosh/nexa-beeper-connector`

That GitHub repository was not reachable from this execution environment, so the Android client was implemented with configurable auth routes and route fallbacks.

### Default auth routes

- Login: `/api/auth/login`
- Sign up: `/api/auth/signup`

### Automatic fallback attempts

#### Login

- `/api/auth/login`
- `/api/v1/auth/login`
- `/auth/login`
- `/api/login`

#### Sign up

- `/api/auth/signup`
- `/api/auth/register`
- `/api/v1/auth/signup`
- `/api/v1/auth/register`
- `/auth/signup`
- `/auth/register`

For Android emulator usage, the default base URL is set to `http://10.0.2.2:3000` so the app can reach a backend running on your host machine.

## Android stack choice

This app uses:

- **Kotlin** for the Android application code;
- **Android Views + Material 3** for a stable native UI;
- **SharedPreferences** for local settings/session persistence;
- **HttpURLConnection** for backend communication without adding extra networking dependencies.

This is a strong fit for an MVP Android app because it stays simple, understandable, and easy to align with a changing backend contract.

## Project structure

- `app/src/main/java/com/nexa/app/MainActivity.kt` – login/signup UI behavior.
- `app/src/main/java/com/nexa/app/data/AuthApiClient.kt` – auth networking and fallback endpoint handling.
- `app/src/main/java/com/nexa/app/data/AuthPreferences.kt` – persistence for settings, session, and logs.
- `app/src/main/res/layout/activity_main.xml` – native Android screen layout.
- `app/src/main/res/values/` – strings, colors, and theme resources.

## Commands

Because binary files are not supported in this workflow, `gradle/wrapper/gradle-wrapper.jar` is intentionally not committed. Regenerate it locally before using the wrapper commands:

```bash
gradle wrapper
./gradlew tasks
./gradlew assembleDebug
```
