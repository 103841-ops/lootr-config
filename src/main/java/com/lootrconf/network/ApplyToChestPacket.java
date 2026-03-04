package com.lootrconf.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class ApplyToChestPacket {
    private final String tableName;
    private final BlockPos pos;

    public ApplyToChestPacket(String tableName, BlockPos pos) {
        this.tableName = tableName;
        this.pos = pos;
    }

    public static void encode(ApplyToChestPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.tableName);
        buf.writeBlockPos(packet.pos);
    }

    public static ApplyToChestPacket decode(FriendlyByteBuf buf) {
        return new ApplyToChestPacket(buf.readUtf(), buf.readBlockPos());
    }

    public static void handle(ApplyToChestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            BlockEntity be = level.getBlockEntity(packet.pos);

            if (be instanceof RandomizableContainerBlockEntity container) {
                ResourceLocation tableId = new ResourceLocation("lootrconf", "chests/" + packet.tableName);
                container.setLootTable(tableId, new Random().nextLong());
                be.setChanged();
                player.sendSystemMessage(Component.literal("§aApplied loot table 'lootrconf:chests/" + packet.tableName + "' to the container."));
            } else {
                player.sendSystemMessage(Component.literal("§cNo valid container found at that position."));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
