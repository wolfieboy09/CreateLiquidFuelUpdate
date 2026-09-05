package com.forsteri.createliquidfuel.datagen.content;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.core.CLFTags;
import com.forsteri.createliquidfuel.core.datagen.LiquidFuelProvider;
import net.dakotapride.garnished.registry.GarnishedFluids;
import net.minecraft.data.PackOutput;

public class CLFLiquidFuelGenerator extends LiquidFuelProvider {
    public CLFLiquidFuelGenerator(PackOutput output) {
        super(output, CreateLiquidFuel.MOD_ID);
    }

    @Override
    protected void generate() {
        add(CLFTags.LAVA)
                .burnTime(20);

        add(CLFTags.BIOFUEL)
                .burnTime(24)
                .superHeats();

        add(CLFTags.PLANT_OIL)
                .burnTime(10)
                .consumedPerTick(2);

        add(GarnishedFluids.PEANUT_OIL.get())
                .burnTime(10)
                .superHeats()
                .consumedPerTick(2);
    }
}
