package com.lootrconf.network;

import com.lootrconf.data.LootTableManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveLootTablePacket {
    private final String name;
    private final String json;

    public SaveLootTablePacket(String name, String json) {
        this.name = name;
        this.json = json;
    }

    public static void encode(SaveLootTablePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.name);
        buf.writeUtf(packet.json, 65536);
    }

    public static SaveLootTablePacket decode(FriendlyByteBuf buf) {
        return new SaveLootTablePacket(buf.readUtf(), buf.readUtf(65536));
    }

    public static void handle(SaveLootTablePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            boolean success = LootTableManager.save(packet.name, packet.json);
            if (success) {
                player.getServer().reloadResources(player.getServer().getPackRepository().getSelectedIds());
                player.sendSystemMessage(Component.literal("§aLoot table '" + packet.name + "' saved successfully!"));
            } else {
                player.sendSystemMessage(Component.literal("§cFailed to save loot table '" + packet.name + "'."));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
