package com.forsteri.createliquidfuel;

import com.forsteri.createliquidfuel.core.LegacyFuelLoader;
import com.forsteri.createliquidfuel.events.ModEventHandler;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = CreateLiquidFuel.MOD_ID)
public class CreateLiquidFuel {
    public static final String MOD_ID = "createliquidfuel";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CreateLiquidFuel(IEventBus eventBus, ModContainer modContainer) {
        eventBus.addListener(ModEventHandler::registerCapabilities);
        eventBus.addListener(CLFDataMaps::register);

        NeoForge.EVENT_BUS.addListener(CreateLiquidFuel::addReloadListeners);
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(LegacyFuelLoader.INSTANCE);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
