package com.forsteri.createliquidfuel.eventhandlers;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.core.LiquidBurnerFuelJsonLoader;

import net.neoforged.neoforge.event.AddReloadListenerEvent;

public class ForgeEventsHandler {
    public static void addReloadListeners(AddReloadListenerEvent event) {
        CreateLiquidFuel.LOGGER.warn("addReloadListeners called");
        event.addListener(LiquidBurnerFuelJsonLoader.INSTANCE);
    }
}