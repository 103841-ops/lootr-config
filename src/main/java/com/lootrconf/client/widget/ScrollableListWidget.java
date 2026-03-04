package com.lootrconf.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A scrollable list widget that renders a list of string items and fires a selection callback.
 */
@OnlyIn(Dist.CLIENT)
public class ScrollableListWidget extends AbstractWidget {

    private final Font font;
    private final List<String> items;
    private final BiConsumer<Integer, String> onSelect;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private static final int ROW_H = 14;

    public ScrollableListWidget(Font font, int x, int y, int width, int height,
                                 List<String> items, BiConsumer<Integer, String> onSelect) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.items = items;
        this.onSelect = onSelect;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x88000000);
        int visibleRows = height / ROW_H;
        for (int i = scrollOffset; i < items.size() && i < scrollOffset + visibleRows; i++) {
            int rowY = getY() + (i - scrollOffset) * ROW_H;
            boolean sel = (i == selectedIndex);
            graphics.fill(getX(), rowY, getX() + width, rowY + ROW_H - 1, sel ? 0x66FFAA00 : 0x00000000);
            graphics.drawString(font, items.get(i), getX() + 2, rowY + 2, sel ? 0xFFD700 : 0xCCCCCC);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered()) return false;
        int row = ((int) mouseY - getY()) / ROW_H + scrollOffset;
        if (row >= 0 && row < items.size()) {
            selectedIndex = row;
            onSelect.accept(row, items.get(row));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isHovered()) return false;
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) Math.signum(delta),
                Math.max(0, items.size() - height / ROW_H)));
        return true;
    }

    public int getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(int idx) { selectedIndex = idx; }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}
