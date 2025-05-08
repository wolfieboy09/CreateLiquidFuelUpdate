package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.mixin.BlazeBurnerAccessor;
import com.forsteri.createliquidfuel.util.Triplet;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

public class BurnerStomachHandler {
    public static Map<Fluid, Pair<ResourceLocation, Triplet<Integer, Boolean, Integer>>> LIQUID_BURNER_FUEL_MAP = new HashMap<>();

    public static void tick(SmartBlockEntity entity) {
        if (!(entity instanceof BlazeBurnerAccessor burnerAccessor)) return;

        SmartFluidTank stomach = (SmartFluidTank) entity.getLevel().getCapability(
            Capabilities.FluidHandler.BLOCK,
            entity.getBlockPos(),
            Direction.DOWN
        );

        if (stomach == null)
            return;

        if (stomach.getFluid().getAmount() <= 0) return;

        Pair<ResourceLocation, Triplet<Integer, Boolean, Integer>> propertyPair =
                LIQUID_BURNER_FUEL_MAP.get(stomach.getFluid().getFluid());
        if (propertyPair == null) return;

        Triplet<Integer, Boolean, Integer> burnerProperty = propertyPair.getSecond();
        if (burnerProperty == null) return;
        CreateLiquidFuel.LOGGER.warn("Successfully got burnerProperty");

        boolean fluidSuperHeats = burnerProperty.getSecond();

        int mbConsuming = burnerProperty.getThird();

        if (stomach.getFluid().getAmount() < mbConsuming) {
            CreateLiquidFuel.LOGGER.warn("Ran out of fuel");
            stomach.getFluid().setAmount(0);
            return;
        }

        if (fluidSuperHeats)
            burnerAccessor.createliquidfuel$invokeSetBlockHeat(BlazeBurnerBlock.HeatLevel.SEETHING);
        else
            burnerAccessor.createliquidfuel$invokeSetBlockHeat(BlazeBurnerBlock.HeatLevel.FADING);

        int newBurnTime = burnerAccessor.createliquidfuel$getRemainingBurnTime() + burnerProperty.getFirst();

        if (newBurnTime > BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY) {
            CreateLiquidFuel.LOGGER.warn("Using burn time first");
            return;
        }

        burnerAccessor.createliquidfuel$setRemainingBurnTime(newBurnTime);

        CreateLiquidFuel.LOGGER.warn("Shrinking fuel");
        stomach.getFluid().shrink(mbConsuming);
    }

    public static void tryUpdateFuel(@NotNull SmartBlockEntity entity, ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        SmartFluidTank stomach = (SmartFluidTank) entity.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK,
                entity.getBlockPos(),
                Direction.DOWN
        );

        //noinspection ConstantValue
        if (stomach == null) return;

        if (itemStack.getCapability(Capabilities.FluidHandler.ITEM) == null) return;

        @SuppressWarnings("DataFlowIssue")
        IFluidHandler handler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);

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
