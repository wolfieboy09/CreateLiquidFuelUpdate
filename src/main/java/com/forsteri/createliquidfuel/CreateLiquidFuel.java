package com.forsteri.createliquidfuel;

import com.forsteri.createliquidfuel.eventhandlers.ForgeEventsHandler;
import com.forsteri.createliquidfuel.eventhandlers.ModEventHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(value = CreateLiquidFuel.MOD_ID)
public class CreateLiquidFuel {
    // Directly reference a slf4j logger
    public static final String MOD_ID = "createliquidfuel";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CreateLiquidFuel(IEventBus eventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(ForgeEventsHandler::addReloadListeners);

        IEventBus MOD_BUS = modContainer.getEventBus();
        MOD_BUS.addListener(ModEventHandler::commonSetup);
        MOD_BUS.addListener(ModEventHandler::registerCapabilities);
    }
}
