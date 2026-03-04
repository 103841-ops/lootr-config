package com.lootrconf.network;

import com.lootrconf.LootrConf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Sent Client → Server to apply a loot table to the chest the player is targeting.
 */
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
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cInsufficient permissions."));
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be == null) {
                player.sendSystemMessage(Component.literal("§cNo block entity at that position."));
                return;
            }
            CompoundTag tag = be.saveWithoutMetadata();
            ResourceLocation tableId = new ResourceLocation("lootrconf", "chests/" + packet.tableName);
            tag.putString("LootTable", tableId.toString());
            tag.putLong("LootTableSeed", new Random().nextLong());
            be.load(tag);
            be.setChanged();
            player.sendSystemMessage(Component.literal("§aApplied loot table '" + packet.tableName + "' to chest at " + packet.pos.toShortString()));
            LootrConf.LOGGER.info("Applied loot table '{}' to block entity at {}", packet.tableName, packet.pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
