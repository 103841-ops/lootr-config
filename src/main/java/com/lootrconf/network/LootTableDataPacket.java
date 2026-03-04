package com.lootrconf.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LootTableDataPacket {
    private final String name;
    private final String json;

    public LootTableDataPacket(String name, String json) {
        this.name = name;
        this.json = json;
    }

    public static void encode(LootTableDataPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.name);
        buf.writeUtf(packet.json, 65536);
    }

    public static LootTableDataPacket decode(FriendlyByteBuf buf) {
        return new LootTableDataPacket(buf.readUtf(), buf.readUtf(65536));
    }

    public static void handle(LootTableDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
                com.lootrconf.client.screen.LootTableEditorScreen screen =
                        new com.lootrconf.client.screen.LootTableEditorScreen(packet.name);
                if (!packet.json.isEmpty()) {
                    screen.loadFromJson(packet.json);
                }
                Minecraft.getInstance().setScreen(screen);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
