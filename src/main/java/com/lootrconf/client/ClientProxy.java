package com.lootrconf.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lootrconf.client.screen.LootTableEditorScreen;
import com.lootrconf.data.LootEntryData;
import com.lootrconf.data.LootTableParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy {

    public static void openEditor(String tableName) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.setScreen(new LootTableEditorScreen(tableName, null)));
    }

    public static void openEditorWithData(String tableName, String tableJson) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            LootEntryData.LootTableData table = null;
            if (tableJson != null && !tableJson.isEmpty()) {
                try {
                    JsonObject json = new Gson().fromJson(tableJson, JsonObject.class);
                    table = LootTableParser.parse(json);
                } catch (Exception ignored) {}
            }
            mc.setScreen(new LootTableEditorScreen(tableName, table));
        });
    }
}
