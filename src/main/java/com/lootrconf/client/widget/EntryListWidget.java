package com.lootrconf.client.widget;

import com.lootrconf.client.screen.LootTableEditorScreen;
import com.lootrconf.data.LootEntryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntryListWidget extends ScrollableListWidget<EntryListWidget.EntryRow> {
    private final LootTableEditorScreen screen;
    private LootEntryData.LootPool pool;
    private int selectedIndex = -1;

    public EntryListWidget(LootTableEditorScreen screen, Minecraft minecraft, int width, int height, int y0, int y1,
                           LootEntryData.LootPool pool) {
        super(minecraft, width, height, y0, y1, 24);
        this.screen = screen;
        this.pool = pool;
        refresh(pool);
    }

    public void refresh(LootEntryData.LootPool pool) {
        this.pool = pool;
        this.clearEntries();
        for (int i = 0; i < pool.entries.size(); i++) {
            this.addEntry(new EntryRow(pool.entries.get(i), i));
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @OnlyIn(Dist.CLIENT)
    public class EntryRow extends ObjectSelectionList.Entry<EntryRow> {
        private final LootEntryData.LootEntry entry;
        private final int index;

        public EntryRow(LootEntryData.LootEntry entry, int index) {
            this.entry = entry;
            this.index = index;
        }

        @Override
        public void render(GuiGraphics graphics, int idx, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean selected = (index == selectedIndex);
            if (selected || hovered) {
                graphics.fill(left, top, left + width, top + height, 0x55FFFFFF);
            }

            try {
                ResourceLocation itemId = new ResourceLocation(entry.item);
                Item item = BuiltInRegistries.ITEM.get(itemId);
                graphics.renderItem(item.getDefaultInstance(), left + 2, top + 3);
            } catch (Exception ignored) {}

            graphics.drawString(Minecraft.getInstance().font, entry.item, left + 24, top + 3, 0xFFFFFF);
            graphics.drawString(Minecraft.getInstance().font,
                    "W:" + entry.weight + " Q:" + entry.quality + " C:" + entry.countMin + "-" + entry.countMax,
                    left + 24, top + 13, 0xAAAAAA);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            selectedIndex = index;
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(entry.item);
        }
    }
}
