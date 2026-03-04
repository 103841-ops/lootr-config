# LootrConf

An in-game loot table editor GUI for Minecraft Forge 1.20.1, with optional [Lootr](https://www.curseforge.com/minecraft/mc-mods/lootr) integration.

---

## Features

- **In-game GUI editor** — create and edit loot tables without leaving the game
- **Pool management** — add/remove loot pools with configurable roll ranges
- **Entry management** — add/remove items with weight, quality, and count settings
- **Rarity presets** — one-click Common / Uncommon / Rare / Epic / Legendary weight buttons
- **Loot functions** — enchantments, naming, NBT, damage, set_potion, exploration_map, and more
- **Loot conditions** — random_chance, random_chance_with_looting, inverted, and more
- **Item picker** — searchable overlay showing all registered items
- **Datapack output** — tables saved directly into a world-local `LootrConf` datapack and hot-reloaded
- **Apply to containers** — assign a table to any chest/barrel/shulker you're looking at
- **Chest randomizer** — optionally randomize all containers on chunk load with weighted table selection
- **Lootr detection** — automatically detects the Lootr mod and adjusts GUI indicators

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.2.0+ |
| Java | 17 |
| Lootr *(optional)* | 0.7+ |

---

## Installation

1. Download the compiled `.jar` from the [Releases](../../releases) page.
2. Place it in your Forge `mods/` folder.
3. (Optional) Install the [Lootr](https://www.curseforge.com/minecraft/mc-mods/lootr) mod for per-player loot instancing.
4. Launch the game.

---

## Building from Source

### Prerequisites

- JDK 17
- Git

### Steps

```bash
git clone https://github.com/your-org/lootr-config.git
cd lootr-config
./gradlew build
```

The compiled jar will be in `build/libs/`.

To launch a development client:

```bash
./gradlew runClient
```

To launch a development server:

```bash
./gradlew runServer
```

---

## Usage

### Opening the Editor

All commands require **operator level 2** (or above).

| Command | Description |
|---|---|
| `/lootrconf editor` | Open a blank loot table editor |
| `/lootrconf editor <name>` | Open the editor loaded with an existing saved table |
| `/lootrconf list` | List all saved loot tables (clickable in chat) |
| `/lootrconf delete <name>` | Delete a saved loot table |
| `/lootrconf apply <name>` | Apply a saved table to the container you are looking at |
| `/lootrconf randomize-all` | Immediately randomize all loaded containers |
| `/lootrconf reload-config` | Acknowledge a config reload |

### Editor GUI Layout

```
┌──────────────────────────────────────────────────┐
│ [Table Name Field]                  Lootr: ON/OFF │
├──────────┬───────────────────────────────────────┤
│  Pools   │  Entries                               │
│          │  item_id   W:10 Q:0 C:1-3              │
│  Pool 1  │  item_id   W:5  Q:1 C:1-1             │
│  Pool 2  │  ...                                   │
│  ...     │                                        │
├──────────┴───────────────────────────────────────┤
│ [+Pool] [-Pool]  [+Entry] [-Entry]               │
│ [Common] [Uncommon] [Rare] [Epic] [Legendary]    │
│                                          [Save]  │
└──────────────────────────────────────────────────┘
```

- **Left sidebar** — pool list; click a pool to select it
- **Right panel** — entries for the selected pool; click an entry to select it
- **Rarity buttons** — set the weight of the selected entry
- **Save** — serialises the table to JSON, saves it to the world's `LootrConf` datapack, and hot-reloads all datapacks

### Where Tables Are Saved

Tables are saved as a world-local datapack at:

```
<world>/datapacks/LootrConf/data/lootrconf/loot_tables/chests/<name>.json
```

The resource location used in-game is:

```
lootrconf:chests/<name>
```

---

## Configuration

The config file is generated at `config/lootrconf-common.toml` on first launch.

```toml
[randomize]
    # Enable randomizing all chest loot tables on chunk load
    enabled = false

    # List of loot table IDs to randomly assign
    # Example: ["lootrconf:chests/common_loot", "lootrconf:chests/rare_loot"]
    tables = []

    # Weights for each table (same order, higher = more likely)
    weights = []

    # Scan radius around spawn (0 = no limit)
    radius = 0

    # Also randomize barrels
    include_barrels = true

    # Also randomize shulker boxes
    include_shulker_boxes = false
```

---

## Lootr Integration

When [Lootr](https://www.curseforge.com/minecraft/mc-mods/lootr) is installed, the editor GUI shows a green **Lootr: ON** indicator. Loot tables assigned via LootrConf will be instanced per-player by Lootr automatically — no extra configuration needed.

Without Lootr, tables behave as standard Minecraft loot tables (first opener gets the loot).

---

## License

MIT — see [LICENSE](LICENSE) for details.