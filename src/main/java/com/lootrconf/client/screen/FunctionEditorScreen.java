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
public class FunctionEditorScreen extends Screen {
    private final Screen parent;
    private final LootEntryData.LootFunction function;
    private final Consumer<LootEntryData.LootFunction> onSave;

    private static final List<String> FUNCTION_TYPES = List.of(
            "minecraft:enchant_randomly",
            "minecraft:enchant_with_levels",
            "minecraft:set_name",
            "minecraft:set_nbt",
            "minecraft:set_damage",
            "minecraft:looting_enchant",
            "minecraft:set_attributes",
            "minecraft:exploration_map",
            "minecraft:set_potion",
            "minecraft:copy_name"
    );

    private EditBox levelsMinField, levelsMaxField, nameField, nbtField;
    private EditBox damageMinField, damageMaxField;
    private int selectedTypeIndex = 0;

    public FunctionEditorScreen(Screen parent, LootEntryData.LootFunction function, Consumer<LootEntryData.LootFunction> onSave) {
        super(Component.literal("Edit Function"));
        this.parent = parent;
        this.function = function;
        this.onSave = onSave;
        int idx = FUNCTION_TYPES.indexOf(function.type);
        selectedTypeIndex = idx >= 0 ? idx : 0;
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int y = 40;

        this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> {
            selectedTypeIndex = (selectedTypeIndex - 1 + FUNCTION_TYPES.size()) % FUNCTION_TYPES.size();
            function.type = FUNCTION_TYPES.get(selectedTypeIndex);
            rebuildWidgets();
        }).bounds(cx - 120, y, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> {
            selectedTypeIndex = (selectedTypeIndex + 1) % FUNCTION_TYPES.size();
            function.type = FUNCTION_TYPES.get(selectedTypeIndex);
            rebuildWidgets();
        }).bounds(cx + 100, y, 20, 20).build());

        y += 30;

        switch (function.type) {
            case "minecraft:enchant_with_levels" -> {
                levelsMinField = new EditBox(this.font, cx - 60, y, 50, 20, Component.literal("Min"));
                levelsMinField.setValue(String.valueOf(function.levelsMin));
                this.addRenderableWidget(levelsMinField);
                levelsMaxField = new EditBox(this.font, cx + 10, y, 50, 20, Component.literal("Max"));
                levelsMaxField.setValue(String.valueOf(function.levelsMax));
                this.addRenderableWidget(levelsMaxField);
                this.addRenderableWidget(Button.builder(
                        Component.literal("Treasure: " + function.treasure),
                        btn -> { function.treasure = !function.treasure; rebuildWidgets(); }
                ).bounds(cx - 40, y + 25, 80, 20).build());
            }
            case "minecraft:set_name" -> {
                nameField = new EditBox(this.font, cx - 100, y, 200, 20, Component.literal("Name"));
                nameField.setValue(function.name);
                nameField.setMaxLength(256);
                this.addRenderableWidget(nameField);
            }
            case "minecraft:set_nbt" -> {
                nbtField = new EditBox(this.font, cx - 100, y, 200, 20, Component.literal("NBT"));
                nbtField.setValue(function.nbt);
                nbtField.setMaxLength(512);
                this.addRenderableWidget(nbtField);
            }
            case "minecraft:set_damage" -> {
                damageMinField = new EditBox(this.font, cx - 60, y, 50, 20, Component.literal("Min"));
                damageMinField.setValue(String.valueOf(function.damageMin));
                this.addRenderableWidget(damageMinField);
                damageMaxField = new EditBox(this.font, cx + 10, y, 50, 20, Component.literal("Max"));
                damageMaxField.setValue(String.valueOf(function.damageMax));
                this.addRenderableWidget(damageMaxField);
            }
        }

        this.addRenderableWidget(Button.builder(Component.literal("Save"), btn -> {
            applyFields();
            onSave.accept(function);
            minecraft.setScreen(parent);
        }).bounds(cx - 55, this.height - 30, 50, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> minecraft.setScreen(parent))
                .bounds(cx + 5, this.height - 30, 50, 20).build());
    }

    private void applyFields() {
        try {
            if (levelsMinField != null) function.levelsMin = Float.parseFloat(levelsMinField.getValue());
            if (levelsMaxField != null) function.levelsMax = Float.parseFloat(levelsMaxField.getValue());
            if (nameField != null) function.name = nameField.getValue();
            if (nbtField != null) function.nbt = nbtField.getValue();
            if (damageMinField != null) function.damageMin = Float.parseFloat(damageMinField.getValue());
            if (damageMaxField != null) function.damageMax = Float.parseFloat(damageMaxField.getValue());
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        String typeName = FUNCTION_TYPES.get(selectedTypeIndex);
        graphics.drawCenteredString(this.font, "Function: " + typeName, this.width / 2, 45, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
