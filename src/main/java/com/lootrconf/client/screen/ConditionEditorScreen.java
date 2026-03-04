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
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ConditionEditorScreen extends Screen {
    private final Screen parent;
    private final LootEntryData.LootCondition condition;
    private final Consumer<LootEntryData.LootCondition> onSave;

    private static final List<String> CONDITION_TYPES = List.of(
            "minecraft:random_chance",
            "minecraft:random_chance_with_looting",
            "minecraft:inverted",
            "minecraft:killed_by_player",
            "minecraft:survives_explosion"
    );

    private int selectedTypeIndex = 0;
    private EditBox chanceField, lootingMultiplierField;

    public ConditionEditorScreen(Screen parent, LootEntryData.LootCondition condition, Consumer<LootEntryData.LootCondition> onSave) {
        super(Component.literal("Edit Condition"));
        this.parent = parent;
        this.condition = condition;
        this.onSave = onSave;
        int idx = CONDITION_TYPES.indexOf(condition.type);
        selectedTypeIndex = idx >= 0 ? idx : 0;
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int y = 40;

        this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> {
            selectedTypeIndex = (selectedTypeIndex - 1 + CONDITION_TYPES.size()) % CONDITION_TYPES.size();
            condition.type = CONDITION_TYPES.get(selectedTypeIndex);
            rebuildWidgets();
        }).bounds(cx - 120, y, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> {
            selectedTypeIndex = (selectedTypeIndex + 1) % CONDITION_TYPES.size();
            condition.type = CONDITION_TYPES.get(selectedTypeIndex);
            rebuildWidgets();
        }).bounds(cx + 100, y, 20, 20).build());

        y += 30;

        switch (condition.type) {
            case "minecraft:random_chance" -> {
                chanceField = new EditBox(this.font, cx - 40, y, 80, 20, Component.literal("Chance"));
                chanceField.setValue(String.valueOf(condition.chance));
                this.addRenderableWidget(chanceField);
            }
            case "minecraft:random_chance_with_looting" -> {
                chanceField = new EditBox(this.font, cx - 100, y, 80, 20, Component.literal("Chance"));
                chanceField.setValue(String.valueOf(condition.chance));
                this.addRenderableWidget(chanceField);
                lootingMultiplierField = new EditBox(this.font, cx + 10, y, 90, 20, Component.literal("Looting Mult."));
                lootingMultiplierField.setValue(String.valueOf(condition.lootingMultiplier));
                this.addRenderableWidget(lootingMultiplierField);
            }
        }

        this.addRenderableWidget(Button.builder(Component.literal("Save"), btn -> {
            applyFields();
            onSave.accept(condition);
            minecraft.setScreen(parent);
        }).bounds(cx - 55, this.height - 30, 50, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> minecraft.setScreen(parent))
                .bounds(cx + 5, this.height - 30, 50, 20).build());
    }

    private void applyFields() {
        try {
            if (chanceField != null) condition.chance = Float.parseFloat(chanceField.getValue());
            if (lootingMultiplierField != null) condition.lootingMultiplier = Float.parseFloat(lootingMultiplierField.getValue());
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        String typeName = CONDITION_TYPES.get(selectedTypeIndex);
        graphics.drawCenteredString(this.font, "Condition: " + typeName, this.width / 2, 45, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
