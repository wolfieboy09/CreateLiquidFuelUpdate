package com.forsteri.createliquidfuel.eventhandlers;

//import com.forsteri.createliquidfuel.core.DrainableFuelLoader;

import com.forsteri.createliquidfuel.core.IHasStomach;
import com.simibubi.create.AllBlockEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModEventHandler {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                AllBlockEntityTypes.HEATER.get(),
                (blockEntity, side) -> ((IHasStomach)blockEntity).getCapability()
        );
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event){
        //event.enqueueWork(DrainableFuelLoader::load);
    }
}