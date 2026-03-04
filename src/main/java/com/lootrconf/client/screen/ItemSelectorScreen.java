package com.lootrconf.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Searchable item selector overlay. Presents all registered items in a scrollable list.
 * When the player clicks an item, it calls the consumer and returns to the parent screen.
 */
@OnlyIn(Dist.CLIENT)
public class ItemSelectorScreen extends Screen {

    private final Screen parent;
    private final java.util.function.Consumer<String> onSelect;

    private EditBox searchField;
    private List<String> filteredItems = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ROW_H = 14;
    private static final int ITEMS_PER_PAGE = 20;

    public ItemSelectorScreen(Screen parent, java.util.function.Consumer<String> onSelect) {
        super(Component.literal("Select Item"));
        this.parent = parent;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        super.init();
        searchField = new EditBox(this.font, this.width / 2 - 100, 20, 200, 18, Component.literal("Search"));
        searchField.setResponder(this::updateFilter);
        addRenderableWidget(searchField);

        addRenderableWidget(Button.builder(Component.literal("↑"), btn -> scroll(-5))
                .bounds(this.width / 2 + 105, 40, 20, 14).build());
        addRenderableWidget(Button.builder(Component.literal("↓"), btn -> scroll(5))
                .bounds(this.width / 2 + 105, 40 + ITEMS_PER_PAGE * ROW_H - 14, 20, 14).build());

        addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> minecraft.setScreen(parent))
                .bounds(this.width / 2 - 40, this.height - 28, 80, 18).build());

        updateFilter("");
    }

    private void updateFilter(String query) {
        filteredItems.clear();
        String lower = query.toLowerCase();
        BuiltInRegistries.ITEM.keySet().stream()
                .map(ResourceLocation::toString)
                .filter(id -> id.contains(lower))
                .sorted()
                .forEach(filteredItems::add);
        scrollOffset = 0;
    }

    private void scroll(int delta) {
        scrollOffset = Math.max(0, Math.min(scrollOffset + delta, Math.max(0, filteredItems.size() - ITEMS_PER_PAGE)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        int listX = this.width / 2 - 100;
        int listY = 42;
        for (int i = scrollOffset; i < filteredItems.size() && i < scrollOffset + ITEMS_PER_PAGE; i++) {
            String id = filteredItems.get(i);
            int y = listY + (i - scrollOffset) * ROW_H;
            boolean hovered = mouseX >= listX && mouseX <= listX + 200 && mouseY >= y && mouseY < y + ROW_H;
            graphics.fill(listX, y, listX + 200, y + ROW_H - 1, hovered ? 0x66FFAA00 : 0x33FFFFFF);
            graphics.drawString(this.font, id, listX + 2, y + 2, 0xEEEEEE);
        }
        graphics.drawString(this.font, filteredItems.size() + " items", listX, listY + ITEMS_PER_PAGE * ROW_H + 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = this.width / 2 - 100;
        int listY = 42;
        for (int i = scrollOffset; i < filteredItems.size() && i < scrollOffset + ITEMS_PER_PAGE; i++) {
            int y = listY + (i - scrollOffset) * ROW_H;
            if (mouseX >= listX && mouseX <= listX + 200 && mouseY >= y && mouseY < y + ROW_H) {
                onSelect.accept(filteredItems.get(i));
                minecraft.setScreen(parent);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll(-(int) Math.signum(delta));
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
