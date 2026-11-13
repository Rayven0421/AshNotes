# AshNotes

AshNotes is an offline-first Android application that seamlessly integrates a block-based rich-text note editor with intelligent budget and expense tracking. Built with modern Android architectural standards and Jetpack Compose, AshNotes provides a clean, quiet, and data-safe environment for personal productivity and financial management.

---

## Executive Summary

- **Offline-First Architecture**: Room Database serves as the single source of truth. All core functions operate locally without network dependency.
- **Unified Document Model**: Notes are structured as versioned, serializable block-based documents supporting 12+ block types, live budget widgets, and rich text formatting.
- **Precision Financial Calculations**: Monetary values are strictly stored and calculated as integer minor units (`Long`) via `BudgetCalculator`, completely eliminating floating-point rounding errors.
- **Ash Design System**: A dark-themed, low-contrast UI built with Material 3 tokens, tailored typography (Bricolage Grotesque and Instrument Sans), and custom visual components like the signature Budget Meter.
- **Data Safety & Cloud Sync**: Versioned JSON backup/restore via Storage Access Framework (SAF), soft deletion workflows, and Firebase Authentication & Cloud Firestore integration.

---

## Technical Architecture

AshNotes adheres to Clean Architecture and MVVM principles, enforcing strict unidirectional data flow and modular layer separation.

```
+-------------------------------------------------------------------+
|                        Feature Layer (UI)                         |
|   Compose Screens, ViewModels, ViewStates (StateFlow), Dialogs    |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                           Domain Layer                            |
|   Pure Kotlin Models (Document, Block, Money, Budget),            |
|   Repositories, Use Cases (BudgetCalculator, ChecklistOperations) |
+-------------------------------------------------------------------+
                                  ^
                                  |
+-------------------------------------------------------------------+
|                            Data Layer                             |
|   Room Entities & DAOs, Migrations, Mappers, DataStore,           |
|   Firebase Auth & Cloud Backup Repositories                       |
+-------------------------------------------------------------------+
```

### Layer Responsibilities

- **Feature Layer**: Implements Jetpack Compose UI components and ViewModels exposing single `StateFlow` UI states. UI state collection uses `collectAsStateWithLifecycle()`.
- **Domain Layer**: Pure Kotlin models (`Document`, `Block`, `Money`, `Budget`), repository interfaces, and pure business logic (`BudgetCalculator`, block operation utilities). Independent of Android framework dependencies.
- **Data Layer**: Manages local data persistence with Room DAOs (Workspace, Note, Budget, Expense, Category, SyncQueue, SyncMetadata), SQLite migrations, SAF backup export/import, and Firebase integration.

---

## Current Implementation Status

AshNotes has successfully completed foundational and core architectural phases:

- **Phase 1 (Foundation)**: Fully implemented with Jetpack Compose, Material 3 dark theme (`AshNotesTheme`, design tokens), type-safe navigation, bottom bar tab-state preservation, and `UiState` architecture.
- **Phase 2 (Local Database)**: Fully implemented Room database with versioned schema exports (`schemas/`), 7 entities, DAOs with Flow observation, transaction support, automated workspace creation, and default category seeding.
- **Phase 3 (Editor Core & Block Model)**: Complete `Document` and `Block` sealed interface model (`Paragraph`, `Heading`, `BulletList`, `NumberedList`, `Checklist`, `Quote`, `Code`, `Divider`, `Table`, `Image`, `Budget`, `ExpenseList`, `BudgetChart`, `UnknownBlock`), paragraph/checklist operations, and test harness (`EditorScreenTest`).
- **Phase 4 (Budget System & Calculations)**: Robust integer minor-unit `Money` handling, `BudgetCalculator` domain logic (spent, remaining, percent used, over-budget detection), and currency formatting.
- **Phase 5 (Data Safety & Backup)**: Storage Access Framework (SAF) versioned JSON export and import with duplicate-id handling and relationship preservation.
- **Phase 6 & Core Repositories**: Implemented `AuthRepositoryImpl`, `CloudBackupRepositoryImpl`, and full repository layer for workspaces, notes, budgets, expenses, and categories.

---

## Technology Stack

- **Language**: Kotlin 2.1+
- **UI Toolkit**: Jetpack Compose (Compose BOM), Material 3
- **Architecture**: MVVM, Clean Architecture, Unidirectional Data Flow
- **Dependency Injection**: Hilt
- **Database**: Room Database (Schema export enabled, explicit version migrations 1->2->3)
- **Asynchronous Operations**: Kotlin Coroutines, StateFlow, SharedFlow
- **Serialization**: kotlinx.serialization
- **Network & Cloud**: Firebase Authentication, Cloud Firestore
- **Preferences**: Jetpack DataStore
- **Background Tasks**: WorkManager
- **Testing**: JUnit 4, Turbine, Instrumented Room & Migration Tests (`DaoTest`, `MigrationTest`)

---

## Project Structure

```
com.rayven0421.ashnotes/
├── AshNotesApp.kt             # Application entry point (@HiltAndroidApp)
├── MainActivity.kt            # Main activity entry point (@AndroidEntryPoint)
├── core/
│   ├── common/                # UiState, Result, Date/Money utilities, BiometricUtils
│   └── designsystem/          # Ash tokens, Theme, Color, Type, Shape, BudgetMeter, Dialogs
├── data/
│   ├── local/                 # Room database, Entities, DAOs, Migrations
│   ├── mapper/                # Entity <-> Domain mappers
│   ├── remote/                # Firebase Auth and Cloud backup implementations
│   └── repository/            # Data layer repository implementations
├── di/                        # Hilt modules (DatabaseModule, RepositoryModule, AuthModule)
├── domain/
│   ├── model/                 # Note, Document, Block, Budget, Expense, Money, BudgetCalculator
│   ├── repository/            # Abstract domain interfaces
│   └── usecase/               # Domain business logic & operations
└── feature/
    ├── authentication/        # Login and registration flows
    ├── budget/                # Budget management and expense entry UI
    ├── editor/                # Block and rich-text note editor UI
    ├── home/                  # Overview, active budget, recent notes UI
    ├── notes/                 # Note collection, search, pin/archive UI
    └── settings/              # System settings, backup/restore, workspace UI
```

---

## Setup and Installation

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer.
- JDK 17 or higher.
- Android SDK 35 / API Level 24 minimum.

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/rayven0421/AshNotes.git
   cd AshNotes
   ```

2. Configure `local.properties`:
   Ensure your local Android SDK path is specified in `local.properties`:
   ```properties
   sdk.dir=/path/to/android/sdk
   ```

3. (Optional) Configure Firebase Services:
   Place your `google-services.json` configuration file under the `app/` directory if cloud synchronization features are required.

4. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

5. Run unit tests:
   ```bash
   ./gradlew test
   ```

---

## Roadmap & Future Enhancements

- **UI & Feature Integration**: Wiring domain use cases and repository flows directly into feature UI screens for fully interactive notes and budget management.
- **Advanced Analytics & Charts**: Rich visual breakdown charts (`BudgetChartBlock`) and historical spending trends.
- **Biometric Security**: Fingerprint / Face Unlock protection for sensitive vaults and confidential notes.
- **Recurring Expenses**: Automated scheduling for subscriptions and fixed overhead costs.

---

## Verification & Quality Assurance

- **Unit Testing**: JVM unit tests covering financial calculations (`BudgetCalculator`), money formatting (`MoneyUtils`), block operations, and document serialization.
- **Database Migrations**: Instrumentally verified Room schema migrations ensuring zero data loss (`MigrationTest`, `DaoTest`).
- **Accessibility**: Interface components adhere to WCAG contrast standards, 48dp touch target guidelines, and TalkBack semantics.

---

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.
