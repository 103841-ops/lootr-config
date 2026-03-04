package com.lootrconf.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * An {@link EditBox} that only accepts numeric (integer or float) input.
 */
@OnlyIn(Dist.CLIENT)
public class NumberFieldWidget extends EditBox {

    private final boolean allowFloat;

    public NumberFieldWidget(Font font, int x, int y, int width, int height,
                              Component hint, boolean allowFloat) {
        super(font, x, y, width, height, hint);
        this.allowFloat = allowFloat;
        setFilter(this::isValidInput);
    }

    private boolean isValidInput(String s) {
        if (s.isEmpty() || s.equals("-")) return true;
        if (allowFloat) {
            if (s.equals(".") || s.equals("-.")) return true;
            try { Double.parseDouble(s); return true; } catch (NumberFormatException e) { return false; }
        } else {
            try { Integer.parseInt(s); return true; } catch (NumberFormatException e) { return false; }
        }
    }

    public int getIntValue(int fallback) {
        try { return Integer.parseInt(getValue()); } catch (NumberFormatException e) { return fallback; }
    }

    public float getFloatValue(float fallback) {
        try { return Float.parseFloat(getValue()); } catch (NumberFormatException e) { return fallback; }
    }
}
