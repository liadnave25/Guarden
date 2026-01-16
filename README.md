# 🌿 Guarden - Your Smart Plant Companion

**Guarden** is a modern, intelligent Android application designed to help plant enthusiasts track, manage, and care for their indoor and outdoor garden. Built with **Jetpack Compose** and powered by **Generative AI**, Guarden transforms plant care into a seamless and engaging experience.

---

## 📱 App Overview

Guarden solves the common problem of forgetting to water plants or not knowing how to care for them. It combines a beautiful UI with background intelligence to keep plants alive and thriving.

### ✨ Key Features

* **🌱 Smart Plant Tracking:** Add plants with custom photos (Camera/Gallery), types, and specific watering schedules.
* **🧠 AI Garden Consultant:** Integrated **Gemini AI** chatbot that knows your specific garden context. Ask for advice, and it answers based on the plants you actually own.
* **⛈️ Weather-Aware Alerts:** Fetches real-time weather data (OpenWeatherMap) based on your location. Sends notifications during extreme heat, cold, or storms to protect your garden.
* **📊 Professional Monitoring:** Real-time crash reporting and user behavior tracking via **Firebase**, ensuring a stable and data-driven product evolution.
* **💧 Smart Reminders:** Background workers monitor your plants and notify you when it's time to water or if you haven't visited the app in a while.
* **🎁 Reactivation Rewards:** Detects inactive users and welcomes them back with temporary Premium gifts.
* **🎨 Dynamic UI:** Smooth animations using **Lottie**, adaptive layouts, and a clean Material3 design system.

---

## 📈 Strategic User Engagement

Guarden implements industry-standard psychological triggers and marketing strategies to ensure user retention and growth.

### 🔄 Habit Loops & Daily Routine
Guarden is designed to become part of the user's daily schedule through structured loops:
- **Fixed-Time Reminders:** Automated checks at 09:00 (`MorningWorker`) and 13:00 (`NoonWorker`).
- **Micro-Action Triggers:** "Plants Miss You" alerts on even-numbered days encourage simple app "Check-ins" to maintain engagement.

### 🎁 Reactivation Rewards (The 14-Day Hook)
To win back inactive users, Guarden features a sophisticated **Hook & Delivery** reward system:
- **The Hook:** If a user is inactive for **14 days**, the system triggers a special notification: *"Special Gift Waiting! 🎁"*.
- **The Reward:** Upon return, the user is granted **7 days of an Ad-Free experience** (Premium trial) to break the churn cycle.
- **The Delivery:** A personalized "Welcome Back" dialog reinforces the value and reward status immediately upon launch.

---

## ⭐ Smart Rating & Sharing (UX + Monetization Upgrade)

Guarden uses **time-based + behavior-based** rating and sharing systems that are **controlled, non-intrusive, and context-aware**.

### ✅ Intelligent In-App Rating Flow (RatingManager)

A dedicated `RatingManager` controls when rating prompts can appear, ensuring users are asked only after meaningful engagement:

- **48 hours after install** before the first rating request can appear.
- **72-hour cooldown window** between rating prompts (from the last request time).
- All timing and status flags are persisted via **DataStore** to keep behavior consistent across sessions.

#### 🎯 Dual Feedback Path
Guarden includes a “double feedback route” based on the user’s rating:

- **High rating (⭐ 4–5):** Shows a **thank-you** message to reinforce positive sentiment.
- **Low rating (⭐ 1–3):** Opens a **feedback request** and **triggers an Interstitial Ad** in parallel to maximize monetization.

### 🔗 Context-Aware Share App System

A smart sharing mechanism encourages organic growth based on the user’s plan and moment of value:

- **Free plan users:** can see a share suggestion **once every 3 days** (cooldown-based).
- **Premium users:** get the share suggestion **immediately after finishing an interaction with the AI Agent**.

---

## 💎 Freemium Business Model

Guarden utilizes a hybrid monetization strategy balancing user experience with revenue generation.

| Feature | 🆓 Free Plan | 👑 Premium Plan |
| :--- | :--- | :--- |
| **Plant Capacity** | Limited to 7 Plants | **Unlimited** |
| **AI Assistant** | Locked 🔒 | **Full Access** 🔓 |
| **Ad Experience** | **Ads Enabled:**<br>• App Open & Native<br>• Interstitial (on low rating) | **100% Ad-Free** |
| **Special Rewards** | **7-Day Ad-Free** (Reactivation) | Always Ad-Free |

> **In-App Logic:** Users can simulate purchasing "Plant Packs" or subscribing to Premium via the Settings screen, which instantly updates the UI and DataStore preferences.

---

## ⚖️ Legal & Transparency

We prioritize user privacy. The following policies are easily accessible within the app settings:
* **Privacy Policy:** [View Policy](https://sites.google.com/view/guarden-privacy-policy/%D7%91%D7%99%D7%AA)
* **Terms & Conditions:** [View Terms](https://sites.google.com/view/guarden-termsconditions/%D7%91%D7%99%D7%AA)

---

## 📊 Analytics & Monitoring (Firebase Integration)

Guarden is a managed product. We integrated **Firebase** to monitor stability and analyze behavior in real-time.

### 📈 Firebase Analytics (Custom Events)
We track key KPIs using `logEvent` via Dependency Injection:
* **`plant_added`**: Tracks parameters like `plant_type` and `water_frequency`.
* **`chat_opened`**: Monitors engagement with the AI premium feature.
* **`purchased_premium`**: Tracks conversion rates for the subscription model.

### 🛡️ Firebase Crashlytics
* **Real-time Stability:** Automatic reporting of fatal crashes and non-fatal errors.
* **Fatal Exception Handling:** Captures stack traces to pinpoint bugs in specific ViewModels or Screens immediately.

---

## 🛠️ Tech Stack & Capabilities

* **Architecture:** **MVVM** with **Hilt** (Dependency Injection).
* **UI:** 100% **Jetpack Compose** (Material Design 3) and **Lottie** animations.
* **Persistence:** **Room Database** (Plants) & **DataStore** (Preferences & Cooldowns).
* **AI:** Google **Generative AI SDK** (Gemini Flash).
* **Networking:** **Retrofit & Gson** for Weather API.
* **Background Processing:** **WorkManager** for scheduled tasks.
* **Ads:** **AdMob SDK** (Native, App Open, Interstitial, Rewarded).

---

## 🚀 Getting Started

1.  **Clone the repository.**
2.  **Configure API Keys:**
    * Open `WeatherApi.kt` / `PlantViewModel.kt` and insert your **OpenWeatherMap Key**.
    * Open `ChatViewModel.kt` and insert your **Gemini AI API Key**.
    * Ensure `google-services.json` is present in the `app/` folder (for Firebase).
3.  **Sync Gradle** and Run.

---

## 👨‍💻 Created By

**Liad Nave** *Product Development Project - Afeka College*