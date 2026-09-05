package com.forsteri.createliquidfuel.registry;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class CLFDataMaps {
    public static final DataMapType<Fluid, LiquidFuelEntry> LIQUID_FUEL = DataMapType.builder(
            CreateLiquidFuel.asResource("blaze_burner_fuel"),
            Registries.FLUID,
            LiquidFuelEntry.CODEC
    ).synced(
            LiquidFuelEntry.CODEC,
            true
    ).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(LIQUID_FUEL);
    }
}
