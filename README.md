# Finance Tracker app - Android version

# Key Features

Comprehensive Expense Tracking: Log transactions with categories, dates, and custom notes :

- Automated Data Extraction: Integration with PDFBox-Android to parse bank statements and automatically populate transactions, reducing manual entry errors.
- Local Data Persistence: Uses Room Database for offline-first capability, ensuring user data never leaves the device.
- Advanced Statistics: Visual breakdown of spending habits and budget adherence.
- Customizable Ecosystem: Support for custom categories and multi-currency configurations.

# Tech Stack & Tools
- Language: Kotlin + Coroutines (Asynchronous programming)
- UI Framework: Jetpack Compose (Material 3 Design System)
- Architecture: MVVM (Model-View-ViewModel)
- Database: Room (SQLite abstraction layer)
- DI/Factory: Custom ViewModelProviders for dependency injection.

# Libraries:
- PDFBox-Android: For document parsing.
- Kotlinx Serialization: For efficient JSON handling.
- Lifecycle Runtime Compose: For state-aware UI components.
