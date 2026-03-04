package com.lootrconf;

import com.lootrconf.command.LootrConfCommands;
import com.lootrconf.config.LootrConfConfig;
import com.lootrconf.handler.ChestRandomizerHandler;
import com.lootrconf.network.NetworkHandler;
import com.lootrconf.util.LootrDetector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LootrConf.MOD_ID)
public class LootrConf {
    public static final String MOD_ID = "lootrconf";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public LootrConf() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LootrConfConfig.SPEC, "lootrconf-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ChestRandomizerHandler.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
        LootrDetector.detect();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("LootrConf client setup complete.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LootrConfCommands.register(event.getDispatcher());
    }
}
