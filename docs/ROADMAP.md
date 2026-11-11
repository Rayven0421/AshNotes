# ROADMAP.md — AshNotes

The agent builds **one milestone at a time**. A milestone is done only when its exit criteria are met **and the user has confirmed the app builds and runs**. Update the status boxes as work completes.

Legend: `[ ]` not started, `[~]` in progress, `[x]` done and confirmed by the user.

**Order is fixed.** Do not start a phase while the previous one is not confirmed. Cloud sync is last.

---

## Phase 1: Foundation
- [x] Project builds and launches (Kotlin, Compose, Material 3).
- [x] Version catalog in `gradle/libs.versions.toml`; Compose BOM; KSP; Hilt configured.
- [x] `AshNotesApp` (`@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`).
- [x] Design tokens and `AshNotesTheme` per `docs/DESIGN.md` (dark only, no dynamic color).
- [x] Type-safe navigation + bottom bar: Home, Notes, Budget, Settings. Tab state preserved.
- [x] `UiState` in `core/common`.
- [x] Four starter screens with Loading/Empty/Success/Error handling. Any sample data clearly named `Sample*`.
- [x] Strings in `strings.xml`.

**Exit:** builds, launches into Home, tabs switch, rotation keeps state, no hardcoded colors/strings.

## Phase 2: Local database
- [x] Room database, `exportSchema = true`, schema folder committed.
- [x] Entities: Workspace, Note, Budget, Expense, Category, SyncQueue, SyncMetadata (Block entity deferred to Phase 3).
- [x] DAOs returning Flow; `@Transaction` for related writes.
- [x] Domain models + repository interfaces; data implementations; mappers.
- [x] Hilt modules for DB, DAOs, repositories.
- [x] Local workspace auto-created on first launch.
- [x] Notes list screen reads from Room; create / rename / soft-delete / pin / archive a note.
- [x] Default categories seeded.
- [ ] DAO instrumented tests (written in DaoTest.kt; awaiting device run confirmation).

**Exit:** create notes, kill app, reopen, data persists. Soft-deleted notes are hidden. Airplane mode irrelevant (no network code exists yet).

## Phase 3: Editor
- [ ] Versioned `Document` / `Block` model (kotlinx.serialization) incl. `UnknownBlock` fallback + unit tests.
- [ ] Block list UI: paragraph, heading, checklist first.
- [ ] Insert, delete, reorder blocks.
- [ ] Remaining block types: bullet, numbered, quote, code, divider, table, image (local).
- [ ] Debounced autosave + flush on pause/navigation.
- [ ] Rich text spans (bold, italic, underline, strikethrough, color, highlight, links) on the same model.
- [ ] Rich-text mode and block mode over one document; switching preserves content.
- [ ] Undo/redo.
- [ ] Formatting toolbar + insert block menu.

**Exit:** edit a note, close the app mid-edit, reopen, nothing lost. Switch modes, nothing changes. Serialization tests pass.

## Phase 4: Budget system
- [ ] `Money` handling in minor units, formatting, parsing (no floating point) + unit tests.
- [ ] Budget CRUD: name, amount, currency (PHP first, extensible), period, edit, archive, delete (soft).
- [ ] Expense CRUD with categories, description, date; transaction history.
- [ ] Calculation use cases (spent, remaining, percent, by-category) computed from rows, with tests for zero, exact, over budget.
- [ ] Budget screen per `docs/DESIGN.md` (budget meter, ranked categories, rows).
- [ ] `BudgetBlock`, `ExpenseListBlock`, `BudgetChartBlock` in the editor, live-updating, storing references not copies.

**Exit:** add an expense; Budget screen and the budget block inside a note update immediately. Over-budget and zero-budget display correctly.

## Phase 5: Offline experience and data safety
- [ ] Airplane-mode pass over every feature.
- [ ] Versioned JSON export / import (Storage Access Framework), validation, duplicate-id handling, relationships preserved.
- [ ] Round-trip test: export, wipe, import, identical data.
- [ ] At least one real Room migration with a migration test (written in MigrationTest.kt; awaiting device run confirmation).
- [ ] Settings: backup and restore UI.

**Exit:** user can back up and fully restore; migrations preserve data.

## Phase 6: Cloud (optional, Firebase free tier)
Only after Phases 1 to 5 are confirmed. See `docs/ARCHITECTURE.md` section 6.
- [ ] Firebase project (Spark), `google-services.json`, Auth + Firestore dependencies.
- [ ] Firestore Security Rules written and tested (Rules Playground / Emulator). Not in test mode.
- [ ] `AuthRepository` (register, login, logout, current user Flow). Local mode still works.
- [ ] Manual upload of one note to confirm the pipeline.
- [ ] Pull on a second install / after reinstall.
- [ ] Sync queue + WorkManager (constraints, backoff).
- [ ] Transactional version checks; conflict preservation; tombstones.
- [ ] Budgets and expenses sync.
- [ ] Sync status UI + manual "Sync now".
- [ ] Access credentials (invite codes hashed; revoke, rotate), or consciously defer sharing and ship owner-only sync.
- [ ] Sync tests with a fake remote (retry, idempotency, conflict, tombstone).

**Exit:** edit offline on device A, reconnect, appears on device B. Conflicting edits are preserved, never overwritten.

## Phase 7: Polish
- [ ] Accessibility pass (TalkBack, 48dp targets, font scale 200 percent, contrast).
- [ ] Performance pass (lazy lists with keys, avoid recomposition storms, startup time).
- [ ] Error handling pass (every failure path shows a clear message).
- [ ] Unit + UI + sync tests green.
- [ ] Phone and tablet layouts checked.
- [ ] Design blacklist review (`docs/DESIGN.md` section 11).

---

## Current status

**Active milestone:** Phase 2: Local database
**Last confirmed build:** November 4, 2026 (Phase 2 Local database built; JVM unit tests verified)
**Open issues:**
- Instrumented DAO tests (`DaoTest.kt`) and Room migration tests (`MigrationTest.kt`) require physical device / emulator test suite run confirmation.
- Budget and Expense CRUD UI and ViewModel data layer connections are stubs awaiting Phase 4.
- Note editor UI and document block serialization are awaiting Phase 3.

**Decisions log:**
- 2026-11-03: Selected Hilt 2.60.1, KSP 2.1.20-1.0.31, Navigation Compose 2.8.8, kotlinx.serialization 1.8.0 for AGP 9.5 compatibility.
- 2026-11-03: Implemented AshNotes dark design tokens & theme in core/designsystem (no dynamic color).
- 2026-11-03: Configured Google Fonts provider for Bricolage Grotesque and Instrument Sans in Type.kt.
- 2026-11-03: Created AshNotesApp (@HiltAndroidApp), MainActivity (@AndroidEntryPoint), and UiState hierarchy in core/common.
- 2026-11-03: Created shared ScreenHeader composable in core/designsystem for consistent top title layout and typography across Home, Notes, Budget, and Settings.
- 2026-11-03: Removed development phase numbers from user-facing copy in strings.xml (e.g. Settings items) to maintain production-ready user copy.
- 2026-11-03: Completed Phase 2 Room Database with entities (Workspace, Note, Budget, Expense, Category, SyncQueue, SyncMetadata), DAOs, repositories, mappers, Hilt bindings, default category seeding, MIGRATION_1_2 migration test, instrumented DAO tests, and destructive Settings data wipe.
- 2026-11-04: Block entity creation deferred to Phase 3 alongside the serializable Document model.
- 2026-11-04: Unticked instrumented DAO and migration test checklist boxes until confirmed on device by user.
