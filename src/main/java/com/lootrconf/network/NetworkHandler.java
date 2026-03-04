package com.lootrconf.network;

import com.lootrconf.LootrConf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LootrConf.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(id++, OpenEditorPacket.class, OpenEditorPacket::encode, OpenEditorPacket::decode, OpenEditorPacket::handle);
        CHANNEL.registerMessage(id++, SaveLootTablePacket.class, SaveLootTablePacket::encode, SaveLootTablePacket::decode, SaveLootTablePacket::handle);
        CHANNEL.registerMessage(id++, RequestLootTablePacket.class, RequestLootTablePacket::encode, RequestLootTablePacket::decode, RequestLootTablePacket::handle);
        CHANNEL.registerMessage(id++, LootTableDataPacket.class, LootTableDataPacket::encode, LootTableDataPacket::decode, LootTableDataPacket::handle);
        CHANNEL.registerMessage(id++, ApplyToChestPacket.class, ApplyToChestPacket::encode, ApplyToChestPacket::decode, ApplyToChestPacket::handle);
    }
}
