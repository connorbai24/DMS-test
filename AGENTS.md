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
## Java Version Requirement
- Requires JDK 21 for build/run. Code and tests avoid newer APIs (for example, `Path.of`, `var`) and use `Paths.get`/explicit types for compatibility.

## Coding Style & Naming Conventions
- Java 21 (LTS); tabs for indentation; braces on same line
- Classes `UpperCamelCase`; methods/fields `lowerCamelCase`; constants `UPPER_SNAKE_CASE`
- Keep UI work on the EDT (`SwingUtilities.invokeLater`); avoid long tasks on the EDT
- Load images via `Picture.draw(g, "file.png", x, y)`; place files in `resources/images/`

## Testing Guidelines
- JUnit tests added in `test/ScoreTest.java` covering `Score` parsing, sorting, top-N trimming, persistence, defensive copy, IO error propagation, constructor validation (null repository and null filename), null-loaded list handling, and null-entry conversion to zero
- `Score` tests also cover constructor validation (throws on null repository)
- JUnit tests added in `test/FileScoreRepositoryTest.java` covering `FileScoreRepository` read/write happy paths, invalid input handling (null/empty file path, null scores), IO errors, and exception messages
- JUnit tests added in `test/ScoreRepositoryTest.java` with an in-memory stub that exercises the repository contract (round-trip, null handling, defensive copy)
- If adding tests, use JUnit in `test/` and name `ClassNameTest.java`; focus on movement, collisions, and score updates

## Security & Configuration Tips
- Ensure `HighScores.txt` remains writable; do not use absolute paths; rely on classpath for images
- When packaging or deploying, include `resources/images/` on the classpath
## Classes & Responsibilities

- `Game` (src/Game.java) - Application entry; builds the Swing UI/menus and wires actions; instantiates modes and swaps panels; ctors: default; methods: `run()`, `main(String[] args)`.
- `TronMap` (src/TronMap.java) - Abstract base for game modes; manages players, timer loop, P1 input, and drawing; ctors: `TronMap(JLabel sco1, JLabel sco2, int p)`; methods: `getRandomStart()`, `getVelocity()`, `tick()`, `reset()`, `setScore()`, `addScore()`, `paintComponent(Graphics)`, `getPreferredSize()`.
- `TronMapSurvival` (src/TronMapSurvival.java) - Single-player survival; updates score over time; persists via `Score`; ctors: `TronMapSurvival(JLabel sco1, int p)`; methods: `tick()`, `setScore()`, `reset()`, `addScore()`, `getHighs()`, `paintComponent(Graphics)`.
- `TronMapTwoPlayer` (src/TronMapTwoPlayer.java) - Two-player mode with P2 controls; tracks round wins and banners; ctors: `TronMapTwoPlayer(JLabel sco1, JLabel sco2, int p)`; methods: `tick()`, `restartGame()`, `setScore()`, `reset()`, `addScore()`, `paintComponent(Graphics)`.
- `TronMapStory` (src/TronMapStory.java) - Progressive levels vs AI; delays between levels; ctors: `TronMapStory(JLabel sco1, JLabel sco2, int p)`; methods: `tick()`, `setScore()`, `nextLevel()`, `addPlayers()`, `reset()`, `addScore()`, `paintComponent(Graphics)`.
- `GameObject` (src/GameObject.java) - Abstract moving entity; position, velocity, bounds; collision helpers; ctors: `GameObject(int x, int y, int velocityX, int velocityY, int width, int height)`; methods: `setBounds(int,int)`, `setXVelocity(int)`, `setYVelocity(int)`, `move()`, `clip()`, `intersects(GameObject)`, `accelerate()`, `draw(Graphics)`, `getAlive()`, `getPath()`.
- `Player` (src/Player.java) - Abstract light-cycle; trail (`Line`s), boost/jump, alive state; ctors: `Player(int randX, int randY, int velx, int vely, java.awt.Color color)`; methods: `getBoostsLeft()`, `accelerate()`, `jump()`, `startBoost()`, `boost()`, `draw(Graphics)`, `getAlive()`, `getPath()`, `crash(Intersection)`, `move()`, `addPlayers(Player[])`.
- `PlayerHuman` (src/PlayerHuman.java) - Human-controlled; merges trail segments; ctors: `PlayerHuman(int randX, int randY, int velx, int vely, java.awt.Color color)`; methods: `addPlayers(Player[])`, `move()`.
- `PlayerAI` (src/PlayerAI.java) - Computer player; scans trails/edges, random boosts/turns; ctors: `PlayerAI(int randX, int randY, int velx, int vely, java.awt.Color color)`; methods: `addPlayers(Player[])`, `move()`, private `reactProximity()`.
- `Shape` (src/Shape.java) - Drawable trail segment interface; methods: `draw(java.awt.Graphics)`, `isVertical()`, `getStartX()`, `getStartY()`, `getEndX()`, `getEndY()`.
- `Line` (src/Shape.java) - Concrete straight segment used for trails; ctors: `Line(int x, int y, int x2, int y2)`; methods: `draw(java.awt.Graphics)`, `isVertical()`, `getStartX()`, `getStartY()`, `getEndX()`, `getEndY()`.
- `Intersection` (src/Intersection.java) - Collision result enum; constants: `NONE`, `UP`, `LEFT`, `DOWN`, `RIGHT`.
- `Picture` (src/Picture.java) - Image helper; caches and loads from classpath `/images` (fallback to file); methods: static `draw(java.awt.Graphics g, String filepath, int x, int y)`.
- `Score` (src/Score.java) - High-score domain/service; maintains top-10, sorts/trims, persists via repository; ctors: `Score(String filename)`, `Score(ScoreRepository repository)`; methods: `addHighScore(int) throws java.io.IOException`, `getHighScores()`.
- `ScoreRepository` (src/ScoreRepository.java) - Persistence port for high scores; methods: `read() throws java.io.IOException`, `write(java.util.List<Integer>) throws java.io.IOException`.
- `FileScoreRepository` (src/FileScoreRepository.java) - UTF-8 file-backed repository; ctors: `FileScoreRepository(String file)`; methods: `read() throws java.io.IOException`, `write(java.util.List<Integer>) throws java.io.IOException`.

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
- Reduced mutability: `repository` and `highs` are `final`; list contents updated in place instead of reassigning the reference.
 - Exception policy: `addHighScore` propagates `IOException`; constructor intentionally tolerates read errors and starts with defaults to keep the app usable. This is documented behavior, not a swallowed failure.

## Score.java Design Pattern Fix (Applied)
- Applied Repository/DAO pattern: separated persistence concerns from the `Score` domain logic.
- New interface `ScoreRepository` defines `read()`/`write(List<Integer>)` for storage abstraction.
- `FileScoreRepository` implements file-based storage with UTF-8 and robust parsing; `Score` depends on the interface, not the concrete class.
- `Score` gained a DI-friendly constructor `Score(ScoreRepository repo)`; existing `Score(String filename)` remains for convenience and uses `FileScoreRepository` under the hood.
- External API remains stable: `getHighScores()` and `addHighScore(int)` unchanged for callers (e.g., `TronMapSurvival`).

## Testing Strategy (Score & Repository)
- Score (domain): construct with temp files and with a stub `ScoreRepository`; verify load/pad to 10, robust parsing, descending sort/trim on insert, persistence call, defensive copy, and that IOExceptions from persistence propagate from `addHighScore`.
- FileScoreRepository (persistence): use temp files/dirs; verify trimming and skipping invalid lines on read; verify write produces exact lines in UTF-8 with platform separators; assert constructor rejects null/empty path; assert read errors surface for missing file/dir; assert `write(null)` throws.
- ScoreRepository (interface): validate a minimal in-memory implementation round-trips values, rejects null writes, and returns defensive copies to prevent external mutation.
