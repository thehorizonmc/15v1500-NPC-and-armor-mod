# NPC Spawn Block (Fabric 1.20.4)

A block that renders a stationary, randomly-dressed player-model NPC — for
filming large Minecraft crowds — plus a keybind that gives *you* the same
kind of random gear on demand.

This is your existing, working `randomlook` mod with one feature added: the
**up arrow key now re-rolls your own outfit**. Nothing about the NPC block
itself has changed.

## Why this replaces `npc-crowd`

The separate `npc-crowd` mod (the other one I built) spawned a real Entity
into the world with a custom renderer and a custom armor-rendering hook. That
combination hit a `ClassNotFoundException` once, and after fixing that, a
native `EXCEPTION_ACCESS_VIOLATION` crash inside `lwjgl_opengl.dll` — a JVM
crash in the graphics driver layer, not an ordinary Java exception.

This mod avoids all of that by design:

- **No Entity is ever spawned.** The "NPC" is a client-side-only fake player
  (`OtherClientPlayerEntity`) that's never added to the world. It's fed
  straight to vanilla's normal player renderer, so armor, held items and the
  shield all render through Minecraft's existing, battle-tested pipeline —
  no custom `ArmorRenderer` hook at all.
- **No texture loading at startup.** Skins are only loaded the first time an
  NPC is actually rendered (`NpcSkins.ensureLoaded()`), which is well after
  the game window and render system are fully up. That sidesteps the timing
  window where the other mod's eager texture upload likely triggered the
  native crash.
- **No networking or entity-sync needed for the NPCs themselves** — look
  direction and hide/show are plain client-side state, recomputed every
  frame from the block's position.

**Please remove `npc-crowd-1.0.0.jar` from your mods folder.** Both mods
register a block/behavior aimed at the same purpose, and there's no reason to
run the crashing one alongside this working one.

## What it does

Place the **NPC Spawn Block** and a stationary player-model NPC (Alex/slim
model) appears standing on it, facing the way you were facing when you placed
it. It:

- pulls a **random skin** from `.minecraft/random-skins` (flat green if none
  are found)
- gets a **random sword or axe**
- gets a **random shield** (50% of the time)
- gets a **random armor outfit** with a bias toward matching sets
- never moves, never has a nametag, needs no server-side state at all

Skin and gear are derived deterministically from the block's position, so
they're stable across restarts without saving anything extra.

### Commands (client-side, type them in chat)

| Command | Effect |
|---|---|
| `/hidenpcs` | Toggle every NPC's visibility (run it again to bring them back). |
| `/facenpcshere` | Every NPC turns to face exactly where your eyes are right now. |
| `/facenpcshere reset` | Back to facing the way each block was placed. |
| `/reloadnpcskins` | Re-scan `random-skins` without restarting the game. |

These are **client commands** — they only affect what *you* see, don't need
op/cheats, and don't persist across a restart (facing resets to "placed
direction" on relaunch; run `/facenpcshere` again if you need it locked in
for a new recording session).

## Randomizing your own gear (new: up arrow key)

Press the **up arrow key** and your own character gets a fresh random outfit
and weapon, using the **exact same odds** as the NPCs (they share the same
`NpcGear.roll()` code, so they can never drift apart).

- Overwrites both hands and all four armor slots every time — including
  clearing slots the roll says should be empty.
- Real vanilla items, so nothing renders in a special/custom way.
- Tap repeatedly for repeatedly new rolls; holding the key does nothing extra.
- Rebindable in **Options → Controls → Key Binds**, under **NPC Spawn Block**.

Unlike the client-side NPC commands, this one talks to the server (a small
empty network packet), because equipping a player's actual inventory has to
happen server-side to stick and to be visible to anyone else watching you.

## The random gear odds

**Tool** — 4 materials × {sword, axe}, iron weighted double:

| Material | Chance |
|---|---|
| Iron | 40% (20% sword, 20% axe) |
| Diamond / Wood / Gold | 20% each (10% sword, 10% axe) |

**Armor material** — same weighting: iron 40%, diamond/wood/gold 20% each.
"Wood" maps to leather by default (vanilla has no wooden armor) — configurable
in `.minecraft/config/npcspawner.json`.

**Armor slots**, rolled fresh every time:
1. Each slot independently rolls empty/filled. Chance **empty**: boots 15%,
   leggings 25%, chestplate 20%, helmet 30%.
2. If 2+ pieces landed, 50% chance to upgrade to a complete 4-piece set.
3. If the set ends up complete, 70% chance all four pieces share one material.

**Shield** — flat 50%.

All of these constants are at the top of
`src/main/java/com/npcspawner/client/NpcGear.java`.

## Setting up skins

1. Find `.minecraft/random-skins` (created automatically on first launch if
   it doesn't exist).
2. Drop in `.png` skin files — either modern 64×64 or legacy 64×32 (both are
   accepted; legacy skins get the same upscale vanilla applies).
3. Run `/reloadnpcskins`, or just restart, to pick them up.

## Building it online (no local install needed)

1. Make a free GitHub account, create a new repository.
2. Upload every file in this project (open the folder, select everything
   inside with Ctrl+A, drag that selection into the upload box) and commit.
3. Go to the **Actions** tab → the **build** workflow runs automatically →
   wait ~3–5 minutes for a green check.
4. Open the finished run → **Artifacts** → download **npc-spawn-block-mod** →
   unzip → you'll find `randomlook-1.0.0.jar`.
5. Put that jar in `.minecraft/mods` alongside **Fabric Loader** and
   **Fabric API** for 1.20.4.

## Project layout

```
├── .github/workflows/build.yml
├── build.gradle, gradle.properties, settings.gradle
└── src/main/
    ├── java/com/npcspawner/
    │   ├── NpcSpawnerMod.java          (server/common entrypoint)
    │   ├── NpcSpawnBlock.java
    │   ├── NpcSpawnBlockEntity.java    (holds no data - purely a render anchor)
    │   ├── PlayerGearNetworking.java   (NEW - serves the up-arrow keybind)
    │   └── client/
    │       ├── NpcSpawnerClient.java   (client entrypoint)
    │       ├── NpcRenderer.java        (the BlockEntityRenderer that draws the NPC)
    │       ├── NpcPlayerEntity.java    (the fake, never-added-to-world player)
    │       ├── NpcGear.java            (ALL the gear odds live here)
    │       ├── NpcSkins.java           (lazy skin loading from random-skins)
    │       ├── NpcConfig.java          (npcspawner.json - the "wood" armor mapping)
    │       ├── NpcClientState.java     (hide/show + look-target, client only)
    │       └── PlayerGearKeybind.java  (NEW - the up-arrow key)
    └── resources/...
```
