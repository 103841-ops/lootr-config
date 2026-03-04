package com.lootrconf.network;

import com.google.gson.Gson;
import com.lootrconf.LootrConf;
import com.lootrconf.data.LootEntryData;
import com.lootrconf.data.LootTableBuilder;
import com.lootrconf.data.LootTableManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Sent Client → Server to request loading an existing loot table for editing.
 */
public class RequestLootTablePacket {

    private final String name;

    public RequestLootTablePacket(String name) {
        this.name = name;
    }

    public static void encode(RequestLootTablePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.name);
    }

    public static RequestLootTablePacket decode(FriendlyByteBuf buf) {
        return new RequestLootTablePacket(buf.readUtf());
    }

    public static void handle(RequestLootTablePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cInsufficient permissions."));
                return;
            }
            try {
                LootEntryData.LootTableData table = LootTableManager.load(player.getServer(), packet.name);
                String json = table != null ? new Gson().toJson(LootTableBuilder.build(table)) : "";
                NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new LootTableDataPacket(packet.name, json)
                );
            } catch (Exception e) {
                LootrConf.LOGGER.error("Failed to load loot table '{}'", packet.name, e);
                player.sendSystemMessage(Component.literal("§cFailed to load loot table: " + e.getMessage()));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
