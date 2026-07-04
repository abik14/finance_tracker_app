# Finance Tracker app - Android version

A modern, privacy-focused Android application designed to help users take control of their financial health. Built with Kotlin and Jetpack Compose, this project showcases a robust implementation of the MVVM architecture, and local data persistence with a soft & welcoming desgin.


## Features

### Core Functionality
- **Transaction Management** – Create, edit, delete, and categorize transactions with support for multiple currencies
- **Budget Planning** – Set monthly/quarterly budgets per category with real-time progress tracking
- **Multi-Currency Support** – Manage finances across different currencies with custom exchange rates
- **Custom Categories** – Create personalized spending categories tailored to your financial habits

### Smart Features
- **📄 Automated Statement Import** – Extract financial data from PDF bank statements using PDFBox (supports multiple bank formats)
- **📈 Interactive Dashboards** – Visual spending breakdowns, income/expense trends, and cash flow analysis

### Technical Highlights
- **100% Offline** – All data stored locally using Room database (no cloud dependency)
- **Smooth Performance** – Coroutine-based async operations for lag-free UI
- **Material 3 Design** – Modern, welcoming and accessible UI with light/dark theme support

## Tech Stack & Tools
- Language: Kotlin + Coroutines (Asynchronous programming)
- UI Framework: Jetpack Compose (Material 3 Design System)
- Architecture: MVVM (Model-View-ViewModel)
- Database: Room (SQLite abstraction layer)
- DI/Factory: Custom ViewModelProviders for dependency injection.

## Libraries
- PDFBox-Android: For document parsing.
- Kotlinx Serialization: For efficient JSON handling.
- Lifecycle Runtime Compose: For state-aware UI components.



