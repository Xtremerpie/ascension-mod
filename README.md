# ASCENSION

An intelligence and progression layer for Minecraft — contextual HUD,
physics analysis, achievements, progression, and structures — without
changing vanilla survival.

## SOURCE PROJECT vs. INSTALLABLE MOD — read this first

This repository is the **source project**. It is for developers, and it
is what you build from. It is not something a Minecraft player installs.

- **`ascension-mod-source.zip`** (what this delivery gives you) — the
  source project: Java source, resource/data files, Gradle build
  config. A developer builds this. **Do not put this zip, or this
  folder, into `.minecraft/mods/`** — Minecraft cannot load a source
  tree or a zip of one.
- **`ascension-1.0.0.jar`** — the actual installable mod, produced by
  running the build (see below) inside this source project. This is the
  one file a Minecraft player copies into `.minecraft/mods/`. **This
  jar was not generated as part of this delivery** — see
  `IMPLEMENTATION_STATUS.md` for exactly why (no network access to
  Minecraft/Fabric's Maven repos in the environment this was built in)
  and exactly what command produces it once you run this locally.

⚠️ Before anything else, also read `IMPLEMENTATION_STATUS.md` in full.
This project was built in an environment with no network access to
Minecraft/Fabric infrastructure, so it has never been compiled,
launched, or play-tested. Every claim in this README about what the mod
"does" describes the code as written, not a verified runtime result.

## Minecraft version

**Minecraft Java Edition 1.21.11 only.** Not tested on, and not intended
for, any other version.

## Requirements

- Minecraft Java Edition 1.21.11
- Fabric Loader **0.18.1** or newer
- Fabric API **0.141.5+1.21.11** or newer
- Java 21

(Re-check these three version numbers against
[fabricmc.net/develop](https://fabricmc.net/develop) before building —
they were current at the time this was written but Fabric ships updates
frequently.)

## Installation (once you have a built jar)

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft
   1.21.11.
2. Download the matching version of **Fabric API** and place it in your
   `.minecraft/mods` folder.
3. Build this project (below) — this produces
   `build/libs/ascension-1.0.0.jar`.
4. Copy **only that jar** into the same `mods` folder (not the source
   zip/folder, not `ascension-1.0.0-sources.jar`, which is for IDEs).
5. Launch Minecraft with the Fabric profile.

### Building the jar

```
cd ascension
gradle wrapper --gradle-version 8.12    # one-time: generates gradlew/gradlew.bat/wrapper jar
.\gradlew.bat build                     # Windows
# or: ./gradlew build                   # macOS/Linux
```

Output: `build/libs/ascension-1.0.0.jar` — this is the real, Loom-remapped
mod jar, ready for `mods/`. (`build/libs/ascension-1.0.0-sources.jar` is
also produced, for IDE use — don't install that one.)

## Controls

| Key | Action |
|---|---|
| **G** | Toggle ASCENSION/God UI (expanded world intelligence panel) |
| **H** | Toggle the HUD entirely |
| **O** | Open the Ascension menu (Status / Achievements / Blueprints / Configuration) |

All three are rebindable through Minecraft's normal Controls menu.

## HUD

Two modes:

- **Normal mode** — contextual only. Modules appear only when relevant
  (looking at a player/entity, holding a bow, falling, another player
  nearby) and disappear otherwise. The HUD never permanently covers the
  screen.
- **ASCENSION/God UI mode** (press G) — an expanded world intelligence
  panel: position, dimension, biome, light level, and nearby
  player/hostile/passive entity counts.

Nine independently-toggleable modules: Target, Distance, Movement,
Physics (fall analysis), Projectile, World, Achievement notifications,
Progression notifications, general Notifications.

## Physics

Movement and projectile values are measured from Minecraft's own entity
state (position deltas, real velocity vectors) — ASCENSION does not run
a parallel physics simulation. Values Minecraft doesn't expose directly
(a projectile's predicted landing spot) are computed via standard
projectile motion and explicitly labelled **ESTIMATE** in the HUD. When
enabled, the same estimate is drawn as a fading line in the world itself
while your arrow is in flight.

## Achievements

31 achievements across all 9 required categories (Exploration, Combat,
Physics, Survival, Engineering, Building, Discovery, Mastery, Ascension),
defined as data files under
`src/main/resources/data/ascension/achievements/` — not hardcoded in
Java. Add a new achievement by dropping a new JSON file in that folder
and adding its filename to `index.json`.

## Progression

Ascension XP and Levels 1–20, with milestone unlocks at 5 (advanced HUD),
10 (blueprints), 15 (advanced physics visualization), and 20 (Ascendant).
Entirely optional — nothing here gates normal survival gameplay.

## Rewards

Five reward types (item, vanilla XP, Ascension XP, blueprint unlock,
enchantment-substitute effect), all data-driven under
`data/ascension/rewards/`, all applied server-side only.

## Blueprints & Structures

Six unlockable blueprints (Watchtower, Archer Tower, Explorer Camp,
Bridge, Training Arena, Small Laboratory), browsable in the in-game
Blueprint screen (Ascension menu → Blueprints), which shows lock state,
description and size, and includes a working "Build here" button for
anything you've unlocked. Building one places real blocks in the world,
server-side, with a maximum structure size check and a build-limit/bounds
check before anything is placed. See `IMPLEMENTATION_STATUS.md` for the
honest note on how much visual variety exists between the six.

## Configuration

`/ascension` in-game or the config screen (press O). Settings persist to
`config/ascension.json`.

## Commands

```
/ascension                  — status summary
/ascension status
/ascension achievements
/ascension blueprints
/ascension build <id>       — build an unlocked blueprint at your current position
/ascension stats
/ascension debug            — toggle debug mode
/ascension reload           — admin, reload achievement/reward JSON from disk
/ascension unlock <id>      — admin, unlock a blueprint for yourself
/ascension reward <id>      — admin, grant a specific reward
/ascension level <n>        — admin, set your Ascension level
```

Admin subcommands require permission level 2 (same as vanilla
`/gamemode`) — not available to ordinary players on a server.

## Multiplayer

Rewards, achievement completion, progression, and structure generation
are all server-authoritative. HUD rendering and trajectory visualization
are client-only. The client cannot grant itself items, XP, or structures.

## Performance

Update intervals (HUD refresh, world scan, trajectory recompute) and
entity scan range are all configurable, with conservative defaults.
Entity/world queries are always bounded to a radius around the player —
never a whole-world scan.

## Known limitations

See `IMPLEMENTATION_STATUS.md` for the full, categorized breakdown. In
short: custom enchantments and player-data persistence both use a
documented, verifiable substitute instead of an API whose exact 1.21.11
shape couldn't be confirmed offline; the six blueprints share one
parameterized structure generator rather than fully bespoke layouts; and
manual block-placement statistics (as opposed to structure-generation
placement, which IS tracked) aren't wired up, since no verifiable Fabric
API event for it could be confirmed without a Mixin in this environment.

## Build instructions

See `LOCAL_TEST_PLAN.md`.

## Testing status

**Not compiled or run.** See `IMPLEMENTATION_STATUS.md`.
