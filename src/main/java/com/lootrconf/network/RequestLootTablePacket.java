package com.lootrconf.network;

import com.lootrconf.data.LootTableManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

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
            String json = LootTableManager.load(packet.name);
            String data = json != null ? json : "";
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new LootTableDataPacket(packet.name, data)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
