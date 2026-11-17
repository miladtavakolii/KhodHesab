# KhodHesab (خودحساب) — Architecture Guide

KhodHesab is an offline-first, privacy-focused SMS personal accounting Android application tailored for Iranian bank transactions.

## Clean Architecture Layers

```
[ UI Layer ] (Jetpack Compose, ViewModels, Navigation)
     │
     ▼
[ Domain Layer ] (Use Cases, Domain Models, Repository Interfaces) — Pure Kotlin
     ▲
     │
[ Data Layer ] (Room Database, Repositories, DAOs) & [ SMS Engine ] (Regex Parsers, Importers)
     │
     ▼
[ Core Layer ] (Persian Normalizers, Jalali Calendar, DispatcherProvider, Security Hasher) — Pure Kotlin
```

### Layer Responsibilities
1. **Core (`ir.khodhesab.core`)**: Pure Kotlin utilities (zero Android dependencies where possible). Includes `PersianTextNormalizer`, `JalaliCalendarHelper`, `SmsHashGenerator`, and `DispatcherProvider`.
2. **Domain (`ir.khodhesab.domain`)**: Core business entities and use cases. Defines repository interfaces and business rules.
3. **Data (`ir.khodhesab.data`)**: Implementation of repositories, Room DB entities, DAOs, and data mappers.
4. **SMS Engine (`ir.khodhesab.sms_engine`)**: SMS receivers, bank-specific regex parsers, and bulk importer engines.
5. **UI (`ir.khodhesab.ui`)**: Jetpack Compose screens, Material3 theme, navigation graphs, and stateful ViewModels.

## Core Utilities Usage

### 1. Persian Text Normalizer (`PersianTextNormalizer`)
Normalizes Persian/Arabic characters, converts Persian & Arabic digits (`۰-۹`, `٠-٩`) to English digits (`0-9`), cleans thousand separators (`1,250,000`), and extracts clean `Long` financial amounts.

```kotlin
val normalizedText = PersianTextNormalizer.normalize("بانك ملي\r\nمبلغ: ۱,۲۵۰,۰۰۰ ريال")
val amount = PersianTextNormalizer.extractAmount("۱,۲۵۰,۰۰۰ ریال") // 1250000L
```

### 2. Jalali Calendar Helper (`JalaliCalendarHelper`)
Handles bidirectional Gregorian <-> Jalali date conversion, leap year detection, date formatting, and financial period boundaries (day, Iranian week Saturday–Friday, and Jalali month).

```kotlin
val jalaliDate = JalaliCalendarHelper.gregorianToJalali(2024, 3, 20) // JalaliDate(1403, 1, 1)
val (monthStart, monthEnd) = JalaliCalendarHelper.getMonthRange(System.currentTimeMillis())
val formatted = JalaliCalendarHelper.formatText(jalaliDate) // "۱ فروردین ۱۴۰۳"
```

### 3. SMS Hash Generator (`SmsHashGenerator`)
Produces deterministic SHA-256 hashes to prevent duplicate SMS imports into the local database.

```kotlin
val hash = SmsHashGenerator.generateHash(sender = "BANK-MELLI", normalizedBody = body, timestampBucket = timestamp)
```

## Testing & Commit Standards
- **Testing:** 100% unit test coverage required for core logic and domain use cases. Run `./gradlew testDebugUnitTest`.
- **Commits:** Conventional Commits standard (`feat:`, `chore:`, `test:`, `docs:`).
