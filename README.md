# AshNotes

AshNotes is an offline-first Android application that seamlessly integrates a block-based rich-text note editor with intelligent budget and expense tracking. Built with modern Android architectural standards and Jetpack Compose, AshNotes provides a clean, quiet, and data-safe environment for personal productivity and financial management.

---

## Executive Summary

- **Offline-First Architecture**: Room Database serves as the single source of truth. All core functions operate locally without network dependency.
- **Unified Document Model**: Notes are structured as versioned, serializable block-based documents that host interactive text content, live budget widgets, and expense lists.
- **Precision Financial Calculations**: Monetary values are strictly stored and calculated as integer minor units (`Long`), completely eliminating floating-point rounding errors.
- **Ash Design System**: A dark-themed, low-contrast UI built with Material 3 tokens, tailored typography (Bricolage Grotesque and Instrument Sans), and custom visual components like the signature Budget Meter.
- **Cloud Synchronization & Security**: Optional synchronization powered by Firebase Authentication and Cloud Firestore with transactional version checks and tombstone deletion handling.

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
|   Pure Kotlin Models (Document, Block, Money), Repositories,      |
|   Use Cases (BudgetCalculator, TextBlockOperations)               |
+-------------------------------------------------------------------+
                                  ^
                                  |
+-------------------------------------------------------------------+
|                            Data Layer                             |
|   Room Entities & DAOs, Mappers, DataStore, Firebase Remote       |
+-------------------------------------------------------------------+
```

### Layer Responsibilities

- **Feature Layer**: Implements Jetpack Compose UI components and ViewModels exposing single `StateFlow<UiState>` models. UI state collection uses `collectAsStateWithLifecycle()`.
- **Domain Layer**: Pure Kotlin models, repository interfaces, and business logic. Independent of Android framework dependencies, Compose, Room, or Firebase.
- **Data Layer**: Manages local data persistence with Room DAOs, remote Firebase services, DataStore preferences, and mapping functions.

---

## Key Features

### 1. Unified Block & Rich-Text Editor
- Support for structured block types: Paragraphs, Headings (H1, H2, H3), Bullet Lists, Numbered Lists, Checklists, Quotes, Code Blocks, Tables, Dividers, and Images.
- Live embedding of dynamic budget widgets (`BudgetBlock`, `ExpenseListBlock`, and `BudgetChartBlock`) directly inside notes.
- Inline rich-text formatting (Bold, Italic, Underline, Strikethrough, Code, Color, and Links).
- Debounced autosave mechanism (500–1000ms) with flush on lifecycle pauses and navigation.
- Full undo and redo history buffer.

### 2. Intelligent Financial & Expense Management
- Multi-currency support (defaulting to PHP) formatted dynamically via system locale and fraction digits.
- Expense tracking categorized by custom icons, descriptions, and timestamps.
- Derived financial computations computed dynamically from expense records:
  - Spent total sum calculation.
  - Remaining balance calculation with over-budget support.
  - Visual pace indicator showing spending velocity relative to elapsed time.
- Zero-budget handling and over-budget alerting.

### 3. Ash Aesthetic Design System
- Matte dark visual tokens engineered for high legibility and zero glare:
  - `ash.base` (`#121116`): App background surface.
  - `ash.surface` (`#1A1922`): Primary container surfaces and bottom navigation.
  - `ash.raised` (`#232130`): Dialogs, menus, and focused editor blocks.
  - `ash.ember` (`#875FFF`): Restrained accent color for focused actions and live meters.
  - `ash.over` (`#E9795A`): Destructive action and over-budget warning indicator.
  - `ash.ok` (`#86B89C`): Positive financial indicator.
- Custom typography pairing: Bricolage Grotesque for titles and Instrument Sans for interface text.
- Tabular figures (`tnum`) applied across numeric financial readouts for precise vertical column alignment.

### 4. Data Safety & Synchronization
- Soft deletion model (`deletedAt`) across database entities to prevent accidental data loss.
- Versioned JSON import and export framework using the Android Storage Access Framework (SAF).
- Cloud synchronization via Firebase Authentication and Cloud Firestore:
  - Queue-based background worker (`SyncQueueEntity`) with exponential backoff.
  - Transactional version validation (`baseVersion` matching) to resolve multi-device edit conflicts safely.
  - Tombstone deletion propagation.

---

## Technology Stack

- **Language**: Kotlin 2.1+
- **UI Toolkit**: Jetpack Compose (Compose BOM), Material 3
- **Architecture**: MVVM, Clean Architecture, Unidirectional Data Flow
- **Dependency Injection**: Hilt
- **Database**: Room Database (Schema export enabled, explicit version migrations)
- **Asynchronous Operations**: Kotlin Coroutines, StateFlow, SharedFlow
- **Serialization**: kotlinx.serialization
- **Network & Cloud**: Firebase Authentication, Cloud Firestore
- **Preferences**: Jetpack DataStore
- **Background Tasks**: WorkManager
- **Typography**: Compose Google Fonts Provider (Bricolage Grotesque & Instrument Sans)

---

## Project Structure

```
com.rayven0421.ashnotes/
├── AshNotesApp.kt             # Application entry point (@HiltAndroidApp)
├── MainActivity.kt            # Main activity entry point (@AndroidEntryPoint)
├── core/
│   ├── common/                # UiState, Result, Date/Money utilities, Dispatchers
│   └── designsystem/          # Ash tokens, Theme, Color, Type, Shape, BudgetMeter
├── data/
│   ├── local/                 # Room database, Entities, DAOs, Migrations
│   ├── mapper/                # Entity <-> Domain mappers
│   ├── remote/                # Firebase Auth and Firestore implementations
│   └── repository/            # Data layer repository implementations
├── di/                        # Hilt modules (DatabaseModule, RepositoryModule, AuthModule)
├── domain/
│   ├── model/                 # Note, Document, Block, Budget, Expense, Money
│   ├── repository/            # Abstract domain interfaces
│   └── usecase/               # Domain business logic (BudgetCalculator, etc.)
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

## Future Features & Roadmap

The development of AshNotes is structured across clear operational phases. The following features are planned for upcoming releases:

### Short-Term Enhancements
- **Advanced Block Types**:
  - Image block support with local storage caching and responsive rendering.
  - Markdown document import and export capabilities.
  - Multi-column grid blocks optimized for tablet and desktop screen layouts.
- **Full-Text Search & Filtering**:
  - Full-text search (FTS4/FTS5) in Room across note titles, text blocks, and expense details.
  - Tagging system with multi-criteria filtering.

### Mid-Term Enhancements
- **Workspace Sharing & Collaboration**:
  - Encrypted workspace invite codes for collaborative budgets and shared notes.
  - Granular user permissions per shared workspace.
- **Advanced Financial Analytics**:
  - Historical spending trends over customizable timeframes (weekly, monthly, annual).
  - Category breakdown visualization charts (`BudgetChartBlock`).
  - Transaction history export to CSV and PDF formats.

### Long-Term Enhancements
- **Biometric Security & Encrypted Notes**:
  - Biometric lock (Fingerprint / Face Unlock) for sensitive notes or budget logs.
  - Client-side AES-256 encryption for confidential financial records.
- **Automated Recurring Expenses**:
  - Scheduled expense generation for recurring subscriptions, utility bills, and fixed costs.
  - Due date notification alerts.

---

## Verification & Quality Assurance

- **Unit Testing**: Comprehensive JVM unit tests covering financial computations (`BudgetCalculatorTest`), money formatting/parsing (`MoneyUtils`), block text operations, and document serialization.
- **Database Migrations**: Instrumentally verified Room schema migrations ensuring zero data loss.
- **Accessibility**: Interface components strictly comply with WCAG AA contrast standards, 48dp touch target guidelines, and TalkBack accessibility labels.

---

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.
