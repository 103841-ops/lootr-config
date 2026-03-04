package com.lootrconf.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data classes that represent the in-memory state of a loot table being edited in the GUI.
 */
public class LootEntryData {

    // ── Loot Table ──────────────────────────────────────────────────────────────

    public static class LootTableData {
        public String type = "minecraft:chest";
        public List<PoolData> pools = new ArrayList<>();

        public LootTableData() {}
    }

    // ── Pool ────────────────────────────────────────────────────────────────────

    public static class PoolData {
        public float rollsMin = 1.0f;
        public float rollsMax = 3.0f;
        public float bonusRollsMin = 0.0f;
        public float bonusRollsMax = 0.0f;
        public List<EntryData> entries = new ArrayList<>();
        public List<ConditionData> conditions = new ArrayList<>();

        public PoolData() {}
    }

    // ── Entry ───────────────────────────────────────────────────────────────────

    public static class EntryData {
        public String itemId = "minecraft:dirt";
        public int weight = 1;
        public int quality = 0;
        public int countMin = 1;
        public int countMax = 1;
        public List<FunctionData> functions = new ArrayList<>();
        public List<ConditionData> conditions = new ArrayList<>();

        public EntryData() {}

        public EntryData(String itemId) {
            this.itemId = itemId;
        }
    }

    // ── Function ─────────────────────────────────────────────────────────────────

    public static class FunctionData {
        public String functionType = "";
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
        // looting_enchant / set_count
        public float countMin = 0.0f;
        public float countMax = 1.0f;
        // set_attributes (simplified)
        public String attributeName = "";
        public String attributeOperation = "addition";
        public float attributeAmountMin = 0.0f;
        public float attributeAmountMax = 1.0f;
        // exploration_map
        public String destination = "on_treasure_maps";
        public String decoration = "mansion";
        public int zoom = 2;
        public int searchRadius = 50;
        // set_potion
        public String potionId = "minecraft:water";
        // copy_name
        public String copyNameSource = "block_entity";

        public FunctionData() {}

        public FunctionData(String functionType) {
            this.functionType = functionType;
        }
    }

    // ── Condition ────────────────────────────────────────────────────────────────

    public static class ConditionData {
        public String conditionType = "";
        public float chance = 0.5f;
        public float lootingMultiplier = 0.1f;
        // inverted wraps another condition
        public ConditionData innerCondition = null;

        public ConditionData() {}

        public ConditionData(String conditionType) {
            this.conditionType = conditionType;
        }
    }
}
