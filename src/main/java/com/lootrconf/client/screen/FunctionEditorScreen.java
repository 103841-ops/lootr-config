package com.lootrconf.client.screen;

import com.lootrconf.data.LootEntryData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Popup screen for adding / editing loot functions on a selected entry.
 */
@OnlyIn(Dist.CLIENT)
public class FunctionEditorScreen extends Screen {

    private static final String[] FUNCTION_TYPES = {
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
    };

    private final Screen parent;
    private final LootEntryData.EntryData entry;

    private int selectedIndex = -1;
    private EditBox field1;
    private EditBox field2;
    private EditBox field3;
    private EditBox field4;

    public FunctionEditorScreen(Screen parent, LootEntryData.EntryData entry) {
        super(Component.literal("Function Editor"));
        this.parent = parent;
        this.entry = entry;
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2 - 200;
        int y = 30;

        for (String type : FUNCTION_TYPES) {
            String label = type.replace("minecraft:", "");
            addRenderableWidget(Button.builder(Component.literal("+ " + label), btn -> addFunction(type))
                    .bounds(cx, y, 190, 16).build());
            y += 20;
        }

        // ── Detail fields for selected function ───────────────────────────────────
        int fx = cx + 200;
        field1 = new EditBox(this.font, fx, 30, 150, 16, Component.literal("Field 1"));
        field2 = new EditBox(this.font, fx, 50, 150, 16, Component.literal("Field 2"));
        field3 = new EditBox(this.font, fx, 70, 150, 16, Component.literal("Field 3"));
        field4 = new EditBox(this.font, fx, 90, 150, 16, Component.literal("Field 4"));
        addRenderableWidget(field1);
        addRenderableWidget(field2);
        addRenderableWidget(field3);
        addRenderableWidget(field4);

        addRenderableWidget(Button.builder(Component.literal("Remove Selected"), btn -> removeSelected())
                .bounds(cx, this.height - 50, 130, 18).build());
        addRenderableWidget(Button.builder(Component.literal("← Back"), btn -> {
            commitFields();
            minecraft.setScreen(parent);
        }).bounds(cx + 140, this.height - 50, 80, 18).build());
    }

    private void addFunction(String type) {
        entry.functions.add(new LootEntryData.FunctionData(type));
        selectedIndex = entry.functions.size() - 1;
        refreshFields();
    }

    private void removeSelected() {
        if (selectedIndex >= 0 && selectedIndex < entry.functions.size()) {
            entry.functions.remove(selectedIndex);
            selectedIndex = Math.min(selectedIndex, entry.functions.size() - 1);
            refreshFields();
        }
    }

    private void commitFields() {
        if (selectedIndex >= 0 && selectedIndex < entry.functions.size()) {
            LootEntryData.FunctionData fn = entry.functions.get(selectedIndex);
            switch (fn.functionType) {
                case "minecraft:enchant_with_levels" -> {
                    try { fn.levelsMin = Float.parseFloat(field1.getValue()); } catch (NumberFormatException ignored) {}
                    try { fn.levelsMax = Float.parseFloat(field2.getValue()); } catch (NumberFormatException ignored) {}
                    fn.treasure = "true".equalsIgnoreCase(field3.getValue());
                }
                case "minecraft:set_name" -> fn.name = field1.getValue();
                case "minecraft:set_nbt" -> fn.nbt = field1.getValue();
                case "minecraft:set_damage" -> {
                    try { fn.damageMin = Float.parseFloat(field1.getValue()); } catch (NumberFormatException ignored) {}
                    try { fn.damageMax = Float.parseFloat(field2.getValue()); } catch (NumberFormatException ignored) {}
                }
                case "minecraft:looting_enchant" -> {
                    try { fn.countMin = Float.parseFloat(field1.getValue()); } catch (NumberFormatException ignored) {}
                    try { fn.countMax = Float.parseFloat(field2.getValue()); } catch (NumberFormatException ignored) {}
                }
                case "minecraft:set_potion" -> fn.potionId = field1.getValue();
                case "minecraft:copy_name" -> fn.copyNameSource = field1.getValue();
                case "minecraft:exploration_map" -> {
                    fn.destination = field1.getValue();
                    fn.decoration = field2.getValue();
                    try { fn.zoom = Integer.parseInt(field3.getValue()); } catch (NumberFormatException ignored) {}
                    try { fn.searchRadius = Integer.parseInt(field4.getValue()); } catch (NumberFormatException ignored) {}
                }
                case "minecraft:set_attributes" -> {
                    fn.attributeName = field1.getValue();
                    fn.attributeOperation = field2.getValue();
                    try { fn.attributeAmountMin = Float.parseFloat(field3.getValue()); } catch (NumberFormatException ignored) {}
                    try { fn.attributeAmountMax = Float.parseFloat(field4.getValue()); } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private void refreshFields() {
        field1.setValue("");
        field2.setValue("");
        field3.setValue("");
        field4.setValue("");
        if (selectedIndex >= 0 && selectedIndex < entry.functions.size()) {
            LootEntryData.FunctionData fn = entry.functions.get(selectedIndex);
            switch (fn.functionType) {
                case "minecraft:enchant_with_levels" -> {
                    field1.setValue(String.valueOf(fn.levelsMin));
                    field2.setValue(String.valueOf(fn.levelsMax));
                    field3.setValue(String.valueOf(fn.treasure));
                }
                case "minecraft:set_name" -> field1.setValue(fn.name);
                case "minecraft:set_nbt" -> field1.setValue(fn.nbt);
                case "minecraft:set_damage" -> {
                    field1.setValue(String.valueOf(fn.damageMin));
                    field2.setValue(String.valueOf(fn.damageMax));
                }
                case "minecraft:looting_enchant" -> {
                    field1.setValue(String.valueOf(fn.countMin));
                    field2.setValue(String.valueOf(fn.countMax));
                }
                case "minecraft:set_potion" -> field1.setValue(fn.potionId);
                case "minecraft:copy_name" -> field1.setValue(fn.copyNameSource);
                case "minecraft:exploration_map" -> {
                    field1.setValue(fn.destination);
                    field2.setValue(fn.decoration);
                    field3.setValue(String.valueOf(fn.zoom));
                    field4.setValue(String.valueOf(fn.searchRadius));
                }
                case "minecraft:set_attributes" -> {
                    field1.setValue(fn.attributeName);
                    field2.setValue(fn.attributeOperation);
                    field3.setValue(String.valueOf(fn.attributeAmountMin));
                    field4.setValue(String.valueOf(fn.attributeAmountMax));
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int cx = this.width / 2 - 200;
        int y = 250;
        graphics.drawString(this.font, "Current functions:", cx, y, 0xAAAAAA);
        y += 12;
        for (int i = 0; i < entry.functions.size(); i++) {
            int color = (i == selectedIndex) ? 0xFFD700 : 0xCCCCCC;
            graphics.drawString(this.font, (i + 1) + ". " + entry.functions.get(i).functionType, cx, y, color);
            y += 12;
        }

        // Field labels for selected function
        if (selectedIndex >= 0 && selectedIndex < entry.functions.size()) {
            int fx = this.width / 2;
            String type = entry.functions.get(selectedIndex).functionType;
            switch (type) {
                case "minecraft:enchant_with_levels" -> {
                    graphics.drawString(this.font, "Levels Min:", fx, 20, 0xAAAAAA);
                    graphics.drawString(this.font, "Levels Max:", fx, 40, 0xAAAAAA);
                    graphics.drawString(this.font, "Treasure (true/false):", fx, 60, 0xAAAAAA);
                }
                case "minecraft:set_name" -> graphics.drawString(this.font, "Name text:", fx, 20, 0xAAAAAA);
                case "minecraft:set_nbt" -> graphics.drawString(this.font, "NBT tag:", fx, 20, 0xAAAAAA);
                case "minecraft:set_damage" -> {
                    graphics.drawString(this.font, "Damage Min:", fx, 20, 0xAAAAAA);
                    graphics.drawString(this.font, "Damage Max:", fx, 40, 0xAAAAAA);
                }
                case "minecraft:looting_enchant" -> {
                    graphics.drawString(this.font, "Count Min:", fx, 20, 0xAAAAAA);
                    graphics.drawString(this.font, "Count Max:", fx, 40, 0xAAAAAA);
                }
                case "minecraft:set_potion" -> graphics.drawString(this.font, "Potion ID:", fx, 20, 0xAAAAAA);
                case "minecraft:copy_name" -> graphics.drawString(this.font, "Source:", fx, 20, 0xAAAAAA);
                case "minecraft:exploration_map" -> {
                    graphics.drawString(this.font, "Destination:", fx, 20, 0xAAAAAA);
                    graphics.drawString(this.font, "Decoration:", fx, 40, 0xAAAAAA);
                    graphics.drawString(this.font, "Zoom:", fx, 60, 0xAAAAAA);
                    graphics.drawString(this.font, "Search Radius:", fx, 80, 0xAAAAAA);
                }
                case "minecraft:set_attributes" -> {
                    graphics.drawString(this.font, "Attribute:", fx, 20, 0xAAAAAA);
                    graphics.drawString(this.font, "Operation:", fx, 40, 0xAAAAAA);
                    graphics.drawString(this.font, "Amount Min:", fx, 60, 0xAAAAAA);
                    graphics.drawString(this.font, "Amount Max:", fx, 80, 0xAAAAAA);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        commitFields();
        int cx = this.width / 2 - 200;
        int y = 262;
        for (int i = 0; i < entry.functions.size(); i++) {
            if (mouseX >= cx && mouseX <= cx + 350 && mouseY >= y && mouseY < y + 12) {
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
