# DESIGN.md — AshNotes

Design brief for the agent. The goal: AshNotes should look like **a specific product with a point of view**, not a generated Material 3 template.

The user's fixed requirements (these always win):
- Dark theme: near-black charcoal base, dark purple-gray secondary surfaces.
- Accent: purple `#875FFF`.
- Rounded corners, subtle borders, minimal shadows.
- Material 3 components, responsive, restrained animation.
- Premium, minimal, uncluttered.

Everything else in this file is how to meet those requirements **without** falling into the default look.

---

## 1. Identity: "ash"

The name is the metaphor. Ash is soft, matte, quiet, with a small ember of color in it. So:

- The interface is **matte and quiet**: low-contrast surfaces, no glossy gradients, no glow.
- The accent violet is the **ember**: used rarely and with purpose (primary action, the focused thing, the live budget meter). If violet is on more than roughly 10 percent of a screen, remove some.
- One **signature element** carries the personality: the **budget meter** (section 5). Everything else stays calm.

Spend boldness in one place. Cut decoration that does not carry information.

---

## 2. Color tokens

Define these once in `core/designsystem` (`Color.kt` + `AshNotesTheme`). **No hex values outside this file.** Force dark theme; do **not** use dynamic color (Material You), because it would override the identity.

| Token | Hex | Role |
|---|---|---|
| `ash.base` | `#121116` | App background (a hint of violet, not pure black, not neutral gray) |
| `ash.surface` | `#1A1922` | Screens' main containers, bottom bar |
| `ash.raised` | `#232130` | Sheets, menus, focused editor block, text fields |
| `ash.line` | `#312E3F` | Borders and dividers (1dp) |
| `ash.text` | `#EAE7F1` | Primary text |
| `ash.textMuted` | `#A39FB4` | Secondary text, timestamps |
| `ash.ember` (accent) | `#875FFF` | Primary actions, selection, live meter |
| `ash.emberSoft` | accent at about 16 percent alpha over base | Selected row, chips, subtle emphasis |
| `ash.over` | `#E9795A` | Over-budget and destructive. The only warm color in the app |
| `ash.ok` | `#86B89C` | Positive/under-budget states (muted sage) |

Rules:
- Map tokens to a Material 3 `darkColorScheme`, so M3 components inherit them, and also expose a custom `AshColors` through `CompositionLocal` for the extra tokens (`line`, `over`, `ok`, `emberSoft`).
- Contrast: body text must meet WCAG AA against its surface. Check `textMuted` on `raised`.
- Never rely on color alone for meaning. Over-budget also gets an icon or text ("Over by ₱320").
- Do **not** use: default Material baseline purple, pure `#000000` backgrounds, rainbow category colors with equal saturation, gradient washes as backgrounds.

---

## 3. Typography

Use a deliberate pairing, loaded from Google Fonts (via the Compose Google Fonts provider) or bundled in `res/font`. Always define a fallback to the system font.

| Role | Face (suggested) | Use |
|---|---|---|
| Display / titles | **Bricolage Grotesque** | Screen titles, note titles, the big remaining amount |
| Body / UI | **Instrument Sans** | Everything else |

If either font fails to load or is unavailable, pick another **distinctive** pair. Do **not** fall back to "Roboto everywhere" as the design.

Rules:
- **Tabular figures for all money and counts**: `fontFeatureSettings = "tnum"`, so digits align in columns.
- Define a real type scale in `Type.kt` (sizes, weights, line heights, letter spacing). Use 4 to 5 levels, not every M3 slot.
- Money is typographically important: amount large and confident, currency symbol smaller and muted, minor units (centavos) slightly muted.
- Sentence case everywhere. **No ALL-CAPS tracked-out eyebrow labels** above sections. If a label is needed, it should earn its place.
- Do not accent a single word in a headline with a different color or italic.
- Line length in the editor: keep readable (about 60 to 75 characters on tablets by constraining max width).

---

## 4. Shape, depth, and spacing

### Shape (vary by role, not one radius everywhere)
| Element | Radius |
|---|---|
| Inputs, chips, small buttons | 8dp |
| List rows, editor blocks | 12dp (or none, when a row is separated by a divider) |
| Bottom sheets, dialogs, menus | 20dp (top corners for sheets) |
| Budget hero container | 24dp (the one big shape) |

### Depth
- **No drop shadows** on cards. Use tone (`surface` vs `raised`) and a 1dp `ash.line` border.
- Elevation only where it explains layering: menus and sheets.

### Spacing
- 4dp grid: 4, 8, 12, 16, 24, 32, 48. Put them in a `Spacing` object.
- Screen horizontal padding 20dp on phones, constrained max content width on tablets.
- Prefer **whitespace and dividers over wrapping everything in a card.**

### Anti-card rule
The default AI app is a screen full of identical rounded cards with the same shadow. Avoid it:
- Lists of notes and transactions are **rows**, not cards.
- Use a card/container only when it groups a distinct object (the budget hero, an embedded budget block in a note).
- No more than two container styles per screen.

---

## 5. Signature element: the Budget Meter

A thin horizontal meter, reused on the Budget screen, in `BudgetBlock`, and (compact) in lists.

- A 6dp-tall track in `ash.line`, with a fill in `ash.ember` from left to right for the share spent.
- A **pace tick**: a small vertical mark showing how far through the budget period we are today. Fill ahead of the tick = spending faster than time. This makes it **informative, not decorative**.
- When spent is greater than total: the fill turns `ash.over`, the track is full, and the overflow is shown as text ("Over by ₱320.00") instead of silently clamping.
- Zero budget: show an explicit "No amount set" state, not an empty bar and not NaN.
- Fill animates once on load and when values change (about 400 ms, standard easing). It respects reduced-motion settings.
- Semantics: expose "62 percent of ₱5,000 spent" to accessibility services.

The remaining balance is the headline of the Budget screen: large display type, tabular figures, with the meter right under it.

---

## 6. Screen concepts

Keep layouts opinionated and specific to the content.

### Home
Not a dashboard grid. A calm, single-column **"where things stand"** page:
1. A short, plain greeting line based on time of day (no exclamation marks, no "Welcome back!").
2. The active budget as one line plus meter (tap goes to Budget).
3. **Recent notes** as rows (title, first line, time).
4. A compact activity list (what changed, when).
5. Quick-create as two clear actions ("New note", "Add expense"), not a row of five tiles.
6. Sync status as a small, quiet line (for example, "Local only" or "Synced 3 min ago"). Never a loud banner.

### Notes
- Search at the top, always visible.
- **Pinned** notes in their own group at the top; the rest below. Rows, with a 1dp divider between them, not boxed cards.
- Row: title (display font, one line), preview (muted, up to two lines), updated time, small pin/archive indicators.
- Sort and filter live in a compact control, not a wall of chips.
- Empty state: one sentence and one action ("No notes yet. Start one." with a "New note" button). No illustrations of clipboards or rockets.

### Editor
The most important screen. It should feel like paper, not a form.
- Title in display type, large, no box around it.
- Blocks have no borders or filled background at rest or when focused. The focused block gets only a thin ember bar (2dp) on its left edge matching the text height; this shows where you are and where block actions apply.
- Block handles (drag/reorder, menu) appear on focus only.
- Toolbar docks above the keyboard: formatting actions, insert block, undo/redo. Icons plus labels in the insert menu.
- Budget blocks are visually distinct objects (a `raised` container with the meter), clearly interactive, never plain text.
- Insert menu: grouped (Text, Lists, Media, Budget) with a short description for each block type.

### Budget
1. Remaining balance (large), meter below it, then "₱X of ₱Y spent, N days left".
2. **Spending by category as a ranked list with proportional bars**, largest first. Do **not** default to a donut or pie chart.
3. Recent transactions as rows with category, description, amount (right aligned, tabular), date headers ("Today", "Yesterday", "Mon 3 Nov").
4. Budget switcher (multiple budgets, archive) as a simple selector at the top.
5. Add expense: bottom sheet with amount field first (numeric keypad, large), then category, then date, then note.

### Settings
Grouped plain rows with clear labels and one line of explanation where needed. No decorative icons on every row. Destructive actions (delete, wipe) are separated and use `ash.over`.

---

## 7. Charts

- **BudgetChartBlock** default: horizontal ranked bars grouped by category, labels and amounts visible, no legend lookup required.
- Draw with Compose `Canvas` or simple composables. Do not add a chart library unless the user agrees.
- Category colors: a small set of **muted, distinguishable** hues of similar lightness, plus a label/icon so color is never the only cue.
- Always show the actual amounts, and show the empty/zero state.

---

## 8. Iconography

- One consistent icon set (Material Symbols or Material Icons, one style: outlined). Consistent stroke weight.
- No emoji as UI icons. No mixed icon families.
- Category icons live in `CategoryEntity.icon` as a key that maps to a vector in the design system.

---

## 9. Motion

- **Motion answers user action**: expanding, reordering, confirming, saving. Show what changed.
- At most one non-triggered moment per screen (for example, the meter filling on first load).
- No staggered fade-and-slide entrance on every list item. No hover/press scale on every card.
- Durations: 150 to 250 ms for small state changes, about 400 ms for the meter fill and sheet transitions. Standard easing.
- Respect the system "remove animations" setting.

---

## 10. Writing (UI copy)

- Plain, specific, active voice. Sentence case.
- Name actions by what happens: "Save changes", not "Submit"; "Add expense", not "Create entry".
- Keep one name for each action across the whole flow. If the button says "Delete note", the confirmation says "Delete note?" and the toast says "Note deleted".
- Empty states say what to do. Errors say what happened and how to fix it. **No apologies, no vagueness** ("Couldn't save. Check storage space and try again.").
- No filler ("Welcome to your productivity journey", "Supercharge your notes").
- No exclamation marks, no emoji in system copy.
- Use realistic sample content in previews (an actual grocery note, a ₱1,250.50 electricity bill), never "Lorem ipsum" or "Item 1".
- All strings live in `strings.xml`.

---

## 11. Generic-look blacklist (reject these on sight)

If the output has any of these, fix it before delivering:

- Default Material baseline purple/teal, or dynamic color.
- Every screen built from identical rounded cards with the same soft shadow.
- Gradient backgrounds, gradient buttons, glow effects, glassmorphism.
- A big number + small label + gradient accent as the default hero.
- ALL-CAPS tracked-out section labels everywhere; numbered "01 / 02 / 03" markers on non-sequential content.
- Emoji used as icons; clip-art empty states.
- A donut chart as the automatic answer to "show spending".
- Roboto-only typography with no scale decisions.
- Copy like "Welcome back!", "Let's get started!", "Oops! Something went wrong."
- Staggered entrance animations on everything.
- Floating action button on every screen by reflex. (Use it only where one primary create action dominates, such as Notes.)

---

## 12. Process for any new screen (do this every time)

1. **Plan** (3 to 6 lines): what is the screen's one job, what is the one memorable element, which tokens it uses.
2. **Check against the blacklist** (section 11). If it reads like the default for this kind of screen, revise and say what you changed.
3. **Build** with tokens only, small composables, previews for Loading, Empty, Error, Success (including a long-text case and an over-budget case where relevant).
4. **Review**: remove one decorative element. Check contrast, touch targets (at least 48dp), font scaling at 200 percent, small phone width (360dp) and tablet width.

---

## 13. Quality floor

- Works on 360dp phones through tablets; content max-width on large screens.
- Respects system font scale and reduced motion.
- All interactive elements labeled for TalkBack, 48dp touch targets.
- Visible focus/selection states.
- No hardcoded colors, dimensions of repeated use, or strings in composables.
