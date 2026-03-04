package com.lootrconf.client.screen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lootrconf.data.LootEntryData;
import com.lootrconf.data.LootTableBuilder;
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

import java.util.ArrayList;

/**
 * Main loot table editor screen.
 *
 * Layout (approximate):
 *   Left panel (x=5..180):   pool list + Add/Remove pool buttons
 *   Center panel (x=185..450): entry list for selected pool + Add/Remove entry buttons
 *   Right panel (x=455..w-5): per-entry field editors (weight, quality, count, functions)
 *   Bottom bar: name field + Save button + Lootr status indicator
 */
@OnlyIn(Dist.CLIENT)
public class LootTableEditorScreen extends Screen {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── State ────────────────────────────────────────────────────────────────────
    private LootEntryData.LootTableData tableData;
    private String tableName;
    private int selectedPool = -1;
    private int selectedEntry = -1;

    // ── Scroll offsets ────────────────────────────────────────────────────────────
    private int poolScrollOffset = 0;
    private int entryScrollOffset = 0;

    // ── Widgets ───────────────────────────────────────────────────────────────────
    private EditBox nameField;
    private EditBox weightField;
    private EditBox qualityField;
    private EditBox countMinField;
    private EditBox countMaxField;
    private EditBox rollsMinField;
    private EditBox rollsMaxField;
    private EditBox bonusMinField;
    private EditBox bonusMaxField;
    private EditBox itemIdField;

    // ── Panel dimensions ──────────────────────────────────────────────────────────
    private static final int POOL_PANEL_W = 150;
    private static final int ENTRY_PANEL_W = 200;
    private static final int ROW_H = 20;
    private static final int PANEL_Y = 30;

    public LootTableEditorScreen(String tableName, LootEntryData.LootTableData existingTable) {
        super(Component.literal("LootrConf — Loot Table Editor"));
        this.tableName = tableName == null ? "" : tableName;
        this.tableData = existingTable != null ? existingTable : createDefaultTable();
        if (!this.tableData.pools.isEmpty()) {
            selectedPool = 0;
        }
    }

    private LootEntryData.LootTableData createDefaultTable() {
        LootEntryData.LootTableData t = new LootEntryData.LootTableData();
        t.pools.add(new LootEntryData.PoolData());
        return t;
    }

    @Override
    protected void init() {
        super.init();
        int w = this.width;
        int h = this.height;
        int bottomY = h - 30;

        // ── Name field ──────────────────────────────────────────────────────────
        nameField = new EditBox(this.font, 5, bottomY, 200, 20, Component.literal("Table Name"));
        nameField.setValue(tableName);
        nameField.setHint(Component.literal("loot table name..."));
        addRenderableWidget(nameField);

        // ── Save button ─────────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("💾 Save"), btn -> save())
                .bounds(210, bottomY, 80, 20).build());

        // ── Add Pool button ──────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("+ Pool"), btn -> addPool())
                .bounds(5, PANEL_Y, 70, 18).build());

        // ── Remove Pool button ───────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("- Pool"), btn -> removePool())
                .bounds(78, PANEL_Y, 70, 18).build());

        // ── Add Entry button ─────────────────────────────────────────────────────
        int entryPanelX = POOL_PANEL_W + 10;
        addRenderableWidget(Button.builder(Component.literal("+ Entry"), btn -> addEntry())
                .bounds(entryPanelX, PANEL_Y, 75, 18).build());

        // ── Remove Entry button ──────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("- Entry"), btn -> removeEntry())
                .bounds(entryPanelX + 78, PANEL_Y, 75, 18).build());

        // ── Pool rolls fields ────────────────────────────────────────────────────
        int fieldY = bottomY - 60;
        rollsMinField = new EditBox(this.font, entryPanelX, fieldY, 45, 16, Component.literal("Rolls Min"));
        rollsMaxField = new EditBox(this.font, entryPanelX + 50, fieldY, 45, 16, Component.literal("Rolls Max"));
        bonusMinField = new EditBox(this.font, entryPanelX + 100, fieldY, 45, 16, Component.literal("Bonus Min"));
        bonusMaxField = new EditBox(this.font, entryPanelX + 150, fieldY, 45, 16, Component.literal("Bonus Max"));
        addRenderableWidget(rollsMinField);
        addRenderableWidget(rollsMaxField);
        addRenderableWidget(bonusMinField);
        addRenderableWidget(bonusMaxField);

        // ── Entry edit fields ────────────────────────────────────────────────────
        int rightX = POOL_PANEL_W + ENTRY_PANEL_W + 20;
        int ry = PANEL_Y + 30;
        itemIdField = new EditBox(this.font, rightX, ry, 160, 16, Component.literal("Item ID"));
        ry += 22;
        weightField = new EditBox(this.font, rightX, ry, 60, 16, Component.literal("Weight"));
        qualityField = new EditBox(this.font, rightX + 70, ry, 60, 16, Component.literal("Quality"));
        ry += 22;
        countMinField = new EditBox(this.font, rightX, ry, 60, 16, Component.literal("Count Min"));
        countMaxField = new EditBox(this.font, rightX + 70, ry, 60, 16, Component.literal("Count Max"));
        addRenderableWidget(itemIdField);
        addRenderableWidget(weightField);
        addRenderableWidget(qualityField);
        addRenderableWidget(countMinField);
        addRenderableWidget(countMaxField);

        // ── Rarity preset buttons ────────────────────────────────────────────────
        int presetY = ry + 30;
        addRenderableWidget(Button.builder(Component.literal("Common"), btn -> setWeight(40))
                .bounds(rightX, presetY, 55, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Uncommon"), btn -> setWeight(20))
                .bounds(rightX + 58, presetY, 62, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Rare"), btn -> setWeight(10))
                .bounds(rightX, presetY + 20, 55, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Epic"), btn -> setWeight(5))
                .bounds(rightX + 58, presetY + 20, 55, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Legendary"), btn -> setWeight(1))
                .bounds(rightX, presetY + 40, 75, 16).build());

        // ── Function / Condition editors ─────────────────────────────────────────
        int funcY = presetY + 70;
        addRenderableWidget(Button.builder(Component.literal("Add Function"), btn -> openFunctionEditor())
                .bounds(rightX, funcY, 100, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Add Condition"), btn -> openConditionEditor())
                .bounds(rightX + 104, funcY, 110, 18).build());

        refreshPoolFields();
        refreshEntryFields();
    }

    // ── Pool operations ───────────────────────────────────────────────────────────

    private void addPool() {
        tableData.pools.add(new LootEntryData.PoolData());
        if (selectedPool < 0) selectedPool = 0;
        selectedEntry = -1;
        refreshPoolFields();
    }

    private void removePool() {
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            tableData.pools.remove(selectedPool);
            selectedPool = Math.min(selectedPool, tableData.pools.size() - 1);
            selectedEntry = -1;
            refreshPoolFields();
        }
    }

    // ── Entry operations ──────────────────────────────────────────────────────────

    private void addEntry() {
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            tableData.pools.get(selectedPool).entries.add(new LootEntryData.EntryData());
            selectedEntry = tableData.pools.get(selectedPool).entries.size() - 1;
            refreshEntryFields();
        }
    }

    private void removeEntry() {
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            var entries = tableData.pools.get(selectedPool).entries;
            if (selectedEntry >= 0 && selectedEntry < entries.size()) {
                entries.remove(selectedEntry);
                selectedEntry = Math.min(selectedEntry, entries.size() - 1);
                refreshEntryFields();
            }
        }
    }

    // ── Weight presets ────────────────────────────────────────────────────────────

    private void setWeight(int weight) {
        weightField.setValue(String.valueOf(weight));
        commitEntryFields();
    }

    // ── Field refresh ─────────────────────────────────────────────────────────────

    private void refreshPoolFields() {
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            LootEntryData.PoolData pool = tableData.pools.get(selectedPool);
            rollsMinField.setValue(String.valueOf(pool.rollsMin));
            rollsMaxField.setValue(String.valueOf(pool.rollsMax));
            bonusMinField.setValue(String.valueOf(pool.bonusRollsMin));
            bonusMaxField.setValue(String.valueOf(pool.bonusRollsMax));
        }
    }

    private void refreshEntryFields() {
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            var entries = tableData.pools.get(selectedPool).entries;
            if (selectedEntry >= 0 && selectedEntry < entries.size()) {
                LootEntryData.EntryData entry = entries.get(selectedEntry);
                itemIdField.setValue(entry.itemId);
                weightField.setValue(String.valueOf(entry.weight));
                qualityField.setValue(String.valueOf(entry.quality));
                countMinField.setValue(String.valueOf(entry.countMin));
                countMaxField.setValue(String.valueOf(entry.countMax));
                return;
            }
        }
        itemIdField.setValue("");
        weightField.setValue("");
        qualityField.setValue("");
        countMinField.setValue("");
        countMaxField.setValue("");
    }

    private void commitPoolFields() {
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            LootEntryData.PoolData pool = tableData.pools.get(selectedPool);
            pool.rollsMin = parseFloat(rollsMinField.getValue(), pool.rollsMin);
            pool.rollsMax = parseFloat(rollsMaxField.getValue(), pool.rollsMax);
            pool.bonusRollsMin = parseFloat(bonusMinField.getValue(), pool.bonusRollsMin);
            pool.bonusRollsMax = parseFloat(bonusMaxField.getValue(), pool.bonusRollsMax);
        }
    }

    private void commitEntryFields() {
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            var entries = tableData.pools.get(selectedPool).entries;
            if (selectedEntry >= 0 && selectedEntry < entries.size()) {
                LootEntryData.EntryData entry = entries.get(selectedEntry);
                entry.itemId = itemIdField.getValue();
                entry.weight = parseInt(weightField.getValue(), entry.weight);
                entry.quality = parseInt(qualityField.getValue(), entry.quality);
                entry.countMin = parseInt(countMinField.getValue(), entry.countMin);
                entry.countMax = parseInt(countMaxField.getValue(), entry.countMax);
            }
        }
    }

    // ── Sub-screens ───────────────────────────────────────────────────────────────

    private void openFunctionEditor() {
        commitEntryFields();
        if (selectedPool >= 0 && selectedEntry >= 0) {
            LootEntryData.EntryData entry = tableData.pools.get(selectedPool).entries.get(selectedEntry);
            this.minecraft.setScreen(new FunctionEditorScreen(this, entry));
        }
    }

    private void openConditionEditor() {
        commitEntryFields();
        if (selectedPool >= 0 && selectedEntry >= 0) {
            LootEntryData.EntryData entry = tableData.pools.get(selectedPool).entries.get(selectedEntry);
            this.minecraft.setScreen(new ConditionEditorScreen(this, entry.conditions));
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────────

    private void save() {
        commitPoolFields();
        commitEntryFields();
        tableName = nameField.getValue().trim();
        if (tableName.isEmpty()) {
            // flash the name field red - for now just return
            return;
        }
        String json = GSON.toJson(LootTableBuilder.build(tableData));
        NetworkHandler.CHANNEL.sendToServer(new SaveLootTablePacket(tableName, json));
    }

    // ── Rendering ─────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int h = this.height;

        // ── Panel backgrounds ────────────────────────────────────────────────────
        graphics.fill(2, PANEL_Y + 20, POOL_PANEL_W + 5, h - 35, 0x88000000);
        graphics.fill(POOL_PANEL_W + 7, PANEL_Y + 20, POOL_PANEL_W + ENTRY_PANEL_W + 12, h - 35, 0x88000000);

        // ── Panel headers ────────────────────────────────────────────────────────
        graphics.drawString(this.font, "Pools", 5, PANEL_Y + 22, 0xFFFFFF);
        graphics.drawString(this.font, "Entries", POOL_PANEL_W + 10, PANEL_Y + 22, 0xFFFFFF);

        // ── Pool list ────────────────────────────────────────────────────────────
        int poolY = PANEL_Y + 34;
        int visiblePools = (h - 70 - poolY) / ROW_H;
        for (int i = poolScrollOffset; i < tableData.pools.size() && i < poolScrollOffset + visiblePools; i++) {
            boolean sel = (i == selectedPool);
            int color = sel ? 0xFFD700 : 0xCCCCCC;
            int bg = sel ? 0x66FFAA00 : 0x33FFFFFF;
            graphics.fill(3, poolY, POOL_PANEL_W + 4, poolY + ROW_H - 2, bg);
            graphics.drawString(this.font, "Pool " + (i + 1), 7, poolY + 5, color);
            poolY += ROW_H;
        }

        // ── Entry list ────────────────────────────────────────────────────────────
        int entryPanelX = POOL_PANEL_W + 10;
        int entryY = PANEL_Y + 34;
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            var entries = tableData.pools.get(selectedPool).entries;
            int visibleEntries = (h - 70 - entryY) / ROW_H;
            for (int i = entryScrollOffset; i < entries.size() && i < entryScrollOffset + visibleEntries; i++) {
                boolean sel = (i == selectedEntry);
                int color = sel ? 0xFFD700 : 0xCCCCCC;
                int bg = sel ? 0x66FFAA00 : 0x33FFFFFF;
                graphics.fill(entryPanelX + 1, entryY, POOL_PANEL_W + ENTRY_PANEL_W + 11, entryY + ROW_H - 2, bg);
                String label = entries.get(i).itemId + " (w:" + entries.get(i).weight + ")";
                graphics.drawString(this.font, label, entryPanelX + 3, entryY + 5, color);
                entryY += ROW_H;
            }
        }

        // ── Right panel labels ─────────────────────────────────────────────────
        int rightX = POOL_PANEL_W + ENTRY_PANEL_W + 20;
        int ry = PANEL_Y + 22;
        graphics.drawString(this.font, "Item ID:", rightX, ry, 0xAAAAAA);
        ry += 22 + 16 + 4;
        graphics.drawString(this.font, "Weight / Quality:", rightX, ry, 0xAAAAAA);
        ry += 22;
        graphics.drawString(this.font, "Count Min / Max:", rightX, ry, 0xAAAAAA);

        // ── Pool fields labels ─────────────────────────────────────────────────
        int fieldY = h - 30 - 64;
        graphics.drawString(this.font, "Rolls:", entryPanelX, fieldY - 10, 0xAAAAAA);
        graphics.drawString(this.font, "Min", entryPanelX, fieldY + 4, 0x888888);
        graphics.drawString(this.font, "Max", entryPanelX + 50, fieldY + 4, 0x888888);
        graphics.drawString(this.font, "Bonus:", entryPanelX + 100, fieldY - 10, 0xAAAAAA);
        graphics.drawString(this.font, "Min", entryPanelX + 100, fieldY + 4, 0x888888);
        graphics.drawString(this.font, "Max", entryPanelX + 150, fieldY + 4, 0x888888);

        // ── Title ─────────────────────────────────────────────────────────────────
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // ── Lootr status indicator ────────────────────────────────────────────────
        boolean lootr = LootrDetector.isLootrLoaded();
        int dotColor = lootr ? 0x00FF00 : 0xFF0000;
        String lootrText = lootr ? "§aLootr: ON" : "§cLootr: OFF";
        graphics.fill(this.width - 80, 8, this.width - 68, 20, dotColor);
        graphics.drawString(this.font, lootrText, this.width - 65, 10, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Pool list click detection
        int poolListY = PANEL_Y + 34;
        int visiblePools = (this.height - 70 - poolListY) / ROW_H;
        for (int i = poolScrollOffset; i < tableData.pools.size() && i < poolScrollOffset + visiblePools; i++) {
            int rowY = poolListY + (i - poolScrollOffset) * ROW_H;
            if (mouseX >= 3 && mouseX <= POOL_PANEL_W + 4 && mouseY >= rowY && mouseY < rowY + ROW_H) {
                commitPoolFields();
                commitEntryFields();
                selectedPool = i;
                selectedEntry = -1;
                entryScrollOffset = 0;
                refreshPoolFields();
                refreshEntryFields();
                return true;
            }
        }
        // Entry list click detection
        int entryPanelX = POOL_PANEL_W + 10;
        int entryListY = PANEL_Y + 34;
        if (selectedPool >= 0 && selectedPool < tableData.pools.size()) {
            var entries = tableData.pools.get(selectedPool).entries;
            int visibleEntries = (this.height - 70 - entryListY) / ROW_H;
            for (int i = entryScrollOffset; i < entries.size() && i < entryScrollOffset + visibleEntries; i++) {
                int rowY = entryListY + (i - entryScrollOffset) * ROW_H;
                if (mouseX >= entryPanelX + 1 && mouseX <= POOL_PANEL_W + ENTRY_PANEL_W + 11
                        && mouseY >= rowY && mouseY < rowY + ROW_H) {
                    commitEntryFields();
                    selectedEntry = i;
                    refreshEntryFields();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX <= POOL_PANEL_W + 5) {
            poolScrollOffset = Math.max(0, poolScrollOffset - (int) Math.signum(delta));
        } else if (mouseX <= POOL_PANEL_W + ENTRY_PANEL_W + 12) {
            entryScrollOffset = Math.max(0, entryScrollOffset - (int) Math.signum(delta));
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private float parseFloat(String s, float fallback) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return fallback; }
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
}
