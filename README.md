# LootrConf

**LootrConf** is a Forge 1.20.1 mod that provides an **in-game GUI loot table editor**.  
You never have to write loot table JSON by hand — just open the editor, build your table,  
hit **Save**, and the mod writes a valid Minecraft loot table JSON into a world datapack.  
If [Lootr](https://www.curseforge.com/minecraft/mc-mods/lootr) is installed, loot will be  
instanced **per player** automatically.

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.2.0+ |
| Java | 17+ |
| Lootr | *Optional* — 0.7+ |

---

## Building

```bash
./gradlew build
```

The resulting JAR is placed in `build/libs/`.

---

## Installation

1. Drop the mod JAR into your `mods/` folder.
2. (Optional) Install [Lootr](https://www.curseforge.com/minecraft/mc-mods/lootr) to enable per-player loot instancing.
3. Launch Forge 1.20.1.

---

## Commands

All commands require **permission level 2** (operator).

| Command | Description |
|---|---|
| `/lootrconf editor` | Open the loot table editor (new table) |
| `/lootrconf editor <name>` | Open the editor and load an existing table |
| `/lootrconf apply <name>` | Apply a loot table to the chest you're looking at |
| `/lootrconf list` | List all saved loot tables (clickable links) |
| `/lootrconf delete <name>` | Delete a saved loot table |
| `/lootrconf randomize-all` | Assign random loot tables to all loaded chests |
| `/lootrconf reload-config` | Reload the config file |

---

## GUI Overview

### Loot Table Editor

- **Left panel** — Pool list. Click a pool to select it. Use **+ Pool** / **- Pool** buttons to add/remove.
- **Center panel** — Entry list for the selected pool. Click an entry to select it.
- **Right panel** — Edit the selected entry: item ID, weight, quality, count min/max.
- **Rarity presets** — Quick-set weight buttons: Common (40), Uncommon (20), Rare (10), Epic (5), Legendary (1).
- **Add Function** — Opens the Function Editor popup to add enchantments, name, NBT, damage, etc.
- **Add Condition** — Opens the Condition Editor popup to add random chance, looting, etc.
- **Bottom bar** — Enter a table name and click **Save** to write the table to disk and reload datapacks.
- **Top-right** — Green/red Lootr status indicator.

### Item Selector

When adding an entry, a searchable item selector lets you pick any registered item.

---

## Loot Table Output

Saved tables are written to:

```
<world_folder>/datapacks/LootrConf/data/lootrconf/loot_tables/chests/<name>.json
```

The resource location used for the loot table is `lootrconf:chests/<name>`.

---

## Chest Randomizer (Map Mode)

Edit `config/lootrconf-common.toml`:

```toml
[randomize]
enabled = true
tables = ["lootrconf:chests/common_loot", "lootrconf:chests/rare_loot"]
weights = [80, 20]
include_barrels = true
include_shulker_boxes = false
```

When `enabled = true`, every chest/barrel loaded in a chunk (that has no existing loot table) gets  
one of the configured tables assigned at random (weighted). If Lootr is installed, loot is instanced  
per player.

---

## Lootr Integration

At startup the mod checks whether Lootr is present:

- **Lootr detected** → "LootrConf: Lootr detected! Loot tables will be instanced per player."
- **Lootr missing** → "LootrConf: Lootr not detected. Loot tables will work as standard Minecraft loot tables."

The editor GUI also shows a green (Lootr ON) or red (Lootr OFF) dot in the top-right corner.

---

## License

MIT
