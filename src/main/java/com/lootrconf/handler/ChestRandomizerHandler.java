package com.lootrconf.handler;

import com.lootrconf.LootrConf;
import com.lootrconf.config.LootrConfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = LootrConf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChestRandomizerHandler {

    private static final Random RANDOM = new Random();
    private static final List<LevelChunk> loadedChunksCache = new ArrayList<>();

    private static final java.util.Set<String> SHULKER_BLOCK_IDS = java.util.Set.of(
            "minecraft:shulker_box",
            "minecraft:white_shulker_box",
            "minecraft:orange_shulker_box",
            "minecraft:magenta_shulker_box",
            "minecraft:light_blue_shulker_box",
            "minecraft:yellow_shulker_box",
            "minecraft:lime_shulker_box",
            "minecraft:pink_shulker_box",
            "minecraft:gray_shulker_box",
            "minecraft:light_gray_shulker_box",
            "minecraft:cyan_shulker_box",
            "minecraft:purple_shulker_box",
            "minecraft:blue_shulker_box",
            "minecraft:brown_shulker_box",
            "minecraft:green_shulker_box",
            "minecraft:red_shulker_box",
            "minecraft:black_shulker_box"
    );

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!LootrConfConfig.RANDOMIZE_ENABLED.get()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(event.getChunk() instanceof LevelChunk levelChunk)) return;

        List<? extends String> tables = LootrConfConfig.RANDOMIZE_TABLES.get();
        List<? extends Integer> weights = LootrConfConfig.RANDOMIZE_WEIGHTS.get();
        if (tables.isEmpty()) return;

        processChunk(levelChunk, tables, weights);
    }

    public static void randomizeAllLoadedChests(MinecraftServer server) {
        List<? extends String> tables = LootrConfConfig.RANDOMIZE_TABLES.get();
        List<? extends Integer> weights = LootrConfConfig.RANDOMIZE_WEIGHTS.get();
        if (tables.isEmpty()) {
            LootrConf.LOGGER.warn("LootrConf: No tables configured for randomization.");
            return;
        }
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (BlockEntity be : level.blockEntityTickers.stream()
                    .map(ticker -> level.getBlockEntity(ticker.getPos()))
                    .filter(java.util.Objects::nonNull)
                    .toList()) {
                // This approach won't easily get all block entities.
                // Instead, use a tracked-chunks approach:
            }
        }
        // Better approach: track chunks as they load and process them
        // For the manual command, we re-scan using the chunk source
        for (ServerLevel level : server.getAllLevels()) {
            int viewDist = level.getServer().getPlayerList().getViewDistance();
            level.players().forEach(player -> {
                int chunkX = player.blockPosition().getX() >> 4;
                int chunkZ = player.blockPosition().getZ() >> 4;
                for (int dx = -viewDist; dx <= viewDist; dx++) {
                    for (int dz = -viewDist; dz <= viewDist; dz++) {
                        LevelChunk lc = level.getChunkSource().getChunkNow(chunkX + dx, chunkZ + dz);
                        if (lc != null) {
                            processChunk(lc, tables, weights);
                            count++;
                        }
                    }
                }
            });
        }
        LootrConf.LOGGER.info("LootrConf: Randomized chests in {} chunks.\", count);
    }

    private static void processChunk(LevelChunk chunk,
                                      List<? extends String> tables,
                                      List<? extends Integer> weights) {
        boolean includeBarrels = LootrConfConfig.INCLUDE_BARRELS.get();
        boolean includeShulkers = LootrConfConfig.INCLUDE_SHULKER_BOXES.get();

        for (var entry : chunk.getBlockEntities().entrySet()) {
            BlockPos pos = entry.getKey();
            BlockEntity be = entry.getValue();
            if (be == null) continue;

            String blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS
                    .getKey(chunk.getBlockState(pos).getBlock()).toString();

            boolean isChest = blockId.equals("minecraft:chest") || blockId.equals("minecraft:trapped_chest")
                    || (includeBarrels && blockId.equals("minecraft:barrel"));
            boolean isShulker = includeShulkers && SHULKER_BLOCK_IDS.contains(blockId);

            if (!isChest && !isShulker) continue;

            CompoundTag fullTag = be.saveWithFullMetadata();
            if (fullTag.contains("LootTable")) continue;

            String chosen = pickWeighted(tables, weights);
            if (chosen == null) continue;

            ResourceLocation tableId = new ResourceLocation(chosen);
            fullTag.putString("LootTable", tableId.toString());
            fullTag.putLong("LootTableSeed", RANDOM.nextLong());
            be.load(fullTag);
            be.setChanged();
        }
    }

    private static String pickWeighted(List<? extends String> tables, List<? extends Integer> weights) {
        if (tables.isEmpty()) return null;
        int totalWeight = 0;
        for (int i = 0; i < tables.size(); i++) {
            int w = (i < weights.size()) ? weights.get(i) : 1;
            totalWeight += Math.max(1, w);
        }
        int roll = RANDOM.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < tables.size(); i++) {
            int w = (i < weights.size()) ? weights.get(i) : 1;
            cumulative += Math.max(1, w);
            if (roll < cumulative) {
                return tables.get(i);
            }
        }
        return tables.get(0);
    }
}