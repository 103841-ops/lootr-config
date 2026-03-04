package com.lootrconf.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lootrconf.LootrConf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing loot table JSON files inside the world datapack.
 */
public class LootTableManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PACK_NAME = "LootrConf";
    private static final String NAMESPACE = "lootrconf";
    private static final int PACK_FORMAT = 15;

    // ── Path helpers ─────────────────────────────────────────────────────────────

    public static Path getDatapackRoot(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("datapacks").resolve(PACK_NAME);
    }

    public static Path getLootTablePath(MinecraftServer server, String name) {
        return getDatapackRoot(server)
                .resolve("data")
                .resolve(NAMESPACE)
                .resolve("loot_tables")
                .resolve("chests")
                .resolve(name + ".json");
    }

    // ── Save ─────────────────────────────────────────────────────────────────────

    public static void save(MinecraftServer server, String name, LootEntryData.LootTableData table) throws IOException {
        ensurePackMcMeta(server);
        Path file = getLootTablePath(server, name);
        Files.createDirectories(file.getParent());
        JsonObject json = LootTableBuilder.build(table);
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(json, writer);
        }
        LootrConf.LOGGER.info("LootrConf: Saved loot table '{}' to {}", name, file);
    }

    // ── Load ─────────────────────────────────────────────────────────────────────

    public static LootEntryData.LootTableData load(MinecraftServer server, String name) throws IOException {
        Path file = getLootTablePath(server, name);
        if (!Files.exists(file)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            return LootTableParser.parse(json);
        }
    }

    // ── Delete ───────────────────────────────────────────────────────────────────

    public static boolean delete(MinecraftServer server, String name) throws IOException {
        Path file = getLootTablePath(server, name);
        return Files.deleteIfExists(file);
    }

    // ── List ─────────────────────────────────────────────────────────────────────

    public static List<String> list(MinecraftServer server) throws IOException {
        Path dir = getDatapackRoot(server)
                .resolve("data")
                .resolve(NAMESPACE)
                .resolve("loot_tables")
                .resolve("chests");
        List<String> names = new ArrayList<>();
        if (!Files.exists(dir)) return names;
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        names.add(fileName.substring(0, fileName.length() - 5));
                    });
        }
        return names;
    }

    // ── pack.mcmeta ──────────────────────────────────────────────────────────────

    private static void ensurePackMcMeta(MinecraftServer server) throws IOException {
        Path packRoot = getDatapackRoot(server);
        Path mcMeta = packRoot.resolve("pack.mcmeta");
        if (!Files.exists(mcMeta)) {
            Files.createDirectories(packRoot);
            JsonObject pack = new JsonObject();
            JsonObject meta = new JsonObject();
            meta.addProperty("pack_format", PACK_FORMAT);
            meta.addProperty("description", "LootrConf custom loot tables");
            pack.add("pack", meta);
            try (Writer writer = Files.newBufferedWriter(mcMeta)) {
                GSON.toJson(pack, writer);
            }
            LootrConf.LOGGER.info("LootrConf: Created pack.mcmeta at {}", mcMeta);
        }
    }
}
