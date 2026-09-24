# NPC Crowd (Fabric 1.20.4)

A spawner block that places stationary player-model NPCs with randomized
skins, armor, tools and shields — built for filming large Minecraft crowds
without needing real players.

---

## What it does

Place the **NPC Spawner** block and one NPC appears standing on top of it.
The NPC:

- uses the vanilla **slim (Alex) player model**
- pulls a **random skin** from a `random-skins` folder in your `.minecraft`
  directory (flat green if none are found)
- gets a **random sword or axe** in its main hand
- gets a **random shield** (50% of the time) in its off hand
- gets a **random armor outfit** with a bias toward matching complete sets
- **never moves** — no AI, no gravity, no drifting, no pushing
- **never shows a nametag**

Breaking the spawner block removes its NPC.

### Commands

| Command | What it does |
|---|---|
| `/facenpcshere` | Every loaded NPC turns to face your head's exact position. Head **and body** rotate together. |
| `/hidenpcs` | Makes all spawner blocks invisible (the NPCs stay). |
| `/shownpcs` | Makes them visible again. |
| `/clearnpcfacing` | Undo `/facenpcshere` — back to each NPC's spawn direction. |

All require permission level 2 (creative/op, or just singleplayer with cheats on).

**On the "locked in" facing:** `/facenpcshere` stores only a *fixed world
point* on each NPC. The actual head and body angle is recomputed from that
point every frame on the render side. So the angle isn't baked in at command
time (the NPCs genuinely aim at the spot), but because the stored point never
moves, the heads stay locked exactly where they were when you ran the command
no matter where you walk afterwards.

---

## The random gear odds

**Tool (main hand)** — 8 options, iron weighted double:

| Item | Chance |
|---|---|
| Iron sword | 20% |
| Iron axe | 20% |
| Diamond / wooden / golden sword | 10% each |
| Diamond / wooden / golden axe | 10% each |

**Armor material** — vanilla has no wooden armor, so leather is the low tier:
iron 40%, diamond 20%, gold 20%, leather 20%.

**Armor slots** — three steps, rolled fresh per NPC:

1. Each slot rolls present/absent independently. Chance it's **empty**:
   helmet 30%, chestplate 20%, leggings 25%, boots 15%.
2. If 2+ pieces came up present, **50% chance** to upgrade to a complete
   4-piece set.
3. If the final set is complete (either way), **70% chance** all four pieces
   share one material; otherwise each piece rolls independently.

**Shield (off hand)** — flat 50%.

Every one of these numbers is a named constant at the top of
`src/main/java/com/example/npccrowd/util/RandomGear.java`, so you can tune
them without touching any logic.

---

## Randomizing your own gear (up arrow key)

Press the **up arrow key** and your own character is instantly given a fresh
random outfit and weapon, using the exact same odds as the NPCs.

- **Overwrites both hands every time** — whatever you were holding is replaced
  by the rolled tool, and the off hand becomes a shield or is emptied.
- **Overwrites all four armor slots** — including clearing slots the roll
  decided should be empty.
- Real vanilla items, so they render exactly like normal gear with no custom
  rendering involved.
- Tap it as many times as you like; each press is one new roll. Holding the key
  does nothing extra.

Rebind it in **Options → Controls → Key Binds**, under the **NPC Crowd**
category, if the arrow key clashes with something.

Because the server owns inventory state, the keybind doesn't equip you
directly — it sends a small message and the server does the rolling, which is
why the result sticks and is visible to anyone else watching you.

## Setting up skins

1. Find your `.minecraft` folder (Windows: `%appdata%\.minecraft`).
2. Make a folder called **`random-skins`** inside it.
3. Drop in as many `.png` skin files as you like.

Requirements and notes:

- Skins must be **64×64** (the modern format). Old 64×32 skins are skipped
  with a warning in the log — convert them first.
- Skins are matched to the **slim/Alex** model, so slim skins look best. Wide
  (Steve) skins still work but arms will look slightly thin — from crowd
  distance this is invisible.
- Press **F3+T** in game to reload skins after adding files; no restart needed.
- Skin loading is **client-side**. Each machine uses its own folder, so only
  the PC you're recording on needs skins. If nothing is found, every NPC is
  flat green — that's your signal the folder isn't being read.

---

## How to compile it (no experience needed)

You don't need Java, Gradle, or any setup on your computer. GitHub will build
it for you for free. This project already includes the build script at
`.github/workflows/build.yml`.

1. **Make a GitHub account** at [github.com](https://github.com) if you don't
   have one.
2. Click **+** (top right) → **New repository**. Give it any name, leave it
   Public or Private, and click **Create repository**.
3. On the empty repo page, click **uploading an existing file**.
4. Unzip the project on your computer, then **drag the contents of the
   `npc-crowd` folder** (the `src` folder, `build.gradle`, `gradle.properties`,
   `settings.gradle`, and the `.github` folder) into the upload box.
   - Important: upload the files *inside* `npc-crowd`, not the folder itself.
   - If the `.github` folder doesn't upload by drag-and-drop (browsers
     sometimes skip dot-folders), see the note below.
5. Click **Commit changes**.
6. Go to the **Actions** tab at the top of your repo. You'll see a run called
   "Build mod" start automatically. Wait ~3–5 minutes for a green checkmark.
7. Click the finished run, scroll to the bottom to **Artifacts**, and download
   **`npc-crowd-jar`**. Unzip it.
8. Inside you'll find a few jars. The one you want is **`npc-crowd-1.0.0.jar`**
   (ignore anything ending in `-sources` or `-dev`).

**If the `.github` folder won't upload:** in your repo click **Add file** →
**Create new file**, type `.github/workflows/build.yml` as the filename (GitHub
turns the slashes into folders automatically), paste in the contents of that
file from the zip, and commit.

**If the build fails:** open the Actions run and read the red step. The most
common cause is a version that's been pulled from the Fabric maven — in that
case update the version numbers in `gradle.properties` from
[fabricmc.net/develop](https://fabricmc.net/develop).

### Updating a repo you already made

If you already uploaded the previous version of this mod, you don't need a new
repository — just replace the files:

1. Open your repo and click **Add file** → **Upload files**.
2. Drag in the contents of the new `npc-crowd` folder, same as before.
   GitHub overwrites files with matching paths and adds the new ones.
3. Click **Commit changes**.
4. The **Actions** tab automatically starts a fresh build. Download the new
   artifact when it goes green.

Every commit keeps its own build, so if a new version breaks you can always
grab the jar from an older green run in the Actions history.

> **One thing to watch:** uploading files this way never *deletes* anything.
> That's fine here since this update only adds and edits files. But if you
> ever need to remove a file, open it in GitHub and use the **⋯** menu →
> **Delete file**.


### Installing the mod

1. Install **Fabric Loader** for 1.20.4 from
   [fabricmc.net/use](https://fabricmc.net/use).
2. Download **Fabric API** for 1.20.4 from
   [modrinth.com/mod/fabric-api](https://modrinth.com/mod/fabric-api).
3. Put **both** the Fabric API jar and your `npc-crowd` jar into
   `.minecraft/mods`.
4. Launch the Fabric 1.20.4 profile.
5. The NPC Spawner block is in the **Functional Blocks** creative tab, or use
   `/give @s npccrowd:npc_spawner`.

---

## How Minecraft mods are structured (the short version)

Useful context for reading or tweaking this project.

**Minecraft is split into a server and a client, even in singleplayer.** The
server owns the truth: what entities exist, where they are, what they're
wearing. The client owns the pictures: models, textures, camera. They talk
over a network connection (a fake in-memory one in singleplayer). This split
explains most of the structure below:

- Code in `src/main/java` runs on **both** sides.
- Code in `src/client/java` runs on the **client only** — rendering, textures.
  Putting rendering code here means a dedicated server never even tries to
  load it (server machines have no graphics classes, so this would crash).

**Anything the client needs to draw must be explicitly synced.** That's what
the *data tracker* in `NpcEntity` is for: the skin seed and the look-target
point are registered as tracked values, and Fabric/Minecraft automatically
sends updates to every client watching that entity. A plain Java field would
only exist on the server and the client would never see it.

**Registration.** Blocks, items and entity types don't exist until you put
them in a *registry* under a unique `Identifier` like `npccrowd:npc_spawner`
(namespace : name). That's what `ModBlocks` and `ModEntities` do, and it has
to happen during mod initialization, which is why `fabric.mod.json` lists
`entrypoints` — the classes Fabric calls on startup.

**Code vs. resources.** Java code defines *behaviour*. JSON files in
`src/main/resources/assets` define *appearance*, and the game finds them by
naming convention, not by any code reference:

| File | Purpose |
|---|---|
| `blockstates/npc_spawner.json` | Maps each blockstate to a model. This is where `hidden=true` gets pointed at an empty model. |
| `models/block/npc_spawner.json` | The actual cube shape + which texture to use. |
| `models/item/npc_spawner.json` | How it looks in your hand/inventory. |
| `textures/block/npc_spawner.png` | The image. |
| `lang/en_us.json` | Display names. Without this you'd see `block.npccrowd.npc_spawner` in the UI. |

**Why `/hidenpcs` uses a blockstate.** A *blockstate* is a variant of a block
(like which way a stair faces). Making "hidden" a blockstate rather than a
client-side toggle means it saves with the world and syncs to every connected
client automatically — including a second account you're recording from.
The block stays solid and selectable while hidden, so you can still break it.

**Mappings.** Minecraft's real code is obfuscated (classes named `a`, `b`...).
Yarn mappings give readable names, which is why `gradle.properties` pins a
`yarn_mappings` version. Mapping names change between Minecraft versions,
which is why mods have to be ported and why this one targets 1.20.4
specifically.

---

## Honest caveats

I wrote this against the verified 1.20.4 API but **could not compile or run
it** — the environment I built it in has no internet access to fetch
Minecraft/Gradle dependencies. Custom entity rendering has a lot of surface
area, so expect the possibility of a compile error or two on the first build.
If that happens, paste me the error from the Actions log and I'll fix it.

Specific things to watch:

- **Commands only affect loaded chunks.** NPCs in unloaded chunks aren't
  touched by `/facenpcshere` or `/hidenpcs`. For a big set, stand somewhere
  central with a high render/simulation distance.
- **Facing is set per-world**, using the world you run the command in.
- Skins render on the slim model regardless of whether the source skin was
  designed for wide arms — intentional, per your spec.
- The NPCs are invulnerable to normal damage but **can** be killed by a
  creative-mode player, which is a handy escape hatch if one gets stuck.
- The cosmetic armor items and the three right-click charms have been
  **removed** in this version, along with the custom armor renderer that was
  the riskiest code in the mod. Everything now uses real vanilla items.
- The up-arrow keybind relies on Fabric's networking API. If pressing it does
  nothing, check the game log for a warning about an unregistered channel —
  that would point at the client and server halves disagreeing.

## Project layout

```
npc-crowd/
├── .github/workflows/build.yml      ← the GitHub auto-builder
├── build.gradle, gradle.properties, settings.gradle
└── src/main/
    ├── java/com/example/npccrowd/
    │   ├── NpcCrowd.java                ← startup entrypoint (both sides)
    │   ├── block/
    │   │   ├── ModBlocks.java
    │   │   └── NpcSpawnerBlock.java
    │   ├── entity/
    │   │   ├── ModEntities.java
    │   │   └── NpcEntity.java
    │   ├── command/NpcCommands.java
    │   ├── network/ModNetworking.java    ← keybind request handler (server)
    │   ├── client/                      ← client-only code (see note below)
    │   │   ├── NpcCrowdClient.java      ← client startup
    │   │   ├── NpcCrowdKeybinds.java    ← the up-arrow keybind
    │   │   ├── NpcEntityRenderer.java   ← model + render-side facing
    │   │   └── NpcSkinManager.java      ← reads the random-skins folder
    │   └── util/
    │       ├── RandomGear.java          ← ALL the probabilities live here
    │       └── WeightedPool.java
    └── resources/
        ├── fabric.mod.json
        └── assets/npccrowd/...
```

Everything sits in one source folder on purpose. An earlier version used Loom's
`splitEnvironmentSourceSets()` with a separate `src/client/java`; that folder
compiled fine but its output never got packaged into the jar, so the client
entrypoint crashed with `ClassNotFoundException` at launch. The client classes
are still safe on a dedicated server, because the `client` entrypoint is never
invoked there and nothing server-side references the `client` package.
