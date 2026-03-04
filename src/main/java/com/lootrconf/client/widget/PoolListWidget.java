package com.lootrconf.client.widget;

import com.lootrconf.client.screen.LootTableEditorScreen;
import com.lootrconf.data.LootEntryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoolListWidget extends ScrollableListWidget<PoolListWidget.PoolEntry> {
    private final LootTableEditorScreen screen;
    private final LootEntryData.LootTable table;
    private int selectedPool;

    public PoolListWidget(LootTableEditorScreen screen, Minecraft minecraft, int width, int height, int y0, int y1,
                          LootEntryData.LootTable table, int selectedPool) {
        super(minecraft, width, height, y0, y1, 30);
        this.screen = screen;
        this.table = table;
        this.selectedPool = selectedPool;
        refresh();
    }

    public void refresh() {
        this.clearEntries();
        for (int i = 0; i < table.pools.size(); i++) {
            this.addEntry(new PoolEntry(table.pools.get(i), i));
        }
    }

    public void setSelectedPool(int index) {
        this.selectedPool = index;
        if (index >= 0 && index < this.children().size()) {
            this.setSelected(this.children().get(index));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class PoolEntry extends ObjectSelectionList.Entry<PoolEntry> {
        private final LootEntryData.LootPool pool;
        private final int index;

        public PoolEntry(LootEntryData.LootPool pool, int index) {
            this.pool = pool;
            this.index = index;
        }

        @Override
        public void render(GuiGraphics graphics, int idx, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean selected = (index == selectedPool);
            if (selected) {
                graphics.fill(left, top, left + width, top + height, 0x88FFFFFF);
            }
            graphics.drawString(Minecraft.getInstance().font,
                    "Pool " + (index + 1), left + 5, top + 3, 0xFFFFFF);
            graphics.drawString(Minecraft.getInstance().font,
                    "Rolls: " + pool.rollsMin + "-" + pool.rollsMax, left + 5, top + 14, 0xAAAAAA);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            screen.selectPool(index);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal("Pool " + (index + 1));
        }
    }
}
