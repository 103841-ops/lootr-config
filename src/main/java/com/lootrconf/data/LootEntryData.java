package com.lootrconf.data;

import java.util.ArrayList;
import java.util.List;

public class LootEntryData {

    public static class LootTable {
        public String type = "minecraft:chest";
        public List<LootPool> pools = new ArrayList<>();
    }

    public static class LootPool {
        public String name = "pool";
        public float rollsMin = 1.0f;
        public float rollsMax = 3.0f;
        public float bonusRollsMin = 0.0f;
        public float bonusRollsMax = 0.0f;
        public List<LootEntry> entries = new ArrayList<>();
        public List<LootCondition> conditions = new ArrayList<>();
    }

    public static class LootEntry {
        public String item = "minecraft:stone";
        public int weight = 1;
        public int quality = 0;
        public int countMin = 1;
        public int countMax = 1;
        public List<LootFunction> functions = new ArrayList<>();
        public List<LootCondition> conditions = new ArrayList<>();
    }

    public static class LootFunction {
        public String type = "";
        // enchant_randomly
        public List<String> enchantments = new ArrayList<>();
        // enchant_with_levels
        public float levelsMin = 1.0f;
        public float levelsMax = 30.0f;
        public boolean treasure = false;
        // set_name
        public String name = "";
        // set_nbt
        public String nbt = "";
        // set_damage
        public float damageMin = 0.0f;
        public float damageMax = 1.0f;
        // looting_enchant
        public float lootingCountMin = 0.0f;
        public float lootingCountMax = 1.0f;
        // set_attributes
        public String attributeName = "";
        public String attributeOperation = "addition";
        public float attributeAmountMin = 1.0f;
        public float attributeAmountMax = 1.0f;
        // exploration_map
        public String explorationDestination = "on_treasure_maps";
        public String explorationDecoration = "mansion";
        public int explorationZoom = 2;
        public int explorationSearchRadius = 50;
        // set_potion
        public String potionId = "minecraft:water";
        // copy_name
        public String copyNameSource = "block_entity";
    }

    public static class LootCondition {
        public String type = "";
        // random_chance
        public float chance = 0.5f;
        // random_chance_with_looting
        public float lootingMultiplier = 0.1f;
        // inverted - wraps another condition
        public LootCondition term = null;
    }
}
