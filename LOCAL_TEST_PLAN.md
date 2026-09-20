# ASCENSION — Local Test Plan

Run these on your Windows machine, in order. Each has an ACTION, EXPECTED
RESULT, PASS condition, and FAIL condition. Stop and fix before moving on
if something fails — don't skip ahead on a red step.

## 0. Prerequisites

- ACTION: Install a JDK 21 (Temurin/Adoptium recommended) and confirm
  with `java -version`.
- EXPECTED: reports a 21.x version.
- PASS: version starts with `21.`
- FAIL: install/fix your JDK before continuing.

- ACTION: Confirm you have internet access to `maven.fabricmc.net` and
  `libraries.minecraft.net` (e.g. open them in a browser).
- EXPECTED: pages load (may be a raw directory listing — that's fine).
- FAIL: if blocked by a firewall/VPN, fix that first — the build cannot
  proceed without them.

## 1. Generate the Gradle wrapper (scripts/jar not included in this delivery)

`gradle/wrapper/gradle-wrapper.properties` is already included, pinned to
Gradle 8.12 (compatible with the Fabric Loom 1.14 plugin this project
uses). The launcher scripts and jar aren't included — generating them
regenerates that properties file too, so:

- ACTION: In the project root, with any Gradle 8.x installed:
  `gradle wrapper --gradle-version 8.12`
- EXPECTED: creates/updates `gradlew`, `gradlew.bat`,
  `gradle/wrapper/gradle-wrapper.jar`, and re-confirms
  `gradle/wrapper/gradle-wrapper.properties`.
- PASS: those files now exist and `gradlew.bat --version` reports Gradle 8.12.
- FAIL: install Gradle 8.x first (or use an IDE — IntelliJ IDEA can
  generate a wrapper for an existing project via its Gradle import).

## 2. Confirm current Fabric versions

- ACTION: Open https://fabricmc.net/develop and check the current Fabric
  Loader / Fabric API / Loom versions listed for Minecraft 1.21.11.
- EXPECTED: compare against `gradle.properties` (`loader_version`,
  `fabric_version`) and `build.gradle`'s `fabric-loom` plugin version.
- PASS: they match, or you've updated the properties/build files to the
  current numbers.
- FAIL: update the mismatched version string(s) before building.

## 3. Build

- ACTION: `.\gradlew.bat build`
- EXPECTED: Gradle downloads Minecraft 1.21.11 + mappings + Fabric Loader
  + Fabric API, compiles all 64 Java files, and produces a jar.
- PASS: build ends with `BUILD SUCCESSFUL` and
  `build/libs/ascension-1.0.0.jar` exists.
- FAIL: read the first compile error carefully. Check
  `IMPLEMENTATION_STATUS.md`'s "highest-risk-of-compile-error areas"
  section first — the networking, command-registration, and
  CheckboxWidget code are the most likely culprits, in that order.

## 4. Launch the development client

- ACTION: `.\gradlew.bat runClient`
- EXPECTED: a Minecraft window opens to the main menu.
- PASS: main menu appears, no crash log.
- FAIL: read `.gradle`/`run/logs/latest.log` for the stack trace.

## 5. Create a test world

- ACTION: Create a new Creative-mode world.
- EXPECTED: world loads normally.
- PASS: you can walk around.
- FAIL: check `run/logs/latest.log` for a mod-init crash; confirm
  `fabric.mod.json` entrypoints match the actual package/class names.

## 6. Mod loaded

- ACTION: check the log (or `/ascension status` in chat).
- EXPECTED: a line like `[Ascension] Loaded 31 achievements, 14 rewards, 6 blueprints.`
- PASS: that line appears with those counts (or close to them).
- FAIL: an achievement/reward/blueprint JSON file may be malformed —
  check the log for `[Ascension] Malformed ... file skipped`.

## 7. HUD visible

- ACTION: look around normally (nothing special held).
- EXPECTED: minimal/no HUD panels, since nothing contextual is happening.
- PASS: screen is clean.
- FAIL: if random panels are always showing, a `shouldDisplay()` check
  is wrong.

## 8. Distance module

- ACTION: join with a second account/instance (LAN world), or spawn
  another player via a test setup.
- EXPECTED: a "Nearest Player" panel appears top-right showing distance,
  horizontal, vertical, and direction.
- PASS: numbers look plausible and update as you move.
- FAIL: check `DistanceHudModule` scan range vs actual distance.

## 9. Target module

- ACTION: look directly at a mob or player.
- EXPECTED: a panel appears with name/distance/height diff/direction
  (and health, for living entities).
- PASS: panel appears and disappears correctly as you look away.
- FAIL: check `RaycastService`/`EntityTargeting`.

## 10. Movement physics

- ACTION: walk, then sprint.
- EXPECTED: a Movement panel appears once you're not idle, showing
  speed/state changing from WALKING to SPRINTING.
- PASS: state label matches what you're doing.
- FAIL: check `MovementPhysics.classify`.

## 11. Bow/projectile physics

- ACTION: fire an arrow from a bow.
- EXPECTED: a Projectile panel appears while your arrow is in flight,
  showing speed/distance/flight time, landing marked ESTIMATE.
- PASS: panel appears and disappears with the arrow's flight.
- FAIL: check `ProjectileHudModule`'s entity search / `getOwner()` check.

## 11b. Projectile fired/hit statistics

- ACTION: fire several arrows, letting at least one stick in a block.
  Then run `/ascension stats`.
- EXPECTED: `projectiles_fired` increments once per arrow shot;
  `projectiles_hit` and `longest_projectile_hit_blocks` increment once
  each arrow lands stuck in a block (note: "hit" here means "landed",
  not specifically "struck a mob" — see IMPLEMENTATION_STATUS.md).
- PASS: numbers match what you actually did.
- FAIL: check `ProjectileTracker.tick`'s owner-filter and `isInGround()`.

## 12. Trajectory visualization

- ACTION: enable trajectory visualization in config (on by default),
  fire an arrow.
- EXPECTED: a fading cyan line traces the arrow's estimated path in the
  world, drawn via `TrajectoryRenderer` (see IMPLEMENTATION_STATUS.md —
  this is the single least-verified file in the project; if it doesn't
  compile, that's expected to be the first place to look).
- PASS: line appears, roughly follows the arrow, fades toward the end.
- FAIL: check `VertexConsumer`'s exact method chain against your local
  Minecraft 1.21.11 client classes first.

## 13. Achievements

- ACTION: run `/ascension reward reward_xp_small` (admin), or genuinely
  travel 1,000 blocks for `explorer`.
- EXPECTED: a chat/log entry and an "Ascension Complete" HUD popup.
- PASS: popup appears top-center, fades after ~5s.
- FAIL: check `NetworkPackets`/payload registration first (see risk list).

## 14. Diamond/item rewards

- ACTION: complete `precision_hunter` or run `/ascension reward reward_item_diamonds_small`.
- EXPECTED: diamonds appear in your inventory (or drop at your feet if full).
- PASS: correct item + count received exactly once.
- FAIL: check `ItemReward`/`RewardManager`.

## 15. XP

- ACTION: complete any achievement with an `experience`-type reward, or
  check `reward_xp_small.json`'s type — note these are currently
  `progression` (Ascension XP), not vanilla XP orbs.
- EXPECTED: Ascension XP increases (check via `/ascension status`).
- PASS: number goes up by the expected amount.

## 16. Ascension levels

- ACTION: run `/ascension level 5`.
- EXPECTED: a "Level Up" popup mentioning "Advanced HUD" unlocked.
- PASS: level reflected in `/ascension status`.
- FAIL: check `AscensionLevel`/`AscensionManager`.

## 17. Blueprint unlock

- ACTION: run `/ascension unlock watchtower`.
- EXPECTED: `/ascension blueprints` now lists `watchtower`.
- PASS: confirmed.

## 18. Structure building

- ACTION: `/ascension unlock watchtower`, then `/ascension build watchtower`
  at a clear, flat location.
- EXPECTED: real blocks appear forming the tower shape.
- PASS: blocks placed, `structures_built` stat increments.
- FAIL: check `StructureGenerator`'s per-id branch.

## 19. Persistence

- ACTION: complete an achievement, note your stats, fully quit the
  client/server, relaunch, rejoin.
- EXPECTED: `/ascension status` shows the same level/XP/achievements.
- PASS: confirmed.
- FAIL: check the `<world save>/ascension/playerdata/<uuid>.json` file
  exists and is valid JSON.

## 20. Ascension menu, Achievement screen, Blueprint screen

- ACTION: press O.
- EXPECTED: the Ascension hub opens with Status/Achievements/Blueprints/
  Configuration/Close buttons.
- PASS: hub opens; clicking Status shows your level/XP/achievement count
  (should match `/ascension status`); clicking Achievements shows a
  scrollable list of all 31 achievements grouped roughly by category,
  with completed ones marked and incomplete ones showing a progress
  percentage; clicking Blueprints shows all 6 blueprints with locked/
  unlocked state, and an unlocked one's "Build here" button actually
  runs `/ascension build <id>` (watch chat for the confirmation).
- FAIL: if the lists are empty, check that `PlayerStatusPayload` synced
  (look for a received-packet log or add a temporary print in the
  client receiver); if nothing loads at all, check
  `CheckboxWidget`/`ButtonWidget.builder` API first (see risk list) —
  that's shared with ConfigScreen.

## 20b. Configuration screen

- ACTION: from the Ascension hub, click Configuration.
- EXPECTED: checkboxes for each HUD module, reflecting current config.
- PASS: toggling a checkbox and closing changes `config/ascension.json`
  and the corresponding HUD module's visibility.
- FAIL: check `ConfigScreen`/`CheckboxWidget` API (see risk list).

## 21. Commands

- ACTION: run each `/ascension ...` subcommand as both an op and a
  non-op player.
- EXPECTED: admin subcommands (unlock/reward/level/reload) refuse
  non-ops; everything else works for anyone.
- PASS: permission checks hold.

## 22. Multiplayer/server behavior

- ACTION: run a dedicated server (`.\gradlew.bat runServer` if
  configured, or a standalone Fabric server jar) with this mod, no
  client-only classes loaded.
- EXPECTED: server starts without a `ClassNotFoundException`/`NoClassDefFoundError`
  referencing anything in the `client`/`ui`/`hud` packages.
- PASS: clean startup.
- FAIL: something in `AscensionMod`'s common init is reaching into
  client-only code — audit imports in `core`/`achievements`/`rewards`/etc.

## 23. Performance

- ACTION: spawn a large number of entities (e.g. `/summon` in a loop or
  a mob farm) near a player with the HUD active, watch F3's TPS/FPS.
- EXPECTED: no severe TPS drop attributable to ASCENSION specifically
  (compare F3 numbers with the mod's HUD toggled off vs on).
- PASS: no significant, reproducible regression.
- FAIL: lower `entityScanRangeBlocks`/increase update intervals in
  `config/ascension.json` and re-test; if still bad, profile
  `WorldHudModule`/`DistanceHudModule`'s entity queries first — they're
  the most search-heavy modules.
