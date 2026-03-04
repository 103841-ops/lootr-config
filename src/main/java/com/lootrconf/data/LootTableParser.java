package com.lootrconf.data;

import com.google.gson.*;
import com.lootrconf.data.LootEntryData.*;

import java.util.ArrayList;

/**
 * Parses a Minecraft 1.20.1 loot table JSON object back into {@link LootTableData}.
 */
public class LootTableParser {

    public static LootTableData parse(JsonObject root) {
        LootTableData table = new LootTableData();
        if (root.has("type")) {
            table.type = root.get("type").getAsString();
        }
        if (root.has("pools")) {
            for (JsonElement poolEl : root.getAsJsonArray("pools")) {
                table.pools.add(parsePool(poolEl.getAsJsonObject()));
            }
        }
        return table;
    }

    private static PoolData parsePool(JsonObject obj) {
        PoolData pool = new PoolData();
        if (obj.has("rolls")) {
            JsonElement rolls = obj.get("rolls");
            if (rolls.isJsonObject()) {
                JsonObject r = rolls.getAsJsonObject();
                pool.rollsMin = r.has("min") ? r.get("min").getAsFloat() : 1f;
                pool.rollsMax = r.has("max") ? r.get("max").getAsFloat() : 1f;
            } else {
                pool.rollsMin = pool.rollsMax = rolls.getAsFloat();
            }
        }
        if (obj.has("bonus_rolls")) {
            JsonElement br = obj.get("bonus_rolls");
            if (br.isJsonObject()) {
                JsonObject r = br.getAsJsonObject();
                pool.bonusRollsMin = r.has("min") ? r.get("min").getAsFloat() : 0f;
                pool.bonusRollsMax = r.has("max") ? r.get("max").getAsFloat() : 0f;
            } else {
                pool.bonusRollsMin = pool.bonusRollsMax = br.getAsFloat();
            }
        }
        if (obj.has("entries")) {
            for (JsonElement entryEl : obj.getAsJsonArray("entries")) {
                pool.entries.add(parseEntry(entryEl.getAsJsonObject()));
            }
        }
        if (obj.has("conditions")) {
            for (JsonElement condEl : obj.getAsJsonArray("conditions")) {
                ConditionData cond = parseCondition(condEl.getAsJsonObject());
                if (cond != null) pool.conditions.add(cond);
            }
        }
        return pool;
    }

    private static EntryData parseEntry(JsonObject obj) {
        EntryData entry = new EntryData();
        entry.itemId = obj.has("name") ? obj.get("name").getAsString() : "minecraft:dirt";
        entry.weight = obj.has("weight") ? obj.get("weight").getAsInt() : 1;
        entry.quality = obj.has("quality") ? obj.get("quality").getAsInt() : 0;

        if (obj.has("functions")) {
            for (JsonElement fnEl : obj.getAsJsonArray("functions")) {
                JsonObject fnObj = fnEl.getAsJsonObject();
                String fnType = fnObj.has("function") ? fnObj.get("function").getAsString() : "";
                if ("minecraft:set_count".equals(fnType)) {
                    if (fnObj.has("count")) {
                        JsonElement count = fnObj.get("count");
                        if (count.isJsonObject()) {
                            JsonObject c = count.getAsJsonObject();
                            entry.countMin = c.has("min") ? c.get("min").getAsInt() : 1;
                            entry.countMax = c.has("max") ? c.get("max").getAsInt() : 1;
                        } else {
                            entry.countMin = entry.countMax = count.getAsInt();
                        }
                    }
                } else {
                    FunctionData fn = parseFunction(fnObj);
                    if (fn != null) entry.functions.add(fn);
                }
            }
        }
        if (obj.has("conditions")) {
            for (JsonElement condEl : obj.getAsJsonArray("conditions")) {
                ConditionData cond = parseCondition(condEl.getAsJsonObject());
                if (cond != null) entry.conditions.add(cond);
            }
        }
        return entry;
    }

    private static FunctionData parseFunction(JsonObject obj) {
        String type = obj.has("function") ? obj.get("function").getAsString() : "";
        FunctionData fn = new FunctionData(type);
        switch (type) {
            case "minecraft:enchant_randomly" -> {
                if (obj.has("enchantments")) {
                    fn.enchantments = new ArrayList<>();
                    for (JsonElement e : obj.getAsJsonArray("enchantments")) {
                        fn.enchantments.add(e.getAsString());
                    }
                }
            }
            case "minecraft:enchant_with_levels" -> {
                if (obj.has("levels")) {
                    JsonObject lvl = obj.getAsJsonObject("levels");
                    fn.levelsMin = lvl.has("min") ? lvl.get("min").getAsFloat() : 1f;
                    fn.levelsMax = lvl.has("max") ? lvl.get("max").getAsFloat() : 30f;
                }
                fn.treasure = obj.has("treasure") && obj.get("treasure").getAsBoolean();
            }
            case "minecraft:set_name" -> {
                if (obj.has("name")) {
                    JsonElement nameEl = obj.get("name");
                    fn.name = nameEl.isJsonObject()
                            ? nameEl.getAsJsonObject().get("text").getAsString()
                            : nameEl.getAsString();
                }
            }
            case "minecraft:set_nbt" -> fn.nbt = obj.has("tag") ? obj.get("tag").getAsString() : "";
            case "minecraft:set_damage" -> {
                if (obj.has("damage")) {
                    JsonObject d = obj.getAsJsonObject("damage");
                    fn.damageMin = d.has("min") ? d.get("min").getAsFloat() : 0f;
                    fn.damageMax = d.has("max") ? d.get("max").getAsFloat() : 1f;
                }
            }
            case "minecraft:looting_enchant" -> {
                if (obj.has("count")) {
                    JsonObject c = obj.getAsJsonObject("count");
                    fn.countMin = c.has("min") ? c.get("min").getAsFloat() : 0f;
                    fn.countMax = c.has("max") ? c.get("max").getAsFloat() : 1f;
                }
            }
            case "minecraft:set_attributes" -> {
                if (obj.has("modifiers")) {
                    JsonArray mods = obj.getAsJsonArray("modifiers");
                    if (mods.size() > 0) {
                        JsonObject mod = mods.get(0).getAsJsonObject();
                        fn.attributeName = mod.has("attribute") ? mod.get("attribute").getAsString() : "";
                        fn.attributeOperation = mod.has("operation") ? mod.get("operation").getAsString() : "addition";
                        if (mod.has("amount")) {
                            JsonObject amt = mod.getAsJsonObject("amount");
                            fn.attributeAmountMin = amt.has("min") ? amt.get("min").getAsFloat() : 0f;
                            fn.attributeAmountMax = amt.has("max") ? amt.get("max").getAsFloat() : 1f;
                        }
                    }
                }
            }
            case "minecraft:exploration_map" -> {
                fn.destination = obj.has("destination") ? obj.get("destination").getAsString() : "on_treasure_maps";
                fn.decoration = obj.has("decoration") ? obj.get("decoration").getAsString() : "mansion";
                fn.zoom = obj.has("zoom") ? obj.get("zoom").getAsInt() : 2;
                fn.searchRadius = obj.has("search_radius") ? obj.get("search_radius").getAsInt() : 50;
            }
            case "minecraft:set_potion" -> fn.potionId = obj.has("id") ? obj.get("id").getAsString() : "minecraft:water";
            case "minecraft:copy_name" -> fn.copyNameSource = obj.has("source") ? obj.get("source").getAsString() : "block_entity";
            default -> { return null; }
        }
        return fn;
    }

    private static ConditionData parseCondition(JsonObject obj) {
        String type = obj.has("condition") ? obj.get("condition").getAsString() : "";
        ConditionData cond = new ConditionData(type);
        switch (type) {
            case "minecraft:random_chance" -> cond.chance = obj.has("chance") ? obj.get("chance").getAsFloat() : 0.5f;
            case "minecraft:random_chance_with_looting" -> {
                cond.chance = obj.has("chance") ? obj.get("chance").getAsFloat() : 0.5f;
                cond.lootingMultiplier = obj.has("looting_multiplier") ? obj.get("looting_multiplier").getAsFloat() : 0.1f;
            }
            case "minecraft:inverted" -> {
                if (obj.has("term")) {
                    cond.innerCondition = parseCondition(obj.getAsJsonObject("term"));
                }
            }
            case "minecraft:killed_by_player", "minecraft:survives_explosion" -> {}
            default -> { return null; }
        }
        return cond;
    }
}
