package com.lootrconf.client.widget;

import com.lootrconf.data.LootEntryData;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A {@link ScrollableListWidget} specialised for displaying loot table pools.
 */
@OnlyIn(Dist.CLIENT)
public class PoolListWidget extends ScrollableListWidget {

    private final List<LootEntryData.PoolData> pools;

    public PoolListWidget(Font font, int x, int y, int width, int height,
                           List<LootEntryData.PoolData> pools,
                           BiConsumer<Integer, String> onSelect) {
        super(font, x, y, width, height, buildLabels(pools), onSelect);
        this.pools = pools;
    }

    private static List<String> buildLabels(List<LootEntryData.PoolData> pools) {
        return IntStream.range(0, pools.size())
                .mapToObj(i -> "Pool " + (i + 1) + " [" + pools.get(i).entries.size() + " entries]")
                .collect(Collectors.toList());
    }
}
