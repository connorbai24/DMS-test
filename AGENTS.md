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
