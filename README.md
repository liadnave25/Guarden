# 🌿 Guarden - Your Smart Plant Companion

**Guarden** is a modern, intelligent Android application designed to help plant enthusiasts track, manage, and care for their indoor and outdoor garden. Built with **Jetpack Compose** and powered by **Generative AI**, Guarden transforms plant care into a seamless and engaging experience.

---

## 📱 App Overview

Guarden solves the common problem of forgetting to water plants or not knowing how to care for them. It combines a beautiful UI with background intelligence to keep plants alive and thriving.

### ✨ Key Features

* **🌱 Smart Plant Tracking:** Add plants with custom photos (Camera/Gallery), types, and specific watering schedules.
* **🧠 AI Garden Consultant:** Integrated **Gemini AI** chatbot that knows your specific garden context. Ask for advice, and it answers based on the plants you actually own.
* **⛈️ Weather-Aware Alerts:** Fetches real-time weather data (OpenWeatherMap) based on your location. Sends notifications during extreme heat or storms to protect your garden.
* **💧 Smart Reminders:** Background workers monitor your plants and notify you when it's time to water or if you haven't visited the app in a while.
* **🎨 Dynamic UI:** Smooth animations using **Lottie**, adaptive layouts, and a clean Material3 design system.

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

## 🛠️ Tech Stack & Capabilities

This project demonstrates modern Android development practices using **Kotlin** and **MVVM Architecture**.

### 🎨 UI & UX
* **Jetpack Compose:** 100% Declarative UI toolkit.
* **Material Design 3:** Modern components and theming.
* **Lottie Files:** High-quality animations for empty states and buttons.
* **Coil:** Efficient image loading for plant photos.

### 🏗️ Architecture & Injection
* **MVVM:** Clean separation of concerns (Model-View-ViewModel).
* **Hilt (Dagger):** Dependency Injection for ViewModels, Database, and API services.
* **Navigation Compose:** Single-activity navigation architecture.

### 💾 Data & Networking
* **Room Database:** Local persistence for plant data.
* **DataStore (Preferences):** Storing user settings (Premium status, limits, notifications).
* **Retrofit & Gson:** Networking client for Weather API calls.
* **Coroutines & Flow:** Asynchronous programming and reactive state management.

### ☁️ Cloud & Services
* **Google Generative AI SDK:** Integration with **Gemini Flash** model for the chat assistant.
* **Google Mobile Ads SDK (AdMob):** Implementation of:
    * *App Open Ads*
    * *Native Advanced Ads* (Custom Compose Implementation)
    * *Rewarded Video Ads*
* **OpenWeatherMap API:** Real-time weather data fetching.

### ⚙️ Background Processing
* **WorkManager:** Robust scheduling for background tasks:
    * `MorningWorker`: Checks weather and engagement.
    * `NoonWorker`: Checks watering schedules.


---

## 🚀 Getting Started

To run this project locally, you will need to set up a few API keys:

1.  **Clone the repository.**
2.  **Open in Android Studio.**
3.  **Configure API Keys:**
    * Open `WeatherApi.kt` / `PlantViewModel.kt` and insert your **OpenWeatherMap Key**.
    * Open `ChatViewModel.kt` and insert your **Gemini AI API Key**.
4.  **Sync Gradle** and Run on an Emulator/Device.

---

## 👨‍💻 Created By

**Liad Nave**
*Product Development Project - Afeka College*
