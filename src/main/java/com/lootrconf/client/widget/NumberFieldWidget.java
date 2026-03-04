package com.lootrconf.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class NumberFieldWidget extends EditBox {
    private final boolean floatMode;
    private final Consumer<Number> onChange;

    public NumberFieldWidget(Font font, int x, int y, int width, int height, String label, boolean floatMode, Consumer<Number> onChange) {
        super(font, x, y, width, height, Component.literal(label));
        this.floatMode = floatMode;
        this.onChange = onChange;
        this.setFilter(s -> {
            if (s.isEmpty() || s.equals("-")) return true;
            try {
                if (floatMode) Float.parseFloat(s);
                else Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.setResponder(s -> {
            try {
                if (floatMode) onChange.accept(Float.parseFloat(s));
                else onChange.accept(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {}
        });
    }

    public void setIntValue(int value) {
        this.setValue(String.valueOf(value));
    }

    public void setFloatValue(float value) {
        this.setValue(String.valueOf(value));
    }

    public int getIntValue(int defaultValue) {
        try { return Integer.parseInt(getValue()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    public float getFloatValue(float defaultValue) {
        try { return Float.parseFloat(getValue()); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
