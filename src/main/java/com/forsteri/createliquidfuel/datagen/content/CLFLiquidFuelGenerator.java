package com.forsteri.createliquidfuel.datagen.content;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.core.datagen.LiquidFuelProvider;
import com.mrh0.createaddition.index.CAFluids;
import net.dakotapride.garnished.registry.GarnishedFluids;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.material.Fluids;

public class CLFLiquidFuelGenerator extends LiquidFuelProvider {
    public CLFLiquidFuelGenerator(PackOutput output) {
        super(output, CreateLiquidFuel.MOD_ID);
    }

    @Override
    protected void generate() {
        add(Fluids.LAVA)
                .burnTime(20);

        add(CAFluids.BIOETHANOL.get())
                .burnTime(24)
                .superHeats();

        add(CAFluids.SEED_OIL.get())
                .burnTime(10)
                .consumedPerTick(2);

        add(GarnishedFluids.PEANUT_OIL.get())
                .burnTime(10)
                .superHeats()
                .consumedPerTick(2);
    }
}
