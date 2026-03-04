package com.lootrconf.client.screen;

import com.lootrconf.data.LootEntryData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Popup screen for adding / removing loot conditions on an entry or pool.
 */
@OnlyIn(Dist.CLIENT)
public class ConditionEditorScreen extends Screen {

    private static final String[] CONDITION_TYPES = {
            "minecraft:random_chance",
            "minecraft:random_chance_with_looting",
            "minecraft:inverted",
            "minecraft:killed_by_player",
            "minecraft:survives_explosion"
    };

    private final Screen parent;
    private final List<LootEntryData.ConditionData> conditions;

    private int selectedIndex = -1;
    private EditBox chanceField;
    private EditBox lootingMultField;

    public ConditionEditorScreen(Screen parent, List<LootEntryData.ConditionData> conditions) {
        super(Component.literal("Condition Editor"));
        this.parent = parent;
        this.conditions = conditions;
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2 - 150;
        int y = 30;

        // ── Type buttons ─────────────────────────────────────────────────────────
        for (String type : CONDITION_TYPES) {
            String label = type.replace("minecraft:", "");
            addRenderableWidget(Button.builder(Component.literal("+ " + label), btn -> addCondition(type))
                    .bounds(cx, y, 200, 16).build());
            y += 20;
        }

        // ── Fields for selected condition ─────────────────────────────────────────
        chanceField = new EditBox(this.font, cx + 210, 30, 80, 16, Component.literal("Chance"));
        lootingMultField = new EditBox(this.font, cx + 210, 50, 80, 16, Component.literal("Looting Mult"));
        addRenderableWidget(chanceField);
        addRenderableWidget(lootingMultField);

        // ── Remove button ─────────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("Remove Selected"), btn -> removeSelected())
                .bounds(cx, this.height - 50, 130, 18).build());

        // ── Back button ───────────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("← Back"), btn -> minecraft.setScreen(parent))
                .bounds(cx + 140, this.height - 50, 80, 18).build());

        refreshFields();
    }

    private void addCondition(String type) {
        conditions.add(new LootEntryData.ConditionData(type));
        selectedIndex = conditions.size() - 1;
        refreshFields();
    }

    private void removeSelected() {
        if (selectedIndex >= 0 && selectedIndex < conditions.size()) {
            conditions.remove(selectedIndex);
            selectedIndex = Math.min(selectedIndex, conditions.size() - 1);
            refreshFields();
        }
    }

    private void commitFields() {
        if (selectedIndex >= 0 && selectedIndex < conditions.size()) {
            LootEntryData.ConditionData cond = conditions.get(selectedIndex);
            try { cond.chance = Float.parseFloat(chanceField.getValue()); } catch (NumberFormatException ignored) {}
            try { cond.lootingMultiplier = Float.parseFloat(lootingMultField.getValue()); } catch (NumberFormatException ignored) {}
        }
    }

    private void refreshFields() {
        if (selectedIndex >= 0 && selectedIndex < conditions.size()) {
            LootEntryData.ConditionData cond = conditions.get(selectedIndex);
            chanceField.setValue(String.valueOf(cond.chance));
            lootingMultField.setValue(String.valueOf(cond.lootingMultiplier));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int cx = this.width / 2 - 150;
        int y = 130;
        graphics.drawString(this.font, "Current conditions:", cx, y, 0xAAAAAA);
        y += 12;
        for (int i = 0; i < conditions.size(); i++) {
            int color = (i == selectedIndex) ? 0xFFD700 : 0xCCCCCC;
            graphics.drawString(this.font, (i + 1) + ". " + conditions.get(i).conditionType, cx, y, color);
            y += 12;
        }
        graphics.drawString(this.font, "Chance:", cx + 210, 20, 0xAAAAAA);
        graphics.drawString(this.font, "Looting Mult:", cx + 210, 40, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        commitFields();
        int cx = this.width / 2 - 150;
        int y = 142;
        for (int i = 0; i < conditions.size(); i++) {
            if (mouseX >= cx && mouseX <= cx + 300 && mouseY >= y && mouseY < y + 12) {
                selectedIndex = i;
                refreshFields();
                return true;
            }
            y += 12;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
