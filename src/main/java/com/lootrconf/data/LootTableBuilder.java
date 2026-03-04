package com.lootrconf.data;

import com.google.gson.*;

public class LootTableBuilder {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String build(LootEntryData.LootTable table) {
        JsonObject root = new JsonObject();
        root.addProperty("type", table.type);

        JsonArray pools = new JsonArray();
        for (LootEntryData.LootPool pool : table.pools) {
            pools.add(buildPool(pool));
        }
        root.add("pools", pools);

        return GSON.toJson(root);
    }

    private static JsonObject buildPool(LootEntryData.LootPool pool) {
        JsonObject obj = new JsonObject();
        obj.add("rolls", buildUniform(pool.rollsMin, pool.rollsMax));
        obj.add("bonus_rolls", buildUniform(pool.bonusRollsMin, pool.bonusRollsMax));

        JsonArray entries = new JsonArray();
        for (LootEntryData.LootEntry entry : pool.entries) {
            entries.add(buildEntry(entry));
        }
        obj.add("entries", entries);

        if (!pool.conditions.isEmpty()) {
            JsonArray conds = new JsonArray();
            for (LootEntryData.LootCondition cond : pool.conditions) {
                conds.add(buildCondition(cond));
            }
            obj.add("conditions", conds);
        }

        return obj;
    }

    private static JsonObject buildEntry(LootEntryData.LootEntry entry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:item");
        obj.addProperty("name", entry.item);
        obj.addProperty("weight", entry.weight);
        if (entry.quality != 0) {
            obj.addProperty("quality", entry.quality);
        }

        JsonArray functions = new JsonArray();

        // Always add set_count if count range differs from default
        if (entry.countMin != 1 || entry.countMax != 1) {
            JsonObject setCount = new JsonObject();
            setCount.addProperty("function", "minecraft:set_count");
            setCount.add("count", buildUniform(entry.countMin, entry.countMax));
            functions.add(setCount);
        }

        for (LootEntryData.LootFunction func : entry.functions) {
            functions.add(buildFunction(func));
        }

        if (functions.size() > 0) {
            obj.add("functions", functions);
        }

        if (!entry.conditions.isEmpty()) {
            JsonArray conds = new JsonArray();
            for (LootEntryData.LootCondition cond : entry.conditions) {
                conds.add(buildCondition(cond));
            }
            obj.add("conditions", conds);
        }

        return obj;
    }

    private static JsonObject buildFunction(LootEntryData.LootFunction func) {
        JsonObject obj = new JsonObject();
        obj.addProperty("function", func.type);

        switch (func.type) {
            case "minecraft:enchant_randomly" -> {
                if (!func.enchantments.isEmpty()) {
                    JsonArray enchArr = new JsonArray();
                    func.enchantments.forEach(enchArr::add);
                    obj.add("enchantments", enchArr);
                }
            }
            case "minecraft:enchant_with_levels" -> {
                obj.add("levels", buildUniform(func.levelsMin, func.levelsMax));
                obj.addProperty("treasure", func.treasure);
            }
            case "minecraft:set_name" -> {
                JsonObject nameObj = new JsonObject();
                nameObj.addProperty("text", func.name);
                obj.add("name", nameObj);
            }
            case "minecraft:set_nbt" -> obj.addProperty("tag", func.nbt);
            case "minecraft:set_damage" -> obj.add("damage", buildUniform(func.damageMin, func.damageMax));
            case "minecraft:looting_enchant" -> obj.add("count", buildUniform(func.lootingCountMin, func.lootingCountMax));
            case "minecraft:set_attributes" -> {
                JsonArray modifiers = new JsonArray();
                JsonObject modifier = new JsonObject();
                modifier.addProperty("attribute", func.attributeName);
                modifier.addProperty("name", func.attributeName + "_modifier");
                modifier.addProperty("operation", func.attributeOperation);
                modifier.add("amount", buildUniform(func.attributeAmountMin, func.attributeAmountMax));
                modifiers.add(modifier);
                obj.add("modifiers", modifiers);
            }
            case "minecraft:exploration_map" -> {
                obj.addProperty("destination", func.explorationDestination);
                obj.addProperty("decoration", func.explorationDecoration);
                obj.addProperty("zoom", func.explorationZoom);
                obj.addProperty("search_radius", func.explorationSearchRadius);
            }
            case "minecraft:set_potion" -> obj.addProperty("id", func.potionId);
            case "minecraft:copy_name" -> obj.addProperty("source", func.copyNameSource);
        }

        return obj;
    }

    private static JsonObject buildCondition(LootEntryData.LootCondition cond) {
        JsonObject obj = new JsonObject();
        obj.addProperty("condition", cond.type);

        switch (cond.type) {
            case "minecraft:random_chance" -> obj.addProperty("chance", cond.chance);
            case "minecraft:random_chance_with_looting" -> {
                obj.addProperty("chance", cond.chance);
                obj.addProperty("looting_multiplier", cond.lootingMultiplier);
            }
            case "minecraft:inverted" -> {
                if (cond.term != null) {
                    obj.add("term", buildCondition(cond.term));
                }
            }
        }

        return obj;
    }

    private static JsonObject buildUniform(float min, float max) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:uniform");
        obj.addProperty("min", min);
        obj.addProperty("max", max);
        return obj;
    }

    private static JsonObject buildUniform(int min, int max) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "minecraft:uniform");
        obj.addProperty("min", (float) min);
        obj.addProperty("max", (float) max);
        return obj;
    }
}
