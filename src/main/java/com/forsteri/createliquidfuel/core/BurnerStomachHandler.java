package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.mixin.accessors.BlazeBurnerAccessor;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

public class BurnerStomachHandler {
    public static void tick(SmartBlockEntity entity) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide
                || !(entity instanceof BlazeBurnerAccessor burnerAccessor)
                || !(entity instanceof IHasStomach hasStomach))
            return;

        SmartFluidTank stomach = hasStomach.getCapability();
        if (stomach == null)
            return;

        boolean hadBurnTime = burnerAccessor.clf$getRemainingBurnTime() > 0;
        boolean hasFuel = false;

        if (stomach.getFluid().getAmount() > 0) {
            LiquidFuelEntry fuelEntry = getFuelEntry(stomach.getFluid().getFluid());
            if (fuelEntry != null) {
                hasFuel = true;
                if (stomach.getFluid().getAmount() < fuelEntry.amountConsumedPerTick()) {
                    stomach.getFluid().setAmount(0);
                } else {
                    if (fuelEntry.superHeats())
                        burnerAccessor.clf$invokeSetBlockHeat(BlazeBurnerBlock.HeatLevel.SEETHING);
                    else
                        burnerAccessor.clf$invokeSetBlockHeat(BlazeBurnerBlock.HeatLevel.FADING);

                    int newBurnTime = Math.min(
                            burnerAccessor.clf$getRemainingBurnTime() + fuelEntry.burnTime(),
                            BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY
                    );
                    burnerAccessor.clf$setRemainingBurnTime(newBurnTime);
                    stomach.getFluid().shrink(fuelEntry.amountConsumedPerTick());
                }
            }
        }

        // Fire a final sync even on the tick where fuel/burn time drops to zero,
        // so the client's snapshot gets corrected down to exactly 0 rather than
        // being left stuck on the last positive value it received.
        boolean stateStillActive = hasFuel || burnerAccessor.clf$getRemainingBurnTime() > 0;
        if ((stateStillActive || hadBurnTime) && entity.getLevel().getGameTime() % 4 == 0)
            entity.sendData();
    }

    public static void tryUpdateFuel(@NotNull SmartBlockEntity entity, ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getLevel() == null || !(entity instanceof IHasStomach hasStomach)) return;

        SmartFluidTank stomach = hasStomach.getCapability();
        if (stomach == null) return;

        IFluidHandler handler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return;

        if (!stomach.getFluid().isEmpty() && handler.getFluidInTank(0).getFluid() != stomach.getFluid().getFluid()) return;

        if (handler.getTanks() != 1) return;
        FluidStack fluidStack = handler.getFluidInTank(0);
        if (fluidStack.isEmpty() || getFuelEntry(fluidStack.getFluid()) == null) return;

        if (stomach.getFluid().getAmount() + fluidStack.getAmount() > stomach.getCapacity()) {
            if (!forceOverflow) return;
        }

        if (!simulate) {
            if (stomach.getFluid().isEmpty())
                stomach.setFluid(fluidStack.copy());
            else
                stomach.getFluid().grow(fluidStack.getAmount());
            if (!entity.getLevel().isClientSide)
                entity.sendData();
        }

        cir.setReturnValue(true);
    }

    public static long computeTotalBurnTicks(@Nullable SmartFluidTank stomach, int remainingBurnTime) {
        long totalTicks = remainingBurnTime;
        if (stomach != null && !stomach.isEmpty()) {
            LiquidFuelEntry fuelEntry = getFuelEntry(stomach.getFluid().getFluid());
            if (fuelEntry != null)
                totalTicks += ((long) stomach.getFluid().getAmount() / Math.max(1, fuelEntry.amountConsumedPerTick()))
                        * fuelEntry.burnTime();
        }
        return totalTicks;
    }

    @Nullable
    public static LiquidFuelEntry getFuelEntry(Fluid fluid) {
        Map<ResourceKey<Fluid>, LiquidFuelEntry> dataMap = BuiltInRegistries.FLUID.getDataMap(CLFDataMaps.LIQUID_FUEL);
        ResourceKey<Fluid> key = BuiltInRegistries.FLUID.getResourceKey(fluid).orElse(null);
        LiquidFuelEntry entry = dataMap.get(key);
        Fluid still = FluidHelper.convertToStill(new FluidStack(fluid, 1).getFluid());
        if (entry == null && still != fluid)
            entry = dataMap.get(BuiltInRegistries.FLUID.getResourceKey(still).orElse(null));
        return entry;
    }
}