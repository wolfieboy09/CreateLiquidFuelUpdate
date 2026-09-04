package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.mixin.BlazeBurnerAccessor;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

public class BurnerStomachHandler {
    public static final ResourceLocation DATA_MAP_IDENTIFIER = CreateLiquidFuel.asResource("data_map");

    public static Map<Fluid, Pair<ResourceLocation, LiquidFuelEntry>> LIQUID_BURNER_FUEL_MAP = new HashMap<>();

    public static void refreshFuelMap(Registry<Fluid> fluidRegistry) {
        LIQUID_BURNER_FUEL_MAP.clear();

        fluidRegistry.getDataMap(CLFDataMaps.LIQUID_FUEL).forEach((resourceKey, entry) -> {
            Fluid fluid = fluidRegistry.get(resourceKey);
            if (fluid != null) {
                LIQUID_BURNER_FUEL_MAP.put(fluid, Pair.of(DATA_MAP_IDENTIFIER, entry));
            }
        });
    }

    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.FLUID, BurnerStomachHandler::refreshFuelMap);
    }

    public static void tick(SmartBlockEntity entity) {
        if (entity.getLevel() == null || !(entity instanceof BlazeBurnerAccessor burnerAccessor)) return;

        SmartFluidTank stomach = (SmartFluidTank) entity.getLevel().getCapability(
            Capabilities.FluidHandler.BLOCK,
            entity.getBlockPos(),
            Direction.DOWN
        );

        if (stomach == null)
            return;

        if (stomach.getFluid().getAmount() <= 0) return;

        Pair<ResourceLocation, LiquidFuelEntry> propertyPair = LIQUID_BURNER_FUEL_MAP.get(stomach.getFluid().getFluid());
        if (propertyPair == null) return;

        LiquidFuelEntry fuelEntry = propertyPair.getSecond();
        if (fuelEntry == null) return;

        if (stomach.getFluid().getAmount() < fuelEntry.amountConsumedPerTick()) {
            stomach.getFluid().setAmount(0);
            return;
        }

        if (fuelEntry.superHeat())
            burnerAccessor.clf$invokeSetBlockHeat(BlazeBurnerBlock.HeatLevel.SEETHING);
        else
            burnerAccessor.clf$invokeSetBlockHeat(BlazeBurnerBlock.HeatLevel.FADING);

        int newBurnTime = burnerAccessor.clf$getRemainingBurnTime() + fuelEntry.burnTime();

        if (newBurnTime > BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY) {
            return;
        }

        burnerAccessor.clf$setRemainingBurnTime(newBurnTime);

        stomach.getFluid().shrink(fuelEntry.amountConsumedPerTick());
    }

    public static void tryUpdateFuel(@NotNull SmartBlockEntity entity, ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getLevel() == null) return;
        SmartFluidTank stomach = (SmartFluidTank) entity.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK,
                entity.getBlockPos(),
                Direction.DOWN
        );

        if (stomach == null) return;

        if (itemStack.getCapability(Capabilities.FluidHandler.ITEM) == null) return;

        IFluidHandler handler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);

        if (handler == null) return;

        if (!stomach.getFluid().isEmpty() && handler.getFluidInTank(0).getFluid() != stomach.getFluid().getFluid()) return;

        if (handler.getTanks() != 1) return;
        FluidStack fluidStack = handler.getFluidInTank(0);
        if (fluidStack.isEmpty()) return;
        if (!BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.containsKey(fluidStack.getFluid()))
            return;

        if (stomach.getFluid().getAmount() + fluidStack.getAmount() > stomach.getCapacity()) {
            if (!forceOverflow) return;
        }

        if (!simulate) {
            if (stomach.getFluid().isEmpty())
                stomach.setFluid(fluidStack.copy());
            else
                stomach.getFluid().grow(fluidStack.getAmount());
        }

        cir.setReturnValue(true);
    }
}
