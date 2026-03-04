package com.lootrconf.handler;

import com.lootrconf.LootrConf;
import com.lootrconf.config.LootrConfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = LootrConf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChestRandomizerHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!LootrConfConfig.RANDOMIZE_ENABLED.get()) return;
        if (event.getLevel().isClientSide()) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        List<? extends String> tables = LootrConfConfig.RANDOMIZE_TABLES.get();
        if (tables.isEmpty()) return;

        int radius = LootrConfConfig.RANDOMIZE_RADIUS.get();
        if (radius > 0 && event.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos spawn = serverLevel.getSharedSpawnPos();
            ChunkPos chunkPos = chunk.getPos();
            int chunkCenterX = chunkPos.getMinBlockX() + 8;
            int chunkCenterZ = chunkPos.getMinBlockZ() + 8;
            double dist = Math.sqrt(Math.pow(chunkCenterX - spawn.getX(), 2) + Math.pow(chunkCenterZ - spawn.getZ(), 2));
            if (dist > radius) return;
        }

        List<? extends Integer> weights = LootrConfConfig.RANDOMIZE_WEIGHTS.get();
        String tableName = selectWeightedRandom(tables, weights);
        if (tableName == null) return;

        ResourceLocation tableId = ResourceLocation.tryParse(tableName);
        if (tableId == null) return;

        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (!shouldRandomize(be)) continue;
            if (be instanceof RandomizableContainerBlockEntity container) {
                // Only set if no loot table is already assigned
                if (container.getLootTable() == null) {
                    container.setLootTable(tableId, RANDOM.nextLong());
                    be.setChanged();
                }
            }
        }
    }

    public static int randomizeLoadedChunks(MinecraftServer server) {
        if (server == null) return 0;

        List<? extends String> tables = LootrConfConfig.RANDOMIZE_TABLES.get();
        if (tables.isEmpty()) return 0;
        List<? extends Integer> weights = LootrConfConfig.RANDOMIZE_WEIGHTS.get();

        int count = 0;
        for (var level : server.getAllLevels()) {
            for (LevelChunk chunk : level.getChunkSource().getLoadedChunks()) {
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!shouldRandomize(be)) continue;
                    if (be instanceof RandomizableContainerBlockEntity container) {
                        if (container.getLootTable() == null) {
                            String tableName = selectWeightedRandom(tables, weights);
                            if (tableName == null) continue;
                            ResourceLocation tableId = ResourceLocation.tryParse(tableName);
                            if (tableId == null) continue;
                            container.setLootTable(tableId, RANDOM.nextLong());
                            be.setChanged();
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private static boolean shouldRandomize(BlockEntity be) {
        if (be instanceof ChestBlockEntity) return true;
        if (be instanceof BarrelBlockEntity && LootrConfConfig.INCLUDE_BARRELS.get()) return true;
        if (be instanceof ShulkerBoxBlockEntity && LootrConfConfig.INCLUDE_SHULKER_BOXES.get()) return true;
        return false;
    }

    private static String selectWeightedRandom(List<? extends String> tables, List<? extends Integer> weights) {
        if (tables.isEmpty()) return null;
        if (weights.isEmpty() || weights.size() != tables.size()) {
            return tables.get(RANDOM.nextInt(tables.size()));
        }
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) return tables.get(RANDOM.nextInt(tables.size()));
        int roll = RANDOM.nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < tables.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) return tables.get(i);
        }
        return tables.get(tables.size() - 1);
    }
}
