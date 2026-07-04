# Moments 📸

A minimalist photo-quest game designed to turn real life into aesthetic memories. Build your personal, curated photo journal by completing daily and random photography quests.

![Moments Banner](https://via.placeholder.com/800x400?text=Moments+App)

## Features ✨

*   **Daily Quests**: Get a unique, curated photography quest every day to challenge your creativity.
*   **Streak & Emblems**: Keep up your daily habit and unlock Bronze (7 Days), Silver (30 Days), and Gold (100 Days) Emblems.
*   **Quest Packs**: Choose from various packs like Mindfulness, Urban Explore, Walking, Couple, and The Grand Collection (Mega Pack).
*   **Custom Packs & QR Sharing**: Create your own custom quest packs and share them with friends via QR code.
*   **Offline First**: Play the core game entirely offline. Server connection is optional and can be toggled in Developer Settings.
*   **Archive & Favorites**: Save your completed quests to your personal gallery. Mark your best shots as favorites to show them off on your profile.
*   **Local Persistence**: All your memories are securely stored locally on your device using a Room database.
*   **Aesthetic UI**: Modern, clean Material Design 3 interface featuring edge-to-edge support and dynamic layouts.

## Getting Started 🚀

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/moments.git
    ```
2.  **Open in Android Studio:**
    Import the project into Android Studio (Hedgehog or newer recommended).
3.  **Build and Run:**
    Sync the Gradle files and run the app on an emulator or physical device.

## Usage 💡

*   **Home Screen**: Start your daily quest, view your current streak, and roll random quests from your installed packs.
*   **Packs Screen**: Browse and install new quest packs. Toggle the "Server Connection" in settings to fetch community packs online.
*   **Archive Screen**: View your curated photo journal. Zoom in and out to see your polaroid-style memories. Mark favorites!
*   **Profile Screen**: Create custom packs, view your achievements and active streaks, and share your profile QR code.

## Technologies Used 🛠️

*   **Kotlin & Jetpack Compose**: Fully built with modern declarative UI.
*   **Room Database**: For local, offline data persistence.
*   **Coil**: For efficient image loading and caching.
*   **CameraX (via Intents)**: For capturing high-quality moments.
*   **ZXing**: For QR code generation and scanning.
*   **Moshi & Retrofit**: For network operations and JSON parsing (when server is enabled).

## Contributing 🤝

Contributions are welcome! Feel free to open an issue or submit a pull request if you'd like to add new quest packs or features.

## License 📄

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
