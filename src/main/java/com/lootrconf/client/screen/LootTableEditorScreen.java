package com.lootrconf.client.screen;

import com.lootrconf.client.widget.EntryListWidget;
import com.lootrconf.client.widget.PoolListWidget;
import com.lootrconf.data.LootEntryData;
import com.lootrconf.data.LootTableBuilder;
import com.lootrconf.data.LootTableParser;
import com.lootrconf.network.NetworkHandler;
import com.lootrconf.network.SaveLootTablePacket;
import com.lootrconf.util.LootrDetector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LootTableEditorScreen extends Screen {
    private String tableName;
    private LootEntryData.LootTable table;
    private PoolListWidget poolListWidget;
    private EntryListWidget entryListWidget;
    private EditBox nameField;
    private int selectedPool = 0;

    private static final int SIDEBAR_WIDTH = 160;
    private static final int HEADER_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 40;

    public LootTableEditorScreen(String tableName) {
        super(Component.literal("LootrConf Editor" + (tableName.isEmpty() ? "" : ": " + tableName)));
        this.tableName = tableName;
        this.table = new LootEntryData.LootTable();
        if (table.pools.isEmpty()) {
            table.pools.add(new LootEntryData.LootPool());
        }
    }

    public void loadFromJson(String json) {
        this.table = LootTableParser.parse(json);
        if (table.pools.isEmpty()) {
            table.pools.add(new LootEntryData.LootPool());
        }
        this.selectedPool = 0;
        if (poolListWidget != null) poolListWidget.refresh();
        if (entryListWidget != null) entryListWidget.refresh(getCurrentPool());
    }

    @Override
    protected void init() {
        super.init();
        int w = this.width;
        int h = this.height;

        nameField = new EditBox(this.font, 10, 5, 200, 20, Component.literal("Table Name"));
        nameField.setValue(tableName);
        nameField.setMaxLength(64);
        this.addRenderableWidget(nameField);

        poolListWidget = new PoolListWidget(this, minecraft, SIDEBAR_WIDTH, h - HEADER_HEIGHT - FOOTER_HEIGHT,
                HEADER_HEIGHT, h - FOOTER_HEIGHT, table, selectedPool);
        poolListWidget.setLeftPos(0);
        this.addWidget(poolListWidget);

        this.addRenderableWidget(Button.builder(Component.literal("+ Pool"), btn -> addPool())
                .bounds(5, h - FOOTER_HEIGHT + 5, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("- Pool"), btn -> removePool())
                .bounds(80, h - FOOTER_HEIGHT + 5, 70, 20).build());

        int entryX = SIDEBAR_WIDTH + 5;
        int entryWidth = w - entryX - 5;
        entryListWidget = new EntryListWidget(this, minecraft, entryWidth, h - HEADER_HEIGHT - FOOTER_HEIGHT,
                HEADER_HEIGHT, h - FOOTER_HEIGHT, getCurrentPool());
        entryListWidget.setLeftPos(entryX);
        this.addWidget(entryListWidget);

        this.addRenderableWidget(Button.builder(Component.literal("+ Entry"), btn -> addEntry())
                .bounds(entryX, h - FOOTER_HEIGHT + 5, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("- Entry"), btn -> removeEntry())
                .bounds(entryX + 75, h - FOOTER_HEIGHT + 5, 70, 20).build());

        int presetX = entryX + 160;
        this.addRenderableWidget(Button.builder(Component.literal("Common"), btn -> setWeight(40))
                .bounds(presetX, h - FOOTER_HEIGHT + 5, 60, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Uncommon"), btn -> setWeight(20))
                .bounds(presetX + 65, h - FOOTER_HEIGHT + 5, 65, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Rare"), btn -> setWeight(10))
                .bounds(presetX + 135, h - FOOTER_HEIGHT + 5, 50, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Epic"), btn -> setWeight(5))
                .bounds(presetX + 190, h - FOOTER_HEIGHT + 5, 50, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Legendary"), btn -> setWeight(1))
                .bounds(presetX + 245, h - FOOTER_HEIGHT + 5, 70, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Save"), btn -> save())
                .bounds(w - 80, h - FOOTER_HEIGHT + 5, 70, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.fill(0, 0, this.width, HEADER_HEIGHT, 0xAA000000);
        graphics.fill(0, this.height - FOOTER_HEIGHT, this.width, this.height, 0xAA000000);

        graphics.fill(0, HEADER_HEIGHT, SIDEBAR_WIDTH, this.height - FOOTER_HEIGHT, 0x88000020);
        graphics.drawString(this.font, "Pools:", 5, HEADER_HEIGHT + 5, 0xFFFFFF);

        graphics.fill(SIDEBAR_WIDTH, HEADER_HEIGHT, this.width, this.height - FOOTER_HEIGHT, 0x88002000);
        graphics.drawString(this.font, "Entries:", SIDEBAR_WIDTH + 5, HEADER_HEIGHT + 5, 0xFFFFFF);

        boolean lootrLoaded = LootrDetector.isLootrLoaded();
        int dotColor = lootrLoaded ? 0xFF00FF00 : 0xFFFF0000;
        String lootrStatus = lootrLoaded ? "§aLootr: ON" : "§cLootr: OFF";
        graphics.fill(this.width - 90, 8, this.width - 78, 20, dotColor);
        graphics.drawString(this.font, lootrStatus, this.width - 75, 8, 0xFFFFFF);

        poolListWidget.render(graphics, mouseX, mouseY, partialTick);
        entryListWidget.render(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private LootEntryData.LootPool getCurrentPool() {
        if (table.pools.isEmpty()) {
            table.pools.add(new LootEntryData.LootPool());
        }
        if (selectedPool >= table.pools.size()) selectedPool = 0;
        return table.pools.get(selectedPool);
    }

    public void selectPool(int index) {
        this.selectedPool = index;
        if (entryListWidget != null) entryListWidget.refresh(getCurrentPool());
        if (poolListWidget != null) poolListWidget.setSelectedPool(index);
    }

    private void addPool() {
        table.pools.add(new LootEntryData.LootPool());
        if (poolListWidget != null) poolListWidget.refresh();
    }

    private void removePool() {
        if (table.pools.size() > 1) {
            table.pools.remove(selectedPool);
            if (selectedPool >= table.pools.size()) selectedPool = table.pools.size() - 1;
            selectPool(selectedPool);
        }
    }

    private void addEntry() {
        getCurrentPool().entries.add(new LootEntryData.LootEntry());
        if (entryListWidget != null) entryListWidget.refresh(getCurrentPool());
    }

    private void removeEntry() {
        LootEntryData.LootPool pool = getCurrentPool();
        int sel = entryListWidget != null ? entryListWidget.getSelectedIndex() : -1;
        if (sel >= 0 && sel < pool.entries.size()) {
            pool.entries.remove(sel);
            entryListWidget.refresh(pool);
        }
    }

    private void setWeight(int weight) {
        LootEntryData.LootPool pool = getCurrentPool();
        int sel = entryListWidget != null ? entryListWidget.getSelectedIndex() : -1;
        if (sel >= 0 && sel < pool.entries.size()) {
            pool.entries.get(sel).weight = weight;
            entryListWidget.refresh(pool);
        }
    }

    private void save() {
        String name = nameField.getValue().trim();
        if (name.isEmpty()) {
            name = "my_loot_table";
            nameField.setValue(name);
        }
        this.tableName = name;
        String json = LootTableBuilder.build(table);
        NetworkHandler.CHANNEL.sendToServer(new SaveLootTablePacket(name, json));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
