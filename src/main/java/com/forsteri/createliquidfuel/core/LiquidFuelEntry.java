package com.forsteri.createliquidfuel.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public record LiquidFuelEntry(
        int burnTime,
        int amountConsumedPerTick,
        boolean superHeats
) {
    public static final Codec<LiquidFuelEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("burnTime").forGetter(LiquidFuelEntry::burnTime),
            Codec.INT.optionalFieldOf("amountConsumedPerTick", 1).forGetter(LiquidFuelEntry::amountConsumedPerTick),
            Codec.BOOL.optionalFieldOf("superHeat", false).forGetter(LiquidFuelEntry::superHeats)
    ).apply(inst, LiquidFuelEntry::new));

    @Override
    public @NotNull String toString() {
        return "LiquidFuelBurner[" +
                ", burnTime=" + burnTime +
                ", amountConsumedPerTick=" + amountConsumedPerTick +
                ", superHeats=" + superHeats;
    }
}