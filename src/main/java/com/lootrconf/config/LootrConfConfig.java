package com.lootrconf.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class LootrConfConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue RANDOMIZE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANDOMIZE_TABLES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> RANDOMIZE_WEIGHTS;
    public static final ForgeConfigSpec.IntValue RANDOMIZE_RADIUS;
    public static final ForgeConfigSpec.BooleanValue INCLUDE_BARRELS;
    public static final ForgeConfigSpec.BooleanValue INCLUDE_SHULKER_BOXES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Randomization settings").push("randomize");

        RANDOMIZE_ENABLED = builder
                .comment("Enable randomizing all chest loot tables on world load")
                .define("enabled", false);

        RANDOMIZE_TABLES = builder
                .comment("List of custom loot table IDs to randomly assign (e.g. \"lootrconf:chests/common_loot\")")
                .defineList("tables", new ArrayList<>(), o -> o instanceof String);

        RANDOMIZE_WEIGHTS = builder
                .comment("Weights corresponding to each table (same order). Higher = more likely.")
                .defineList("weights", new ArrayList<>(), o -> o instanceof Integer);

        RANDOMIZE_RADIUS = builder
                .comment("Radius around spawn to scan for chests (0 = entire loaded world)")
                .defineInRange("radius", 0, 0, Integer.MAX_VALUE);

        INCLUDE_BARRELS = builder
                .comment("Also randomize barrels")
                .define("include_barrels", true);

        INCLUDE_SHULKER_BOXES = builder
                .comment("Also randomize shulker boxes")
                .define("include_shulker_boxes", false);

        builder.pop();

        SPEC = builder.build();
    }
}
