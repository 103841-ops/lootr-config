package com.lootrconf.command;

import com.lootrconf.data.LootEntryData;
import com.lootrconf.data.LootTableManager;
import com.lootrconf.network.NetworkHandler;
import com.lootrconf.network.OpenEditorPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Random;

public class LootrConfCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lootrconf")
                .requires(src -> src.hasPermission(2))

                // /lootrconf editor [name]
                .then(Commands.literal("editor")
                        .executes(ctx -> openEditor(ctx, ""))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> openEditor(ctx, StringArgumentType.getString(ctx, "name")))))

                // /lootrconf apply <table_name>
                .then(Commands.literal("apply")
                        .then(Commands.argument("table_name", StringArgumentType.string())
                                .executes(ctx -> applyToChest(ctx, StringArgumentType.getString(ctx, "table_name")))))

                // /lootrconf list
                .then(Commands.literal("list")
                        .executes(LootrConfCommands::listTables))

                // /lootrconf delete <name>
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> deleteTable(ctx, StringArgumentType.getString(ctx, "name")))))

                // /lootrconf randomize-all
                .then(Commands.literal("randomize-all")
                        .executes(LootrConfCommands::randomizeAll))

                // /lootrconf reload-config
                .then(Commands.literal("reload-config")
                        .executes(LootrConfCommands::reloadConfig))
        );
    }

    // ── /lootrconf editor [name] ──────────────────────────────────────────────────

    private static int openEditor(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new OpenEditorPacket(name)
            );
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }
    }

    // ── /lootrconf apply <table_name> ─────────────────────────────────────────────

    private static int applyToChest(CommandContext<CommandSourceStack> ctx, String tableName) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            // Use player's look target to find the block
            HitResult hit = player.pick(5.0, 0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                ctx.getSource().sendFailure(Component.literal("You must be looking at a chest, barrel, or shulker box."));
                return 0;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be == null) {
                ctx.getSource().sendFailure(Component.literal("No block entity at that position."));
                return 0;
            }
            CompoundTag tag = be.saveWithoutMetadata();
            ResourceLocation tableId = new ResourceLocation("lootrconf", "chests/" + tableName);
            tag.putString("LootTable", tableId.toString());
            tag.putLong("LootTableSeed", new Random().nextLong());
            be.load(tag);
            be.setChanged();
            ctx.getSource().sendSuccess(() -> Component.literal("§aApplied loot table '" + tableName + "' to block at " + pos.toShortString()), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    // ── /lootrconf list ───────────────────────────────────────────────────────────

    private static int listTables(CommandContext<CommandSourceStack> ctx) {
        try {
            MinecraftServer server = ctx.getSource().getServer();
            List<String> tables = LootTableManager.list(server);
            if (tables.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal("§7No loot tables found."), false);
                return 1;
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§eLootrConf loot tables:"), false);
            for (String name : tables) {
                MutableComponent link = Component.literal("  §a" + name)
                        .withStyle(s -> s.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lootrconf editor " + name)));
                ctx.getSource().sendSuccess(() -> link, false);
            }
            return tables.size();
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error listing tables: " + e.getMessage()));
            return 0;
        }
    }

    // ── /lootrconf delete <name> ──────────────────────────────────────────────────

    private static int deleteTable(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            boolean deleted = LootTableManager.delete(ctx.getSource().getServer(), name);
            if (deleted) {
                ctx.getSource().sendSuccess(() -> Component.literal("§aDeleted loot table '" + name + "'."), false);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("Table '" + name + "' not found."));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    // ── /lootrconf randomize-all ──────────────────────────────────────────────────

    private static int randomizeAll(CommandContext<CommandSourceStack> ctx) {
        com.lootrconf.handler.ChestRandomizerHandler.randomizeAllLoadedChests(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.literal("§aRandomization triggered for all loaded chunks."), false);
        return 1;
    }

    // ── /lootrconf reload-config ──────────────────────────────────────────────────

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("§eLootrConf config values are reloaded automatically when the file changes. Restart the server to apply manual edits."), false);
        return 1;
    }
}
