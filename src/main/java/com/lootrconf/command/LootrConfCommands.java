package com.lootrconf.command;

import com.lootrconf.data.LootTableManager;
import com.lootrconf.handler.ChestRandomizerHandler;
import com.lootrconf.network.NetworkHandler;
import com.lootrconf.network.OpenEditorPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class LootrConfCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lootrconf")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("editor")
                        .executes(ctx -> openEditor(ctx, ""))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> openEditor(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("list")
                        .executes(LootrConfCommands::listTables))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> deleteTable(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("apply")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> applyToChest(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("randomize-all")
                        .executes(LootrConfCommands::randomizeAll))
                .then(Commands.literal("reload-config")
                        .executes(LootrConfCommands::reloadConfig))
        );
    }

    private static int openEditor(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            if (name.isEmpty()) {
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenEditorPacket(""));
            } else {
                String json = LootTableManager.load(name);
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new com.lootrconf.network.LootTableDataPacket(name, json != null ? json : ""));
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Must be a player to use this command."));
            return 0;
        }
    }

    private static int listTables(CommandContext<CommandSourceStack> ctx) {
        List<String> tables = LootTableManager.list();
        if (tables.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§eNo loot tables saved yet."), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§6Saved loot tables:"), false);
            for (String name : tables) {
                MutableComponent entry = Component.literal("§a- " + name)
                        .withStyle(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lootrconf editor " + name)));
                ctx.getSource().sendSuccess(() -> entry, false);
            }
        }
        return tables.size();
    }

    private static int deleteTable(CommandContext<CommandSourceStack> ctx, String name) {
        boolean success = LootTableManager.delete(name);
        if (success) {
            ctx.getSource().sendSuccess(() -> Component.literal("§aDeleted loot table '" + name + "'."), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Loot table '" + name + "' not found."));
            return 0;
        }
    }

    private static int applyToChest(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            HitResult hit = player.pick(5.0, 1.0f, false);
            if (hit instanceof BlockHitResult blockHit) {
                var be = player.serverLevel().getBlockEntity(blockHit.getBlockPos());
                if (be instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity container) {
                    container.setLootTable(new net.minecraft.resources.ResourceLocation("lootrconf", "chests/" + name),
                            new java.util.Random().nextLong());
                    be.setChanged();
                    ctx.getSource().sendSuccess(() -> Component.literal("§aApplied loot table 'lootrconf:chests/" + name + "' to the container."), true);
                    return 1;
                }
            }
            ctx.getSource().sendFailure(Component.literal("Look at a container to apply a loot table."));
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 0;
    }

    private static int randomizeAll(CommandContext<CommandSourceStack> ctx) {
        int count = ChestRandomizerHandler.randomizeLoadedChunks(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.literal("§aRandomized " + count + " containers."), true);
        return count;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("§aLootrConf config reloaded."), true);
        return 1;
    }
}
