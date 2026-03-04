package com.lootrconf.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ItemSelectorScreen extends Screen {
    private final Screen parent;
    private final Consumer<String> onSelect;
    private EditBox searchField;
    private List<ResourceLocation> filteredItems = new ArrayList<>();
    private List<ResourceLocation> allItems = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedIndex = -1;

    private static final int ITEMS_PER_ROW = 8;
    private static final int ITEM_SIZE = 20;
    private static final int ROWS_VISIBLE = 8;

    public ItemSelectorScreen(Screen parent, Consumer<String> onSelect) {
        super(Component.literal("Select Item"));
        this.parent = parent;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        super.init();

        allItems.clear();
        for (ResourceLocation key : BuiltInRegistries.ITEM.keySet()) {
            allItems.add(key);
        }
        allItems.sort(ResourceLocation::compareTo);

        searchField = new EditBox(this.font, this.width / 2 - 100, 10, 200, 20, Component.literal("Search..."));
        searchField.setResponder(text -> filterItems(text));
        this.addRenderableWidget(searchField);
        filterItems("");

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> minecraft.setScreen(parent))
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    private void filterItems(String query) {
        filteredItems.clear();
        String lower = query.toLowerCase();
        for (ResourceLocation id : allItems) {
            if (id.toString().contains(lower)) filteredItems.add(id);
        }
        scrollOffset = 0;
        selectedIndex = -1;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int startX = this.width / 2 - (ITEMS_PER_ROW * ITEM_SIZE) / 2;
        int startY = 40;

        int startIdx = scrollOffset * ITEMS_PER_ROW;
        int endIdx = Math.min(startIdx + ROWS_VISIBLE * ITEMS_PER_ROW, filteredItems.size());

        for (int i = startIdx; i < endIdx; i++) {
            int relIdx = i - startIdx;
            int row = relIdx / ITEMS_PER_ROW;
            int col = relIdx % ITEMS_PER_ROW;
            int x = startX + col * ITEM_SIZE;
            int y = startY + row * ITEM_SIZE;

            ResourceLocation itemId = filteredItems.get(i);
            Item item = BuiltInRegistries.ITEM.get(itemId);

            if (i == selectedIndex) {
                graphics.fill(x - 1, y - 1, x + ITEM_SIZE - 1, y + ITEM_SIZE - 1, 0xFFFFFFFF);
            }

            graphics.renderItem(item.getDefaultInstance(), x, y);

            if (mouseX >= x && mouseX < x + ITEM_SIZE && mouseY >= y && mouseY < y + ITEM_SIZE) {
                graphics.renderTooltip(this.font, Component.literal(itemId.toString()), mouseX, mouseY);
            }
        }

        graphics.drawString(this.font, filteredItems.size() + " items", startX, startY - 15, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = this.width / 2 - (ITEMS_PER_ROW * ITEM_SIZE) / 2;
        int startY = 40;

        int startIdx = scrollOffset * ITEMS_PER_ROW;
        int endIdx = Math.min(startIdx + ROWS_VISIBLE * ITEMS_PER_ROW, filteredItems.size());

        for (int i = startIdx; i < endIdx; i++) {
            int relIdx = i - startIdx;
            int row = relIdx / ITEMS_PER_ROW;
            int col = relIdx % ITEMS_PER_ROW;
            int x = startX + col * ITEM_SIZE;
            int y = startY + row * ITEM_SIZE;

            if (mouseX >= x && mouseX < x + ITEM_SIZE && mouseY >= y && mouseY < y + ITEM_SIZE) {
                selectedIndex = i;
                if (button == 0) {
                    ResourceLocation itemId = filteredItems.get(i);
                    onSelect.accept(itemId.toString());
                    minecraft.setScreen(parent);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, (filteredItems.size() + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW - ROWS_VISIBLE);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
