package com.lootrconf.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ScrollableListWidget<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    public ScrollableListWidget(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
    }

    public void setLeftPos(int left) {
        this.x0 = left;
        this.x1 = left + this.width;
    }
}
