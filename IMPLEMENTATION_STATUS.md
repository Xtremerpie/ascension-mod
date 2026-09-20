# ASCENSION — Implementation Status

This document separates what was actually built from what was tested,
per the explicit requirement not to call something "implemented" if it's
a placeholder, or "tested" if it wasn't actually executed.

## Environment this was built in

This code was written in a sandboxed container with **no network access**
to Minecraft/Fabric/Gradle infrastructure, **no Gradle installed**, and
**no Java compiler installed** (only a Java 21 *runtime*, `java`, not
`javac`). Re-verified directly, most recently in this session:

```
$ javac --version
/bin/sh: javac: not found
$ find / -name javac -type f 2>/dev/null
(no results)
$ which gradle
(not found)
$ curl -o /dev/null -w "%{http_code}" https://maven.fabricmc.net/
403
$ curl -o /dev/null -w "%{http_code}" https://libraries.minecraft.net/
403
$ curl -o /dev/null -w "%{http_code}" https://services.gradle.org/
403
$ curl -o /dev/null -w "%{http_code}" https://repo.maven.apache.org/maven2/
403
```

The container's own network configuration lists an explicit allowlist of
reachable hosts (GitHub, npm, PyPI, crates.io, Ubuntu's package mirrors,
Anthropic's own API — nothing Java/Minecraft-related). This is a fixed
property of the sandbox, confirmed by three independent signals (missing
compiler, missing build tool, and a network allowlist that structurally
excludes every host the build needs) — not a transient outage worth
retrying.

Practical consequence: **a real Gradle/Fabric Loom build cannot be run in
this environment, at all, under any circumstances, no matter how the
request is phrased.** `.\gradlew.bat build` was never run.
`build/libs/ascension-1.0.0.jar` does not exist anywhere in this
delivery. See `BUILD_VIA_GITHUB_ACTIONS.md` for the fastest real path to
that jar (a GitHub Actions workflow, included at
`.github/workflows/build.yml`, that runs the unmodified build on a
runner that *does* have the required network access) and the section
below for building it locally instead.

- **The mod was never compiled**, launched, or play-tested. No world was
  created, no HUD was seen on screen, no achievement fired in a running
  game.
- Every version/API claim below (Loom 1.14, Fabric Loader 0.18.1, Fabric
  API 0.141.5+1.21.11, the payload networking API shape, etc.) was
  checked against **web search results**, not against the actual
  Minecraft 1.21.11 jar or its mappings, because that jar was not
  reachable either.

Static checks that WERE performed in this environment:
- A brace-balance scan across all 69 `.java` files (one earlier false
  positive from a literal `)` inside a Javadoc comment — confirmed
  harmless; all files balanced on the final pass).
- A repository-wide grep for `TODO`, `FIXME`, `placeholder`, `fake`,
  `dummy`, `temporary`, `not implemented`, `return null`, `coming soon` —
  every hit was inside a comment *documenting* an honest limitation, not
  an actual stub.
- JSON validity of every achievement/reward/blueprint data file (loaded
  and re-serialized successfully by the Python generation script).

## IMPLEMENTED (real, working logic — not stubs)

- Gradle/Fabric Loom project scaffold targeting 1.21.11 exclusively.
- Physics analysis layer: `MovementPhysics`, `ProjectilePhysics`,
  `TrajectoryCalculator` — all measure/estimate from real Minecraft
  entity state, never simulate their own physics.
- Client-side targeting reusing Minecraft's own crosshair raycast (zero
  extra raycasts added).
- Full modular HUD framework + all 9 HUD modules from the spec, each
  independently toggleable via config, each contextual (not
  permanently on-screen).
- `AscensionConfig`: JSON-persisted, screen-editable settings.
- 31 real, data-driven achievements across all 9 categories (JSON files
  + an index, loaded at runtime — not hardcoded in Java).
- Reward engine: 5 reward types, all data-driven, applied server-side
  only, never duplicated (achievement completion is gated on a
  persisted `completed` flag).
- Progression: XP curve, level-up detection, milestone table (5/10/15/20).
- Player data persistence surviving reload/restart/disconnect (see
  PersistentState note below for the one deliberate API substitution).
- 6 blueprints with real, bounded, size-validated block-placement
  generation — not "structure unlocked" text with no world changes.
- Full `/ascension` command tree with permission-gated admin subcommands.
- Achievement/level-up server→client networking via CustomPayload.
- Strict client/server code separation (`AscensionMod` vs
  `AscensionClient`; no client-only imports in common code).
- A real, functional in-game `ConfigScreen` (checkboxes bound live to
  `AscensionConfig`, saved on close).
- `AscensionScreen` hub (opened with O) linking to Status, Achievements,
  Blueprints, and Configuration.
- `AchievementScreen`: real scrollable list built from the actual
  `AchievementRegistry` data, showing category/title/description/
  progress-percent/completion state per achievement.
- `BlueprintScreen`: real scrollable list built from `BlueprintRegistry`,
  showing name/description/size/unlock state, with a working "Build
  here" button per unlocked blueprint that issues the player's own
  `/ascension build <id>` command (reusing the already-implemented
  server-authoritative build path rather than a second, parallel,
  unverified network action).
- `PlayerStatusPayload`/`PlayerStatusDto`: server→client sync of
  level/XP/achievement-progress/unlocked-blueprints, sent on join and
  after any change (achievement complete, level up, unlock, build,
  admin reward/level commands). This is what feeds the three screens
  above real data instead of nothing.
- `/ascension build <id>` command, actually invoking
  `StructureManager.build` at the player's current position — this was
  a genuine gap found and closed during self-review (a prior pass wrote
  a working `StructureManager` with nothing calling it).
- Projectile fired/hit statistics (`ProjectileTracker`): real,
  bounded per-player-tick polling of the player's own in-flight arrows.
  See the "hit" definition caveat under SIMPLIFIED below — this is a
  real, working implementation with an honestly narrower definition of
  "hit" than "struck a living target."
- Highest-velocity statistic now actually recorded (from the same
  per-tick position delta already computed for distance-travelled),
  where before it was an unused method with nothing calling it.
- World-space trajectory rendering (`TrajectoryRenderer`, wired via
  `WorldRenderEvents.AFTER_TRANSLUCENT`) — draws the estimated path as a
  fading line in the world, not just numbers in a HUD panel. See the
  confidence note under "highest-risk" below — this is the single least
  certain file in the project, because Minecraft's vertex-consumer
  rendering API changes shape most often across versions.
- `gradle/wrapper/gradle-wrapper.properties`, pointing at Gradle 8.12
  (compatible with Fabric Loom 1.14). See NOT IMPLEMENTED below for why
  the wrapper *scripts* and *jar* aren't included.

## SIMPLIFIED (real, but narrower than the original spec asked for)

- **Custom enchantment reward**: modern Minecraft (1.20.5+) registers
  enchantments through a data-driven JSON schema whose exact 1.21.11
  shape could not be verified offline. `EnchantmentReward` instead grants
  a timed vanilla status effect as the closest verifiable real behavior.
  See the class-level comment in `EnchantmentReward.java` for the exact
  migration path once you can confirm the schema locally.
- **Player data storage**: uses per-player JSON files under
  `<world>/ascension/playerdata/` instead of Minecraft's `PersistentState`
  API. The current `PersistentState` registration shape (Codec-based as
  of recent versions) could not be verified against the real 1.21.11
  source offline. Functionally equivalent (survives reload/restart); see
  `PlayerDataManager.java` for the documented rationale and migration
  note.
- **Structure generation**: all 6 blueprints share one parameterized
  hollow-box/tower generator rather than 6 fully bespoke layouts. Every
  one does place real blocks with real safety bounds; the geometry
  variety is limited by time, not faked.
- **Projectile "hit" definition**: `ProjectileTracker` counts a hit when
  a tracked arrow becomes `isInGround()` (stuck in a block) — a real,
  verifiable Minecraft signal. This means a shot that lands in terrain
  counts the same as one that struck a living target and continued;
  distinguishing "hit an entity" specifically would need a
  damage-source hook that couldn't be verified against the real
  1.21.11 API offline. The achievements built against this stat
  (Precision, Long Shot, Projectile Master) use this "landed" definition.
- **BlueprintScreen's Build action**: reuses the existing
  `/ascension build <id>` chat command rather than a dedicated
  client→server network packet — same server-authoritative result,
  one fewer unverified payload type.

## NOT IMPLEMENTED

- **Manual block-placement statistics** (for the Building category):
  no verified stable Fabric API event for "block placed by player"
  could be confirmed in this environment without a Mixin, and a Mixin
  into vanilla placement logic couldn't be written safely without
  access to the real obfuscated/mapped method signatures. This is the
  one gap from the previous pass that remains genuinely open.
  `StatisticsManager.onBlockPlaced` exists and is called by structure
  generation, but not yet by the player manually placing blocks in
  survival. If you can confirm a placement event (or write the Mixin)
  locally, it's a single call-site addition — nothing else changes.
- **gradlew / gradlew.bat launcher scripts and gradle-wrapper.jar** —
  deliberately not included. `gradle-wrapper.properties` (which pins the
  Gradle version these scripts would use) IS included and correct, but
  the scripts and jar are what `gradle wrapper --gradle-version 8.12`
  generates, and hand-writing a look-alike script here risked shipping
  a subtly wrong variant next to an instruction telling you to generate
  the real one anyway — one of those two would end up being the correct
  source of truth, and it should be the one Gradle itself produces. This
  container has Java 21 but no `gradle` binary and no network path to
  `services.gradle.org` to bootstrap one.

## ENVIRONMENT BLOCKED (could not attempt here at all)

- Running `.\gradlew.bat build` (dependency repos unreachable).
- Launching the Fabric development client / `runClient`.
- Creating or loading a test world.
- Any of the runtime verification steps in the original spec (HUD
  visually confirmed, distance calculation confirmed in-game, entity
  targeting confirmed, achievements firing confirmed, rewards granted
  confirmed, persistence confirmed across a real restart, configuration
  screen opened in-game, blueprint/structure system confirmed placing
  blocks in a real world, commands executed in a real server console,
  disabling systems confirmed to actually disable them, performance
  measured under load).

**None of the above were performed, and none are claimed as PASS
anywhere in this project.** `LOCAL_TEST_PLAN.md` gives you the exact
steps to run all of them yourself.

## Highest-risk-of-compile-error areas, if you hit build errors

In rough order of how likely they are to need a tweak against the real
1.21.11 API surface, since none of this was compiled:

1. `client/TrajectoryRenderer.java` — the `VertexConsumer` draw-call
   chain (`.vertex(...).color(...).normal(...)`). Minecraft's immediate-
   mode rendering API changes shape across versions more than anything
   else touched in this project; if the build fails here, check the
   current `VertexConsumer` interface first. Nothing else in that class
   depends on it.
2. `network/NetworkPackets.java` / `network/AscensionNetworking.java` —
   `CustomPayload`/`PacketCodec` exact generic shape.
3. `commands/AscensionCommands.java` —
   `net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback`
   package/version.
4. `ui/ConfigScreen.java` — `CheckboxWidget.Builder` method chain.
5. `core/AscensionEvents.java` — `ServerLivingEntityEvents.AFTER_DEATH`
   exact parameter types.
6. `client/AscensionClient.java` — `WorldRenderEvents.AFTER_TRANSLUCENT`
   registration and the `WorldRenderContext` accessor methods
   (`camera()`, `matrixStack()`, `consumers()`).
7. Everything else primarily uses long-stable APIs (`World`, `Entity`,
   `BlockPos`, `Vec3d`, `ServerPlayerEntity`, Brigadier) that have been
   comparatively stable across recent Minecraft versions.

## On the installable JAR specifically

`build.gradle`'s `base { archivesName = project.archives_base_name }`
(`archives_base_name=ascension` in `gradle.properties`) plus
`version = project.mod_version` (`1.0.0`) means a successful
`.\gradlew.bat build` produces `build/libs/ascension-1.0.0.jar` — that
is the real, remapped, installable mod jar (Loom remaps it to the
Minecraft runtime's mappings automatically as part of the `jar`/`build`
task; there is no separate unmapped jar you'd need to avoid picking by
mistake in this configuration, since only one Loom project source set is
in use). A `-sources.jar` is also produced (from `withSourcesJar()`) —
that one is for IDEs, not for `mods/`.

**This JAR was not built.** No `build/libs/` directory exists in the
delivered source. The claim above is about what the build configuration
is set up to produce, verified by reading `build.gradle`/
`gradle.properties`, not by having run it.
