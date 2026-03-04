package com.lootrconf.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent Server → Client to tell the client to open the loot table editor screen.
 * Contains an optional table name to pre-load (empty string = new table).
 */
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
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.lootrconf.client.ClientProxy.openEditor(packet.tableName))
        );
        ctx.get().setPacketHandled(true);
    }

    public String getTableName() {
        return tableName;
    }
}
