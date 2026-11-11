# ARCHITECTURE.md — AshNotes

The rules for how the app is structured. Follow them exactly. If the project drifts from this file, fix the project or ask the user before changing this file.

---

## 1. Stack

Kotlin, Jetpack Compose, Material 3, Clean Architecture + MVVM, Hilt, Room, DataStore, WorkManager, Kotlin Serialization, Coroutines + Flow.

Cloud (Phase 6 only, optional): **Firebase** (Auth + Firestore) on the free Spark plan. The Firebase SDK replaces Ktor for networking, so do not add Ktor unless a real need appears.

Use versions that are compatible with each other. Declare them only in `gradle/libs.versions.toml`. Use the Compose BOM and the Firebase BoM so you do not pin individual library versions by hand.

---

## 2. Layers and dependency direction

```
feature (UI + ViewModel)  ->  domain (models, repository interfaces, use cases)  <-  data (Room, remote, repository impls)
```

- `domain` is pure Kotlin. **No Android imports, no Room, no Firebase, no Compose.**
- `data` implements `domain` interfaces. Only `data` may import Room or Firebase.
- `feature` depends on `domain` only. ViewModels call use cases or repository interfaces.
- `core/designsystem` holds theme and shared components. It knows nothing about data.
- `core/common` holds `UiState`, small utilities, dispatcher qualifiers.

Violations that must never happen:
- A composable or ViewModel importing a DAO, `FirebaseFirestore`, `FirebaseAuth`, or `DataStore`.
- A domain model annotated with `@Entity`.
- A repository that returns Room entities to the UI.

Start as **one Gradle `app` module with packages** unless the project already has modules. Split into modules only when the user asks.

---

## 3. Package layout

```
com.<user>.ashnotes/
├── AshNotesApp.kt            # @HiltAndroidApp
├── MainActivity.kt           # @AndroidEntryPoint
├── navigation/               # routes, NavHost, bottom bar
├── core/
│   ├── common/               # UiState, Result, dispatchers
│   └── designsystem/         # theme, tokens, shared components
├── domain/
│   ├── model/                # Note, Block, Document, Budget, Expense, Category, Money
│   ├── repository/           # interfaces
│   └── usecase/
├── data/
│   ├── local/                # Room db, entities, DAOs, converters, migrations
│   ├── remote/               # (Phase 6) Firebase data sources
│   ├── repository/           # implementations
│   └── mapper/
└── feature/
    ├── home/  notes/  editor/  budget/  expenses/  workspace/  authentication/  settings/
```

Each feature folder: `XScreen.kt`, `XViewModel.kt`, `XUiState.kt`, and a `components/` subfolder.

---

## 4. Data rules

### Identifiers and time
- All ids are **UUID strings**, generated on the client.
- Timestamps are epoch milliseconds (`Long`).
- Soft delete: `deletedAt: Long?`. Queries for the UI exclude deleted rows.

### Money
- Stored as `Long` minor units. A `Money`/amount value in domain carries `amountMinor: Long` and a `currencyCode: String`. Default currency: PHP.
- Formatting uses `java.util.Currency` / `NumberFormat`, based on currency fraction digits (do not hardcode 2 decimals).
- Parsing user input: parse text to `BigDecimal`, then to minor units. **Never** use `Double`.
- Derived values computed on read from expense rows:
  - `spent = sum(expense.amountMinor)` for non-deleted expenses of the budget
  - `remaining = total - spent` (**may be negative**; handle and display it)
  - `percentUsed`: if `total == 0` then define behavior explicitly (for example, 0 percent with an "unset" state if spent is 0, and "over" state if spent is greater than 0). **No division by zero.**
- Unit tests required for: zero budget, exact budget, over budget, one-unit rounding, large values.

### Room
- `exportSchema = true`. Commit the schema JSON files.
- Every schema change = version bump + `Migration` + migration test. **No destructive migrations.**
- Multi-row changes (for example, saving a note and its blocks) use `@Transaction`.
- DAOs return `Flow` for observed data and `suspend` for writes.
- Indices on foreign keys (`noteId`, `budgetId`, `workspaceId`) and on `deletedAt` where filtered.
- Foreign keys with explicit `onDelete` behavior. Because deletes are soft, hard cascades are only used by purge and restore operations.

### Document model (editor)
- One versioned, serializable model (kotlinx.serialization):
  - `Document(version: Int, blocks: List<Block>)`
  - `Block` is a `sealed interface` with an id, a position, timestamps, metadata, and typed content per block type.
- Rich text is stored as **spans over text** (start, end, style) inside block content, so both editors read the same data.
- Block types: Paragraph, Heading, BulletList, NumberedList, Checklist, Table, Image, Divider, Code, Quote, Budget, ExpenseList, BudgetChart.
- **Budget blocks store references (a `budgetId` and display options), not copies of amounts.** They observe live data, so they update when expenses change.
- Unknown future block types and versions must deserialize safely (use a fallback `UnknownBlock` that preserves raw content). Never drop data you do not understand.
- Switching editor modes must not alter the stored document.

### Autosave
- Edits update in-memory state immediately.
- Persistence is debounced (about 500 to 1000 ms) and also flushed on pause/stop/navigation away.
- Failures surface in the UI; they are never swallowed.

---

## 5. State and UI plumbing

- `UiState<T>`: `Loading | Empty | Success(data) | Error(message)`.
- ViewModel pattern: repository `Flow` mapped to `UiState`, then `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loading)`.
- Collect with `collectAsStateWithLifecycle()`.
- One-off events (snackbars, navigation) via a `Channel`/`SharedFlow` or state-consumed flags, not by re-triggering state.
- Navigation: type-safe routes. Preserve tab state across bottom bar switches.

---

## 6. Sync (Phase 6, Firebase). Not before local persistence is solid.

**Principle:** Room is truth. Firestore is a remote copy.

### Free-tier constraints (Spark plan)
- Free quotas are daily (about 20,000 writes and 50,000 reads per day at the time of writing; verify on the pricing page). Exceeding them stops the service until reset.
- Cloud Functions and Cloud Storage need a paid plan. **Do not design around them.** Images stay local in v1.

### Cost-aware design
- **One Firestore document per note**, with blocks embedded (mind the 1 MiB document limit). Room can keep blocks as rows; the mapper bundles them.
- Never sync per keystroke. Upload through WorkManager (network constraint, exponential backoff).
- Batch writes. Pull with "changed since last sync" queries. No whole-collection snapshot listeners.

### Correctness
1. Every local change: write to Room, then enqueue to `SyncQueueEntity` (in the same transaction).
2. Worker pushes queue items. Document id = local UUID, so retries are **idempotent**.
3. Push uses a **Firestore transaction**: read remote `version`; if it equals the local `baseVersion`, write with `version + 1`; otherwise it is a **conflict**.
4. Conflicts are **preserved** (conflict copy or conflict record the user can resolve). Never overwrite blindly.
5. Pull uses a **server timestamp** for ordering, not the device clock.
6. Deletions sync as tombstones (`deleted = true`).
7. Mark synced rows in `SyncMetadataEntity`. Expose sync status as a Flow to the UI.
8. A manual "Sync now" action must exist.

### Security
- Auth through Firebase Auth. Local mode (no account) must always work.
- **Firestore Security Rules are mandatory before any real data.** Never leave test mode on. Only signed-in members of a workspace may read or write it; only the owner changes membership.
- A workspace id is an identifier, **never** a credential. Access credentials (invites) are random, stored hashed server-side, revocable, and rotatable.
- No secrets in the repo. `google-services.json` is not committed to public repos.
- Logging out never deletes local data.

---

## 7. Backup and restore

- Versioned JSON (`backupVersion`), via Storage Access Framework (no storage permission needed).
- Validate before importing. Handle duplicate ids without data loss (skip, or remap while preserving relationships). Show a result summary.
- Backup must round-trip: export, wipe, import, identical data (tested).

---

## 8. Testing expectations

| Area | Tests |
|---|---|
| Money and budget calculations | JVM unit tests (required) |
| Document serialization and unknown blocks | JVM unit tests |
| Room DAOs and migrations | Instrumented tests |
| ViewModels | Unit tests with fake repositories + Turbine-style Flow assertions |
| Sync | Tests with a fake remote: retry, idempotency, conflict, tombstone |
| UI | Compose tests for critical flows (create note, add expense) |

---

## 9. When unsure

Choose the option that (1) protects user data, (2) keeps offline working, (3) keeps layers clean, (4) is the smallest change. Explain the choice in one or two sentences.
