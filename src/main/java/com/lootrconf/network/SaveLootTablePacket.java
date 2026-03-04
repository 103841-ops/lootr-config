package com.lootrconf.network;

import com.lootrconf.LootrConf;
import com.lootrconf.data.LootEntryData;
import com.lootrconf.data.LootTableManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Sent Client → Server to save a loot table and optionally trigger a reload.
 */
public class SaveLootTablePacket {

    private final String name;
    private final String tableJson;

    public SaveLootTablePacket(String name, String tableJson) {
        this.name = name;
        this.tableJson = tableJson;
    }

    public static void encode(SaveLootTablePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.name);
        buf.writeUtf(packet.tableJson, 1 << 20);
    }

    public static SaveLootTablePacket decode(FriendlyByteBuf buf) {
        return new SaveLootTablePacket(buf.readUtf(), buf.readUtf(1 << 20));
    }

    public static void handle(SaveLootTablePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cYou don't have permission to save loot tables."));
                return;
            }
            try {
                com.google.gson.JsonObject json = new com.google.gson.Gson().fromJson(packet.tableJson, com.google.gson.JsonObject.class);
                LootEntryData.LootTableData table = com.lootrconf.data.LootTableParser.parse(json);
                LootTableManager.save(player.getServer(), packet.name, table);
                player.sendSystemMessage(Component.literal("§aLoot table '" + packet.name + "' saved! Use §e/reload§a to activate it."));
            } catch (Exception e) {
                LootrConf.LOGGER.error("Failed to save loot table '{}'", packet.name, e);
                player.sendSystemMessage(Component.literal("§cFailed to save loot table: " + e.getMessage()));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
