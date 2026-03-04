package com.lootrconf.client.widget;

import com.lootrconf.data.LootEntryData;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * A {@link ScrollableListWidget} specialised for displaying loot entries within a pool.
 */
@OnlyIn(Dist.CLIENT)
public class EntryListWidget extends ScrollableListWidget {

    private final List<LootEntryData.EntryData> entries;

    public EntryListWidget(Font font, int x, int y, int width, int height,
                            List<LootEntryData.EntryData> entries,
                            BiConsumer<Integer, String> onSelect) {
        super(font, x, y, width, height, buildLabels(entries), onSelect);
        this.entries = entries;
    }

    private static List<String> buildLabels(List<LootEntryData.EntryData> entries) {
        return entries.stream()
                .map(e -> e.itemId + " (w:" + e.weight + ")")
                .collect(Collectors.toList());
    }
}
