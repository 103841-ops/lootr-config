package com.lootrconf.util;

import com.lootrconf.LootrConf;
import net.minecraftforge.fml.ModList;

public class LootrDetector {

    private static boolean lootrLoaded = false;

    public static void detect() {
        lootrLoaded = ModList.get().isLoaded("lootr");
        if (lootrLoaded) {
            LootrConf.LOGGER.info("LootrConf: Lootr detected! Loot tables will be instanced per player.");
        } else {
            LootrConf.LOGGER.info("LootrConf: Lootr not detected. Loot tables will work as standard Minecraft loot tables.");
        }
    }

    public static boolean isLootrLoaded() {
        return lootrLoaded;
    }
}
