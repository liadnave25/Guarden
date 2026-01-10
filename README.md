# 🌿 Guarden - Your Smart Plant Companion

**Guarden** is a modern, intelligent Android application designed to help plant enthusiasts track, manage, and care for their indoor and outdoor garden. Built with **Jetpack Compose** and powered by **Generative AI**, Guarden transforms plant care into a seamless and engaging experience.

---

## 📱 App Overview

Guarden solves the common problem of forgetting to water plants or not knowing how to care for them. It combines a beautiful UI with background intelligence to keep plants alive and thriving.

### ✨ Key Features

* **🌱 Smart Plant Tracking:** Add plants with custom photos (Camera/Gallery), types, and specific watering schedules.
* **🧠 AI Garden Consultant:** Integrated **Gemini AI** chatbot that knows your specific garden context. Ask for advice, and it answers based on the plants you actually own.
* **⛈️ Weather-Aware Alerts:** Fetches real-time weather data (OpenWeatherMap) based on your location. Sends notifications during extreme heat or storms to protect your garden.
* **📊 Professional Monitoring:** Real-time crash reporting and user behavior tracking via **Firebase**, ensuring a stable and data-driven product evolution.
* **💧 Smart Reminders:** Background workers monitor your plants and notify you when it's time to water or if you haven't visited the app in a while.
* **🎨 Dynamic UI:** Smooth animations using **Lottie**, adaptive layouts, and a clean Material3 design system.

---

## ⭐ Smart Rating & Sharing (UX + Monetization Upgrade)

Guarden recently upgraded the user experience and monetization layer by implementing **time-based + behavior-based** rating and sharing systems that are **controlled, non-intrusive, and context-aware**.

### ✅ Intelligent In-App Rating Flow (RatingManager)

A dedicated `RatingManager` controls when rating prompts can appear, ensuring users are asked only after meaningful engagement:

- **48 hours after install** before the first rating request can appear.
- **72-hour cooldown window** between rating prompts (from the last request time).
- All timing and status flags are persisted via **DataStore** to keep behavior consistent across sessions.

#### 🎯 Dual Feedback Path
Guarden includes a “double feedback route” based on the user’s rating:

- **High rating (⭐ 4–5):** Shows a **thank-you** message to reinforce positive sentiment.
- **Low rating (⭐ 1–3):** Opens a **feedback request** (to learn what to improve) and **triggers an Interstitial Ad** in parallel to maximize monetization without impacting highly satisfied users.

### 🔗 Context-Aware Share App System

A smart sharing mechanism encourages organic growth, but adapts to the user’s plan and the moment of value:

- **Free plan users:** can see a share suggestion **once every 3 days** (cooldown-based).
- **Premium users:** get the share suggestion at a **high-value moment** — **immediately after finishing an interaction with the AI Agent**.

All share cooldown data and last-trigger timestamps are stored in **DataStore**, providing a personalized and consistent UX.

---

## 💎 Freemium Business Model

Guarden utilizes a hybrid monetization strategy balancing user experience with revenue generation.

| Feature | 🆓 Free Plan | 👑 Premium Plan |
| :--- | :--- | :--- |
| **Plant Capacity** | Limited to 7 Plants | **Unlimited** |
| **AI Assistant** | Locked 🔒 | **Full Access** 🔓 |
| **Ad Experience** | **Ads Enabled:**<br>• *App Open Ad* (on launch)<br>• *Native Ad* (in list)<br>• *Rewarded Ad* (to add plants) | **100% Ad-Free** |
| **Support** | Standard | Priority |

> **In-App Logic:** Users can simulate purchasing "Plant Packs" or subscribing to Premium via the Settings screen, which instantly updates the UI and DataStore preferences.

---

## 📊 Analytics & Monitoring (Firebase Integration)

Guarden is not just a standalone app but a managed product. We integrated **Firebase** to monitor stability and analyze user behavior in real-time.

### 📈 Firebase Analytics (Custom Events)
We track key KPIs to understand user engagement and the sales funnel using `logEvent` via Dependency Injection:
* **`plant_added`**: Tracks when a user saves a plant, including parameters like `plant_type` and `water_frequency`.
* **`chat_opened`**: Monitors engagement with the AI premium feature.
* **`purchased_premium`**: Tracks conversion rates for the subscription model.
* **`purchased_plant_pack`**: Tracks revenue from one-time in-app purchases.

### 🛡️ Firebase Crashlytics
* **Real-time Stability:** Automatic reporting of fatal crashes and non-fatal errors.
* **Fatal Exception Handling:** The app captures stack traces (e.g., `RuntimeException`) to pinpoint bugs in specific ViewModels or Screens immediately.

---

## 🛠️ Tech Stack & Capabilities

This project demonstrates modern Android development practices using **Kotlin** and **MVVM Architecture**.

### 🎨 UI & UX
* **Jetpack Compose:** 100% Declarative UI toolkit.
* **Material Design 3:** Modern components and theming.
* **Lottie Files:** High-quality animations for empty states and buttons.
* **Coil:** Efficient image loading for plant photos.

### 🏗️ Architecture & Injection
* **MVVM:** Clean separation of concerns (Model-View-ViewModel).
* **Hilt (Dagger):** Dependency Injection for ViewModels, Database, Analytics, and API services.
* **Navigation Compose:** Single-activity navigation architecture.

### 💾 Data & Networking
* **Room Database:** Local persistence for plant data.
* **DataStore (Preferences):** Storing user settings (Premium status, limits, notifications, rating/share cooldown timestamps).
* **Retrofit & Gson:** Networking client for Weather API calls.
* **Coroutines & Flow:** Asynchronous programming and reactive state management.

### ☁️ Cloud & Services
* **Google Generative AI SDK:** Integration with **Gemini Flash** model for the chat assistant.
* **Firebase Suite:**
    * **Analytics:** For behavioral tracking.
    * **Crashlytics:** For stability monitoring.
* **Google Mobile Ads SDK (AdMob):** Implementation of App Open, Native, Rewarded, and Interstitial ads.
* **OpenWeatherMap API:** Real-time weather data fetching.

### ⚙️ Background Processing
* **WorkManager:** Robust scheduling for background tasks:
    * `MorningWorker`: Checks weather and engagement.
    * `NoonWorker`: Checks watering schedules.

---

## 🚀 Getting Started

To run this project locally, you will need to set up a few API keys:

1.  **Clone the repository.**
2.  **Open in Android Studio.**
3.  **Configure API Keys:**
    * Open `WeatherApi.kt` / `PlantViewModel.kt` and insert your **OpenWeatherMap Key**.
    * Open `ChatViewModel.kt` and insert your **Gemini AI API Key**.
    * Ensure `google-services.json` is present in the `app/` folder (for Firebase).
4.  **Sync Gradle** and Run on an Emulator/Device.

---

## 👨‍💻 Created By

**Liad Nave**  
*Product Development Project - Afeka College*