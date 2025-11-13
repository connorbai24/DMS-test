# Repository Guidelines

## Project Structure & Module Organization
- `src/` - Java sources (entry point: `Game.main`; no packages)
- `resources/images/` - image assets loaded via classpath (`/images/...`)
- `out/` - compiled classes (IDE output); avoid committing new build artifacts
- `HighScores.txt` - survival-mode score storage (plain text)

## Build, Test, and Development Commands
- Build (CLI)
  - Windows: `mkdir out\classes; javac -d out\classes -cp resources src\*.java`
  - macOS/Linux: `mkdir -p out/classes; javac -d out/classes -cp resources src/*.java`
- Run
  - Windows: `java -cp "out\classes;resources" Game`
  - macOS/Linux: `java -cp "out/classes:resources" Game`
- IDE: open `tron-master.iml` in IntelliJ IDEA; mark `resources` as Resources Root

## Coding Style & Naming Conventions
- Java 8+; tabs for indentation; braces on same line
- Classes `UpperCamelCase`; methods/fields `lowerCamelCase`; constants `UPPER_SNAKE_CASE`
- Keep UI work on the EDT (`SwingUtilities.invokeLater`); avoid long tasks on the EDT
- Load images via `Picture.draw(g, "file.png", x, y)`; place files in `resources/images/`

## Testing Guidelines
- No automated tests in repo; manual checks: menu navigation, gameplay, collisions, boosts, and that `HighScores.txt` updates and keeps top 10
- If adding tests, use JUnit in `test/` and name `ClassNameTest.java`; focus on movement, collisions, and score updates

## Security & Configuration Tips
- Ensure `HighScores.txt` remains writable; do not use absolute paths; rely on classpath for images
- When packaging or deploying, include `resources/images/` on the classpath
## Classes & Responsibilities

- `Game` (src/Game.java) - Application entry; builds the Swing UI/menus and wires actions; instantiates `TronMapSurvival`, `TronMapTwoPlayer`, and `TronMapStory` and swaps panels.
- `TronMap` (src/TronMap.java) - Base panel for game modes; manages players, timer tick loop, Player 1 input, and drawing; provides abstract hooks `tick`, `reset`, `setScore`, `addScore`.
- `TronMapSurvival` (src/TronMapSurvival.java) - Single-player survival; updates score over time; persists high scores via `Score`; renders Game Over and provides a high-scores panel.
- `TronMapTwoPlayer` (src/TronMapTwoPlayer.java) - Two human players; adds WASD/Q/1 controls for P2; tracks round wins and displays P1/P2/tie banners.
- `TronMapStory` (src/TronMapStory.java) - Progressive levels vs AI; spawns more AIs each level; delays between levels; updates score and renders win/over banners.
- `GameObject` (src/GameObject.java) - Abstract moving entity; holds position, velocity, bounds; movement/clipping and collision vs trail `Shape`s; prevents immediate reverse direction.
- `Player` (src/Player.java) - Abstract light-cycle; trail path (`Line`s), boost timer, jump mechanic, alive state; implements drawing and crash handling; `move()` deferred to subclasses.
- `PlayerHuman` (src/PlayerHuman.java) - Human-controlled player; updates movement and merges trail segments; supports jump and boost.
- `PlayerAI` (src/PlayerAI.java) - Computer player; scans all trails and edges to pick safe turns, occasionally boosts/turns randomly; maintains trail like human.
- `Shape` and `Line` (src/Shape.java) - `Shape` interface for drawable trail segments; `Line` concrete straight segment used for trails and collision checks.
- `Intersection` (src/Intersection.java) - Enum for collision result; code uses `NONE` and `UP` to signal no-hit vs hit.
- `Picture` (src/Picture.java) - Image helper; caches and loads from classpath `/images` (fallback to file) and draws to `Graphics`.
- `Score` (src/Score.java) - High-score storage; reads top 10 from `HighScores.txt`, inserts and sorts, writes back.

## Score.java Smells & Pattern/Principle Issues
- Unclosed I/O resources: `BufferedReader` in constructor and `PrintStream` in `addHighScore` are never closed; no try-with-resources, risking leaks and incomplete writes.
- Swallowed exceptions and misleading API: constructor and `addHighScore` catch `IOException` and ignore it; `addHighScore` declares `throws IOException` but never actually propagates one, creating a confusing contract.
- Representation exposure and aliasing: `getHighScores()` returns the mutable internal list; callers can mutate it. `addHighScore` reassigns `highs` to a new list, breaking external references obtained earlier.
- Magic number `10`: repeated cap for top-N appears in multiple places; should be a single `static final int TOP_N` constant.
- Input parsing bug: `r.trim();` result is discarded; whitespace is not removed before `Integer.parseInt(r)`, risking `NumberFormatException` on lines with spaces; blank lines are not handled.
- Platform/encoding assumptions: writes `\n` explicitly and uses default charset via `PrintStream(new File(file))`; prefer `System.lineSeparator()` and an explicit charset for portability.
- Inefficient collections/algorithm: copies to `LinkedList` then removes by index from the tail (O(n) on `LinkedList`); sorts then reverses instead of sorting with a reverse comparator; an `ArrayList` would be more appropriate here.
- Weak invariants vs UI expectations: does not ensure the list has 10 entries after load; UI (`TronMapSurvival.getHighs`) assumes 10 and can index out of bounds if fewer lines exist.
- Mutability that could be `final`: fields `file` and possibly `highs` are reassigned but conceptually constant; reducing mutability would simplify reasoning.
- Mixed responsibilities: class is both the in-memory model and its persistence mechanism; if following SRP/DAO-Repository patterns strictly, persistence could be separated from the score list model.

## Score.java Smell Fixes (Applied)
- Resource safety: switched to try-with-resources for read/write; use `Files.newBufferedReader/newBufferedWriter` with `UTF-8` to avoid leaks and encoding drift.
- Correct API/exception behavior: `addHighScore` now truly propagates `IOException` instead of swallowing; no hidden failures.
- Defensive copying: `getHighScores()` returns a new `ArrayList` to avoid external mutation/aliasing.
- Single source of truth: introduced `static final int TOP_N = 10` and used consistently.
- Robust parsing: actually trim lines, skip blanks and non-integers; avoids `NumberFormatException`.
- Portability: write with `System.lineSeparator()` and explicit charset.
- Simpler, efficient logic: in-place sort with `reverseOrder()`; avoid `LinkedList` and post-sort reverse.
- Invariants: constructor pads to `TOP_N` entries so UI assumptions (top 10 present) hold even if the file is short/missing.
- Reduced mutability: `file` and `highs` are `final`; list contents updated in place instead of reassigning the reference.
