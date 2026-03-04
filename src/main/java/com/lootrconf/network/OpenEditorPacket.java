package com.lootrconf.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenEditorPacket {
    private final String tableName;

    public OpenEditorPacket(String tableName) {
        this.tableName = tableName;
    }

    public static void encode(OpenEditorPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.tableName);
    }

    public static OpenEditorPacket decode(FriendlyByteBuf buf) {
        return new OpenEditorPacket(buf.readUtf());
    }

    public static void handle(OpenEditorPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
                Minecraft.getInstance().setScreen(
                        new com.lootrconf.client.screen.LootTableEditorScreen(packet.tableName)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
