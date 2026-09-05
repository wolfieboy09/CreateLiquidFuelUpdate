package com.forsteri.createliquidfuel.integration.kubejs.events.handlers;

import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class LiquidFuelBuilder {
    private transient final Either<Fluid, TagKey<Fluid>> key;
    private transient int burnTime = 10;
    private transient int consumptionPerTick = 1;
    private transient boolean superHeats = false;

    @HideFromJS
    public LiquidFuelBuilder(Either<Fluid, TagKey<Fluid>> key) {
        this.key = key;
    }

    @HideFromJS
    public LiquidFuelBuilder(Either<Fluid, TagKey<Fluid>> key, LiquidFuelEntry entry) {
        this.key = key;
        this.burnTime = entry.burnTime();
        this.consumptionPerTick = entry.amountConsumedPerTick();
        this.superHeats = entry.superHeats();
    }

    @HideFromJS
    public Either<Fluid, TagKey<Fluid>> getKey() {
        return this.key;
    }

    @Info("How many ticks of burn time each consumed mB of this fluid provides")
    @ReturnsSelf
    public LiquidFuelBuilder burnTime(int burnTime) {
        this.burnTime = burnTime;
        return this;
    }

    @Info("How many mB of this fluid the blaze burner consumes each tick")
    @ReturnsSelf
    public LiquidFuelBuilder consumedPerTick(int consumptionPerTick) {
        this.consumptionPerTick = consumptionPerTick;
        return this;
    }

    @Info("Whether this fluid super heats the blaze burner")
    @ReturnsSelf
    public LiquidFuelBuilder superHeats(boolean superHeats) {
        this.superHeats = superHeats;
        return this;
    }

    @Info("Make this fluid super heat the blaze burner")
    @ReturnsSelf
    public LiquidFuelBuilder superHeats() {
        return this.superHeats(true);
    }

    @HideFromJS
    public LiquidFuelEntry build() {
        return new LiquidFuelEntry(this.burnTime, this.consumptionPerTick, this.superHeats);
    }
}