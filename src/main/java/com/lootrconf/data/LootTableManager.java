package com.lootrconf.data;

import com.lootrconf.LootrConf;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LootTableManager {
    private static final String DATAPACK_NAME = "LootrConf";
    private static final String PACK_MCMETA = """
            {
              "pack": {
                "description": "LootrConf Loot Tables",
                "pack_format": 15
              }
            }
            """;

    public static Path getDatapackRoot() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("datapacks")
                .resolve(DATAPACK_NAME);
    }

    public static Path getLootTablePath(String name) {
        Path root = getDatapackRoot();
        if (root == null) return null;
        return root.resolve("data/lootrconf/loot_tables/chests/" + name + ".json");
    }

    public static boolean save(String name, String json) {
        Path path = getLootTablePath(name);
        if (path == null) return false;
        try {
            Files.createDirectories(path.getParent());
            ensurePackMcmeta();
            Files.writeString(path, json, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LootrConf.LOGGER.error("Failed to save loot table '{}': {}", name, e.getMessage());
            return false;
        }
    }

    public static String load(String name) {
        Path path = getLootTablePath(name);
        if (path == null || !Files.exists(path)) return null;
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LootrConf.LOGGER.error("Failed to load loot table '{}': {}", name, e.getMessage());
            return null;
        }
    }

    public static boolean delete(String name) {
        Path path = getLootTablePath(name);
        if (path == null || !Files.exists(path)) return false;
        try {
            Files.delete(path);
            return true;
        } catch (IOException e) {
            LootrConf.LOGGER.error("Failed to delete loot table '{}': {}", name, e.getMessage());
            return false;
        }
    }

    public static List<String> list() {
        Path root = getDatapackRoot();
        List<String> names = new ArrayList<>();
        if (root == null) return names;
        Path dir = root.resolve("data/lootrconf/loot_tables/chests/");
        if (!Files.exists(dir)) return names;
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replace(".json", ""))
                    .forEach(names::add);
        } catch (IOException e) {
            LootrConf.LOGGER.error("Failed to list loot tables: {}", e.getMessage());
        }
        return names;
    }

    private static void ensurePackMcmeta() throws IOException {
        Path root = getDatapackRoot();
        if (root == null) return;
        Path mcmeta = root.resolve("pack.mcmeta");
        if (!Files.exists(mcmeta)) {
            Files.createDirectories(root);
            Files.writeString(mcmeta, PACK_MCMETA, StandardCharsets.UTF_8);
        }
    }
}
