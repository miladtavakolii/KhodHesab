# KhodHesab (خودحساب) — Offline-First SMS Personal Accounting App

KhodHesab is an offline-first, privacy-respecting Android application built with Jetpack Compose, Clean Architecture, Hilt, Room, and WorkManager to automatically track personal finances from Iranian bank transaction SMS messages.

## Features & Highlights
- **100% Offline & Private:** No external network requests; all data stays strictly on your device.
- **Smart SMS Parsing:** Automatic detection and parsing of Iranian bank transaction SMS messages (Bank Melli, Mellat, Saman, Pasargad, Sepah, Blue Bank, etc.).
- **Persian Calendar Native:** Full Jalali (Solar Hijri) calendar support with leap year accuracy and Iranian week reporting (Saturday to Friday).
- **Clean Architecture & SOLID:** Strict separation into Core, Domain, Data, SMS Engine, and UI layers.

## Tech Stack
- **Language:** Kotlin 2.1+
- **UI:** Jetpack Compose (Material 3), Navigation Compose
- **DI:** Dagger Hilt
- **Database:** Room with KSP
- **Asynchronous:** Kotlin Coroutines & Flow
- **Background Work:** WorkManager with Hilt Integration
- **Testing:** JUnit 4, MockK, Turbine, Truth

## Getting Started

### Prerequisites
- JDK 17
- Android Studio Ladybug or newer
- Android SDK 37 (Min SDK 24)

### Build Commands
```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest
```

## 8-Phase Development Roadmap
- [x] **Phase 0:** Project Setup, Architecture Skeleton & Core Utilities
- [x] **Phase 1:** Core Data Layer & Room Database Schema
- [ ] **Phase 2:** Advanced Bank Regex & SMS Engine
- [ ] **Phase 3:** Domain Layer & Financial Analytics Use Cases
- [ ] **Phase 4:** Background SMS Processing & Sync Worker
- [ ] **Phase 5:** Jetpack Compose UI Framework & Navigation
- [ ] **Phase 6:** Advanced Features (Budgeting, Multi-account, Backup)
- [ ] **Phase 7:** Final Polish, Performance Optimization & UI Testing

## License
Private / Open Source Project.
