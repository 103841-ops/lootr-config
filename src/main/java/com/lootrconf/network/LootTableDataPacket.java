package com.lootrconf.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent Server → Client with loot table data (JSON string) for editing.
 */
public class LootTableDataPacket {

    private final String name;
    private final String tableJson;

    public LootTableDataPacket(String name, String tableJson) {
        this.name = name;
        this.tableJson = tableJson;
    }

    public static void encode(LootTableDataPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.name);
        buf.writeUtf(packet.tableJson, 1 << 20);
    }

    public static LootTableDataPacket decode(FriendlyByteBuf buf) {
        return new LootTableDataPacket(buf.readUtf(), buf.readUtf(1 << 20));
    }

    public static void handle(LootTableDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.lootrconf.client.ClientProxy.openEditorWithData(packet.name, packet.tableJson))
        );
        ctx.get().setPacketHandled(true);
    }

    public String getName() { return name; }
    public String getTableJson() { return tableJson; }
}
