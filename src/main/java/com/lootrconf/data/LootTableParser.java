package com.lootrconf.data;

import com.google.gson.*;

public class LootTableParser {
    public static LootEntryData.LootTable parse(String json) {
        LootEntryData.LootTable table = new LootEntryData.LootTable();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("type")) table.type = root.get("type").getAsString();

            if (root.has("pools")) {
                for (JsonElement poolEl : root.getAsJsonArray("pools")) {
                    table.pools.add(parsePool(poolEl.getAsJsonObject()));
                }
            }
        } catch (Exception e) {
            // Return partial table on error
        }
        return table;
    }

    private static LootEntryData.LootPool parsePool(JsonObject obj) {
        LootEntryData.LootPool pool = new LootEntryData.LootPool();

        if (obj.has("rolls")) {
            JsonObject rolls = obj.getAsJsonObject("rolls");
            pool.rollsMin = rolls.has("min") ? rolls.get("min").getAsFloat() : 1.0f;
            pool.rollsMax = rolls.has("max") ? rolls.get("max").getAsFloat() : 1.0f;
        }

        if (obj.has("bonus_rolls")) {
            JsonObject br = obj.getAsJsonObject("bonus_rolls");
            pool.bonusRollsMin = br.has("min") ? br.get("min").getAsFloat() : 0.0f;
            pool.bonusRollsMax = br.has("max") ? br.get("max").getAsFloat() : 0.0f;
        }

        if (obj.has("entries")) {
            for (JsonElement entryEl : obj.getAsJsonArray("entries")) {
                pool.entries.add(parseEntry(entryEl.getAsJsonObject()));
            }
        }

        if (obj.has("conditions")) {
            for (JsonElement condEl : obj.getAsJsonArray("conditions")) {
                pool.conditions.add(parseCondition(condEl.getAsJsonObject()));
            }
        }

        return pool;
    }

    private static LootEntryData.LootEntry parseEntry(JsonObject obj) {
        LootEntryData.LootEntry entry = new LootEntryData.LootEntry();
        if (obj.has("name")) entry.item = obj.get("name").getAsString();
        if (obj.has("weight")) entry.weight = obj.get("weight").getAsInt();
        if (obj.has("quality")) entry.quality = obj.get("quality").getAsInt();

        if (obj.has("functions")) {
            for (JsonElement funcEl : obj.getAsJsonArray("functions")) {
                JsonObject funcObj = funcEl.getAsJsonObject();
                String funcType = funcObj.has("function") ? funcObj.get("function").getAsString() : "";
                if (funcType.equals("minecraft:set_count")) {
                    JsonObject count = funcObj.getAsJsonObject("count");
                    entry.countMin = count.has("min") ? (int) count.get("min").getAsFloat() : 1;
                    entry.countMax = count.has("max") ? (int) count.get("max").getAsFloat() : 1;
                } else {
                    entry.functions.add(parseFunction(funcObj));
                }
            }
        }

        if (obj.has("conditions")) {
            for (JsonElement condEl : obj.getAsJsonArray("conditions")) {
                entry.conditions.add(parseCondition(condEl.getAsJsonObject()));
            }
        }

        return entry;
    }

    private static LootEntryData.LootFunction parseFunction(JsonObject obj) {
        LootEntryData.LootFunction func = new LootEntryData.LootFunction();
        func.type = obj.has("function") ? obj.get("function").getAsString() : "";

        switch (func.type) {
            case "minecraft:enchant_randomly" -> {
                if (obj.has("enchantments")) {
                    for (JsonElement e : obj.getAsJsonArray("enchantments")) {
                        func.enchantments.add(e.getAsString());
                    }
                }
            }
            case "minecraft:enchant_with_levels" -> {
                if (obj.has("levels")) {
                    JsonObject levels = obj.getAsJsonObject("levels");
                    func.levelsMin = levels.has("min") ? levels.get("min").getAsFloat() : 1.0f;
                    func.levelsMax = levels.has("max") ? levels.get("max").getAsFloat() : 30.0f;
                }
                func.treasure = obj.has("treasure") && obj.get("treasure").getAsBoolean();
            }
            case "minecraft:set_name" -> {
                if (obj.has("name")) {
                    JsonElement nameEl = obj.get("name");
                    if (nameEl.isJsonObject() && nameEl.getAsJsonObject().has("text")) {
                        func.name = nameEl.getAsJsonObject().get("text").getAsString();
                    }
                }
            }
            case "minecraft:set_nbt" -> {
                if (obj.has("tag")) func.nbt = obj.get("tag").getAsString();
            }
            case "minecraft:set_damage" -> {
                if (obj.has("damage")) {
                    JsonObject dmg = obj.getAsJsonObject("damage");
                    func.damageMin = dmg.has("min") ? dmg.get("min").getAsFloat() : 0.0f;
                    func.damageMax = dmg.has("max") ? dmg.get("max").getAsFloat() : 1.0f;
                }
            }
            case "minecraft:looting_enchant" -> {
                if (obj.has("count")) {
                    JsonObject cnt = obj.getAsJsonObject("count");
                    func.lootingCountMin = cnt.has("min") ? cnt.get("min").getAsFloat() : 0.0f;
                    func.lootingCountMax = cnt.has("max") ? cnt.get("max").getAsFloat() : 1.0f;
                }
            }
            case "minecraft:set_potion" -> {
                if (obj.has("id")) func.potionId = obj.get("id").getAsString();
            }
            case "minecraft:copy_name" -> {
                if (obj.has("source")) func.copyNameSource = obj.get("source").getAsString();
            }
        }

        return func;
    }

    private static LootEntryData.LootCondition parseCondition(JsonObject obj) {
        LootEntryData.LootCondition cond = new LootEntryData.LootCondition();
        cond.type = obj.has("condition") ? obj.get("condition").getAsString() : "";

        switch (cond.type) {
            case "minecraft:random_chance" -> {
                if (obj.has("chance")) cond.chance = obj.get("chance").getAsFloat();
            }
            case "minecraft:random_chance_with_looting" -> {
                if (obj.has("chance")) cond.chance = obj.get("chance").getAsFloat();
                if (obj.has("looting_multiplier")) cond.lootingMultiplier = obj.get("looting_multiplier").getAsFloat();
            }
            case "minecraft:inverted" -> {
                if (obj.has("term")) cond.term = parseCondition(obj.getAsJsonObject("term"));
            }
        }

        return cond;
    }
}
