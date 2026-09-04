package com.forsteri.createliquidfuel.events;

import com.forsteri.createliquidfuel.core.IHasStomach;
import com.simibubi.create.AllBlockEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModEventHandler {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                AllBlockEntityTypes.HEATER.get(),
                (blockEntity, side) -> ((IHasStomach) blockEntity).getCapability()
        );
    }
}