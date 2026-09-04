package com.forsteri.createliquidfuel.mixin;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public interface BlazeBurnerAccessor {
    @Accessor("remainingBurnTime")
    int clf$getRemainingBurnTime();

    @Accessor("remainingBurnTime")
    void clf$setRemainingBurnTime(int remainingBurnTime);

    @Invoker("setBlockHeat")
    void clf$invokeSetBlockHeat(BlazeBurnerBlock.HeatLevel heat);
}
