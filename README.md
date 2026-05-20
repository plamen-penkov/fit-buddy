# 🏋️ FitBuddy

A **MyFitnessPal-inspired** calorie and macronutrient tracking app for Android, built entirely with **Kotlin**. FitBuddy helps you log meals, set daily calorie goals, configure macro splits, and visualize your nutrition trends — all from a clean, modern interface.

---

## ✨ Features

### 📊 Dashboard
- At-a-glance view of your **daily calorie goal**, calories consumed, and calories remaining.
- Custom-drawn **circular progress ring** that fills as you eat throughout the day.
- Displays base goal and consumed stats side-by-side.

### 📓 Food Diary
- Log food entries organized by **meal category** — Breakfast, Lunch, Dinner, and Snacks.
- Each entry tracks **protein, carbs, and fats** (in grams); calories are **auto-calculated** using the standard `(P×4) + (C×4) + (F×9)` formula.
- **Daily summary card** shows total calories and a macro breakdown at a glance.
- **Swipe gestures** and arrow buttons to navigate between days with smooth slide-in/out animations.
- Friendly date labels (Today, Yesterday, Tomorrow) for quick context.
- Tap any food entry to **delete** it via a confirmation dialog.

### 📈 Weekly Stats
- **Bar chart** showing the last 7 days of nutritional data, rendered with a fully custom `Canvas`-based view.
- Toggle between **Calories, Protein, Carbs, and Fats** with a segmented pill-style control — each stat gets its own accent color.
- Rounded bars with value labels above each column.

### ⚙️ Settings
- Set your **daily calorie goal**.
- Configure **macronutrient distribution** as percentages (e.g. 30% Protein / 40% Carbs / 30% Fats) with built-in validation ensuring they total exactly 100%.
- All preferences persist automatically via **SharedPreferences**.

### 🧭 Navigation
- Consistent **Material Bottom Navigation Bar** across all screens (Dashboard, Diary, Stats, More).
- Seamless, animation-free transitions between tabs.

### 🎨 Splash Screen
- Branded splash screen using the **AndroidX Core SplashScreen** library with a custom gradient fitness logo.

---

## 🏗️ Architecture & Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |
| **UI Framework** | Programmatic Android Views (no XML layouts) |
| **Navigation** | Material `BottomNavigationView` + manual `Intent`-based routing |
| **Data Persistence** | `SharedPreferences` + Gson serialization |
| **Custom Graphics** | `Canvas` API for circular progress and bar chart |
| **Splash Screen** | AndroidX Core SplashScreen 1.2.0 |
| **Layout System** | `ConstraintLayout`, `RelativeLayout`, `LinearLayout` |
| **Build System** | Gradle (Kotlin DSL) with Version Catalog |
| **JDK** | 21 |

---

## 📁 Project Structure

```
fit-buddy/
├── app/
│   └── src/main/
│       ├── java/com/example/fit_buddy/
│       │   ├── activities/
│       │   │   ├── DashboardActivity.kt    # Home screen with calorie progress ring
│       │   │   ├── DiaryActivity.kt        # Food diary with swipe navigation
│       │   │   ├── StatsActivity.kt        # Weekly bar chart analytics
│       │   │   ├── MoreActivity.kt         # Settings menu & profile hub
│       │   │   └── SettingsActivity.kt     # Calorie goal & macro config
│       │   └── utils/
│       │       ├── DataManager.kt          # Singleton for data persistence & food logic
│       │       └── NavigationHelper.kt     # Reusable bottom nav setup
│       ├── res/
│       │   ├── drawable/                   # Vector logos & splash assets
│       │   ├── mipmap-*/                   # Launcher icons (all densities)
│       │   └── values/                     # Colors, strings, themes
│       └── AndroidManifest.xml
├── gradle/
│   └── libs.versions.toml                  # Centralized dependency versions
├── build.gradle.kts                        # Root build config
├── settings.gradle.kts                     # Project settings
└── gradle.properties                       # JVM & AndroidX config
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 21**
- **Android SDK** with API level 36 installed

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/fit-buddy.git
   cd fit-buddy
   ```

2. **Open in Android Studio**
   - File → Open → select the `fit-buddy` root directory.
   - Let Gradle sync complete automatically.

3. **Run the app**
   - Select an emulator or connected device (API 24+).
   - Click **Run ▶** or press `Shift + F10`.

### Build APK

```bash
./gradlew assembleDebug
```

The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📦 Dependencies

| Dependency | Purpose |
|---|---|
| `androidx.core:core-ktx` | Kotlin extensions for Android |
| `androidx.appcompat:appcompat` | Backward-compatible UI components |
| `androidx.constraintlayout:constraintlayout` | Flexible layout system |
| `com.google.android.material:material` | Bottom navigation & Material theming |
| `com.google.code.gson:gson` | JSON serialization for food data |
| `androidx.core:core-splashscreen` | Modern splash screen API |
| `androidx.lifecycle:lifecycle-runtime-ktx` | Lifecycle-aware coroutine scope |
| `androidx.compose.*` | Compose BOM (available for future migration) |

---

## 🗃️ Data Model

### `FoodItem`

```kotlin
data class FoodItem(
    val name: String,      // e.g. "Chicken Breast"
    val protein: Int,      // grams
    val carbs: Int,        // grams
    val fats: Int,         // grams
    val date: String,      // "yyyy-MM-dd"
    val mealType: String   // "Breakfast" | "Lunch" | "Dinner" | "Snacks"
) {
    val calories: Int      // auto-computed: (P×4) + (C×4) + (F×9)
}
```

All food data is stored as a JSON array in `SharedPreferences` and loaded/saved via the `DataManager` singleton.

---

## 🛣️ Roadmap

- [ ] User profile screen
- [ ] Meal reminders / notifications
- [ ] Food search with a nutrition API
- [ ] Exercise & activity tracking
- [ ] Weight tracking with trend graphs
- [ ] Dark mode support
- [ ] Room database migration for scalable storage
- [ ] Export/import data (CSV/JSON)

---

## 🤝 Contributing

Contributions are welcome! To get started:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  Built with 💪 and Kotlin
</p>
