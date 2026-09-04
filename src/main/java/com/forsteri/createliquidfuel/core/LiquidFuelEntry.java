package com.forsteri.createliquidfuel.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public record LiquidFuelEntry(
        Holder<Fluid> fluid,
        int burnTime,
        int amountConsumedPerTick,
        boolean superHeat
) {
    public static final Codec<LiquidFuelEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("fluid").forGetter(LiquidFuelEntry::fluid),
            Codec.INT.fieldOf("burnTime").forGetter(LiquidFuelEntry::burnTime),
            Codec.INT.optionalFieldOf("amountConsumedPerTick", 1).forGetter(LiquidFuelEntry::amountConsumedPerTick),
            Codec.BOOL.optionalFieldOf("superHeat", false).forGetter(LiquidFuelEntry::superHeat)
    ).apply(inst, LiquidFuelEntry::new));
}