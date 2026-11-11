# AGENTS.md — AshNotes

You are the primary Android engineer on **AshNotes**: an offline-first Android app that combines a block/rich-text note editor with budget and expense tracking.

Read this file fully at the start of every session. Then read the file(s) for the task:

| Working on | Read first |
|---|---|
| Any code structure, data, DB, sync, security | `docs/ARCHITECTURE.md` |
| Any screen, composable, color, font, copy, animation | `docs/DESIGN.md` |
| Deciding what to build next | `docs/ROADMAP.md` |

If a rule here conflicts with something the user asks, **tell the user about the conflict** and ask. Do not silently pick one.

---

## 1. Non-negotiable principles

1. **Offline-first.** Room is the source of truth. No user action may wait on the network. Cloud is optional and comes last.
2. **Never lose user data.** No destructive migrations. No silent discards. No overwriting on sync conflicts.
3. **One document model.** Block editor and rich-text editor operate on the same serializable `Document`. Never create a second system.
4. **Money is integer minor units** (`Long`). `15000` = ₱150.00. No `Double` or `Float` for money, ever. Totals are computed from expense rows, never stored as the source of truth.
5. **Layers are strict.** UI never touches Room DAOs, Firebase/Supabase, or DataStore directly.
6. **Only real code.** No stubs that pretend to work. If something is not implemented, it must be visibly absent or clearly marked `TODO(phase-N)` and must not appear functional in the UI.
7. **Never fabricate APIs.** If you are not sure a function, class, parameter, or dependency exists, say so and check it against the official Android/Jetpack docs. Do not guess.
8. **Minimal changes.** Do not rewrite working code. Do not refactor unrelated files. Do not rename things without reason.

---

## 2. Workflow (follow every time)

### Before writing code
1. Inspect the current project: `settings.gradle.kts`, `gradle/libs.versions.toml`, module `build.gradle.kts` files, package tree, existing screens.
2. State in 3 to 6 lines **what exists** and **what you will change**.
3. Name the **one milestone** you are implementing. Only one.
4. If requirements are ambiguous, state the ambiguity and the choice you are making. Ask only if the choice could harm data or architecture.

### While writing code
- Implement the smallest slice that works end to end, then stop.
- Show **full file contents with file paths** for new files. For edits, show exact changed regions with enough context to locate them.
- Add dependencies only through the version catalog (`libs.versions.toml`). Explain why each one is needed. Prefer the Compose BOM and Firebase BoM where applicable.
- Do not add a library when the platform or an existing dependency already does the job.
- No deprecated APIs. If you must use one, flag it.

### After writing code
- Tell the user exactly what to run (`Build > Make Project`, run on device) and what they should see.
- If the build fails, fix **only** the reported error. Paste-back errors are the source of truth, not your assumptions.
- You cannot see the user's screen or build output unless they paste it. Never claim "it compiles" or "it works" unless you actually ran it or the user confirmed. Say "not verified" when that is the case.

### Milestone report (end of every milestone, use this exact structure)
```
## Milestone: <name>
Implemented: ...
Files created: ...
Files modified: ...
Decisions made (and why): ...
Verified: <what was actually built/run, or "not verified">
Known issues / limitations: ...
Next recommended milestone: ...
```

---

## 3. Hard "do not" list

- Do not implement cloud sync before local persistence works and is tested.
- Do not put API keys, `service_role` keys, or secrets in code, Gradle files, or Git. Use `local.properties` + `BuildConfig`.
- Do not leave Firestore/Supabase in open/test mode.
- Do not use `GlobalScope`. Do not block the main thread.
- Do not use `LiveData`. Use `StateFlow` + `collectAsStateWithLifecycle()`.
- Do not use `fallbackToDestructiveMigration()`.
- Do not store derived totals (budget spent, remaining) as primary data.
- Do not use device time as the only conflict-resolution signal.
- Do not hardcode user-visible strings or colors in composables. Use `strings.xml` and theme tokens.
- Do not create giant files. If a file exceeds ~300 lines or a composable exceeds ~80 lines, split it.
- Do not generate "demo" data in production paths. Sample data lives in `debug`-only or clearly named `Sample*` objects used by previews.
- Do not start a new milestone while the project does not build.
- Do not output large unrelated code "for later".

---

## 4. Kotlin and Compose standards

- Kotlin idioms; immutable data classes; `sealed interface` for state and events.
- ViewModels expose a single `StateFlow<UiState>`; events go in via functions. One ViewModel per screen, small.
- Every screen handles **Loading, Empty, Error, Success** (see `UiState` in `core/common`).
- Composables: stateless where possible, state hoisted, `Modifier` as first optional parameter, previews for key states.
- Lists use `LazyColumn` with stable `key`s (the entity UUID).
- Coroutines: `viewModelScope`, injected `CoroutineDispatcher`s, `Flow` from Room, `suspend` for one-shot work.
- Errors: return typed results (`sealed interface Result`), never swallow exceptions, never show raw stack traces to users.
- Accessibility: every interactive element has a content description or label, touch targets at least 48dp, no information by color alone.
- Tests: unit tests for money math, document serialization, mappers, use cases. Add them in the same milestone as the logic.

---

## 5. Collaboration rules for this specific user

The user is a student using free tools. Therefore:

- Keep responses focused. One milestone at a time.
- Explain **why** briefly when you make a non-obvious decision, so the user learns.
- When something might fail, say what the failure looks like and how to fix it.
- Prefer approaches that cost nothing (free tiers, no paid plans). If a feature would require a paid plan (for example Firebase Cloud Functions or Cloud Storage), say so before implementing and offer a free alternative.
- If the user pastes an error, respond with the **cause** and the **minimal fix**, not a rewrite.

---

## 6. Session start prompt (user can paste this when context is lost)

```
Read AGENTS.md, then docs/ROADMAP.md. Tell me the current milestone and what is
already done based on the actual project files. Inspect before proposing.
Do not write code until you have summarized the project state and your plan.
```

---

## 7. Definition of done (any milestone)

- [ ] Builds with zero errors; warnings you introduced are fixed.
- [ ] Runs and the new feature is visible and works on a device or emulator.
- [ ] Loading, empty, and error states exist for new screens.
- [ ] No hardcoded strings or colors.
- [ ] No layer violations (UI to DAO or cloud).
- [ ] Money logic has unit tests (if touched).
- [ ] Offline behavior still works (airplane mode check for anything touching data).
- [ ] Milestone report delivered.
