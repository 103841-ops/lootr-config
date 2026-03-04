package com.lootrconf.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lootrconf.data.LootEntryData.*;

/**
 * Converts in-memory {@link LootTableData} objects into valid Minecraft 1.20.1 loot table JSON.
 */
public class LootTableBuilder {

    public static JsonObject build(LootTableData table) {
        JsonObject root = new JsonObject();
        root.addProperty("type", table.type);

        JsonArray pools = new JsonArray();
        for (PoolData pool : table.pools) {
            pools.add(buildPool(pool));
        }
        root.add("pools", pools);
        return root;
    }

    private static JsonObject buildPool(PoolData pool) {
        JsonObject obj = new JsonObject();
        obj.add("rolls", uniformRange(pool.rollsMin, pool.rollsMax));
        obj.add("bonus_rolls", uniformRange(pool.bonusRollsMin, pool.bonusRollsMax));

        JsonArray entries = new JsonArray();
        for (EntryData entry : pool.entries) {
            entries.add(buildEntry(entry));
        }
        obj.add("entries", entries);

        if (!pool.conditions.isEmpty()) {
            obj.add("conditions", buildConditions(pool.conditions));
        }
        return obj;
    }

    private static JsonObject buildEntry(EntryData entry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:item");
        obj.addProperty("name", entry.itemId);
        obj.addProperty("weight", entry.weight);
        if (entry.quality != 0) {
            obj.addProperty("quality", entry.quality);
        }

        JsonArray functions = new JsonArray();
        // set_count function
        JsonObject setCount = new JsonObject();
        setCount.addProperty("function", "minecraft:set_count");
        setCount.add("count", uniformRange(entry.countMin, entry.countMax));
        functions.add(setCount);

        for (FunctionData fn : entry.functions) {
            JsonObject fnObj = buildFunction(fn);
            if (fnObj != null) {
                functions.add(fnObj);
            }
        }
        obj.add("functions", functions);

        if (!entry.conditions.isEmpty()) {
            obj.add("conditions", buildConditions(entry.conditions));
        }
        return obj;
    }

    private static JsonObject buildFunction(FunctionData fn) {
        JsonObject obj = new JsonObject();
        switch (fn.functionType) {
            case "minecraft:enchant_randomly" -> {
                obj.addProperty("function", "minecraft:enchant_randomly");
                if (!fn.enchantments.isEmpty()) {
                    JsonArray arr = new JsonArray();
                    fn.enchantments.forEach(arr::add);
                    obj.add("enchantments", arr);
                }
            }
            case "minecraft:enchant_with_levels" -> {
                obj.addProperty("function", "minecraft:enchant_with_levels");
                obj.add("levels", uniformRange(fn.levelsMin, fn.levelsMax));
                obj.addProperty("treasure", fn.treasure);
            }
            case "minecraft:set_name" -> {
                obj.addProperty("function", "minecraft:set_name");
                JsonObject nameJson = new JsonObject();
                nameJson.addProperty("text", fn.name);
                obj.add("name", nameJson);
            }
            case "minecraft:set_nbt" -> {
                obj.addProperty("function", "minecraft:set_nbt");
                obj.addProperty("tag", fn.nbt);
            }
            case "minecraft:set_damage" -> {
                obj.addProperty("function", "minecraft:set_damage");
                obj.add("damage", uniformRange(fn.damageMin, fn.damageMax));
            }
            case "minecraft:looting_enchant" -> {
                obj.addProperty("function", "minecraft:looting_enchant");
                obj.add("count", uniformRange(fn.countMin, fn.countMax));
            }
            case "minecraft:set_attributes" -> {
                obj.addProperty("function", "minecraft:set_attributes");
                JsonArray modifiers = new JsonArray();
                JsonObject modifier = new JsonObject();
                modifier.addProperty("name", fn.attributeName);
                modifier.addProperty("attribute", fn.attributeName);
                modifier.addProperty("operation", fn.attributeOperation);
                modifier.add("amount", uniformRange(fn.attributeAmountMin, fn.attributeAmountMax));
                modifiers.add(modifier);
                obj.add("modifiers", modifiers);
            }
            case "minecraft:exploration_map" -> {
                obj.addProperty("function", "minecraft:exploration_map");
                obj.addProperty("destination", fn.destination);
                obj.addProperty("decoration", fn.decoration);
                obj.addProperty("zoom", fn.zoom);
                obj.addProperty("search_radius", fn.searchRadius);
            }
            case "minecraft:set_potion" -> {
                obj.addProperty("function", "minecraft:set_potion");
                obj.addProperty("id", fn.potionId);
            }
            case "minecraft:copy_name" -> {
                obj.addProperty("function", "minecraft:copy_name");
                obj.addProperty("source", fn.copyNameSource);
            }
            default -> {
                return null;
            }
        }
        return obj;
    }

    private static JsonArray buildConditions(java.util.List<ConditionData> conditions) {
        JsonArray arr = new JsonArray();
        for (ConditionData cond : conditions) {
            JsonObject obj = buildCondition(cond);
            if (obj != null) {
                arr.add(obj);
            }
        }
        return arr;
    }

    private static JsonObject buildCondition(ConditionData cond) {
        JsonObject obj = new JsonObject();
        switch (cond.conditionType) {
            case "minecraft:random_chance" -> {
                obj.addProperty("condition", "minecraft:random_chance");
                obj.addProperty("chance", cond.chance);
            }
            case "minecraft:random_chance_with_looting" -> {
                obj.addProperty("condition", "minecraft:random_chance_with_looting");
                obj.addProperty("chance", cond.chance);
                obj.addProperty("looting_multiplier", cond.lootingMultiplier);
            }
            case "minecraft:inverted" -> {
                obj.addProperty("condition", "minecraft:inverted");
                if (cond.innerCondition != null) {
                    obj.add("term", buildCondition(cond.innerCondition));
                }
            }
            case "minecraft:killed_by_player" -> obj.addProperty("condition", "minecraft:killed_by_player");
            case "minecraft:survives_explosion" -> obj.addProperty("condition", "minecraft:survives_explosion");
            default -> {
                return null;
            }
        }
        return obj;
    }

    private static JsonObject uniformRange(float min, float max) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:uniform");
        obj.addProperty("min", min);
        obj.addProperty("max", max);
        return obj;
    }
}
