# EcoPantry

A household food-inventory and community food-donation Android app, built with **Kotlin**,
**Jetpack Compose (Material 3)**, **Room** (local database) and **Firebase** (Authentication +
Cloud Firestore + Cloud Messaging as the external/cloud database), following the architecture,
navigation and coding conventions of the previous assignment's app.

## Project structure

```
app/src/main/java/com/ecopantry/app/
├── data/            Room entities/DAOs, AppDatabase, AppDataStore, AuthRepository,
│                    DonationRepository, Repository (local facade), ServiceProvider
├── model/           Enums & the Donation (Firestore) data class
├── notification/    FirebaseMessagingService (donation-claim push notifications)
├── worker/          WorkManager CoroutineWorker that scans for soon-to-expire items
├── utility/         NotificationScheduler (channels + periodic work)
└── ui/
    ├── navigation/  AppNavigation.kt (Navigation 3 routes + bottom nav / nav rail)
    ├── screen/      One Screen.kt + ScreenViewModel.kt pair per app screen
    ├── theme/       Color.kt, Type.kt, Theme.kt (EcoPantry green palette)
    └── widget/      Reusable composables (stat cards, bar chart, input error text)
```

## Required setup before building

### 1. Firebase project
This project ships with a **placeholder** `app/google-services.json` so the module resolves
without a real backend. Before running the app:

1. Create a Firebase project at https://console.firebase.google.com.
2. Add an Android app with package name **`com.mishba.ecopantryapp`**.
3. Download the generated `google-services.json` and replace
   `app/google-services.json` with it.
4. In the Firebase console, enable:
   - **Authentication → Sign-in method → Email/Password**
   - **Firestore Database** (start in test mode for development, then lock down with
     security rules before submission/production)
   - **Cloud Messaging** (for donation-claim push notifications)

### 2. Firestore collections used
| Collection   | Purpose                                                              |
|--------------|-----------------------------------------------------------------------|
| `users`      | Profile info, OTP code/expiry, `isVerified`, `twoFactorEnabled` flags |
| `donations`  | Public donation listings browsed/claimed by other users              |

### 3. Build
Open the project root in Android Studio (Koala+ recommended) and let Gradle sync, or from the
command line:

```
./gradlew assembleDebug
```

## Notes on design decisions

- **OTP email verification (FR02):** a pure Android client cannot send custom emails without a
  backend. `AuthRepository` implements the full 6-digit OTP generate/store/verify logic against
  Firestore exactly as specified, and also triggers Firebase Authentication's built-in
  verification email as the real message delivered to the inbox. Swapping in a Firebase Cloud
  Function that emails the same stored code is a drop-in replacement once a backend is available.
- **Donation-claim push notifications (FR12):** `EcoPantryMessagingService` is the client-side
  receiver for Firebase Cloud Messaging. In production, a Cloud Function listening on writes to
  the `donations` collection would send the push; the client is already wired to receive and
  display it.
- **Charts:** the Weekly Tracker and Impact screens use a small dependency-free Canvas-based bar
  chart (`ui/widget/SimpleBarChart.kt`) instead of pulling in a third-party charting library.
- **App & splash logo:** generated from the supplied `logo_EP_outline.ai` artwork
  (`assets/logo_EP_outline.ai` in the assignment submission), recoloured to the EcoPantry green
  brand colour and exported to all required mipmap/adaptive-icon densities.
