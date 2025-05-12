package com.forsteri.createliquidfuel.mixin;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.forsteri.createliquidfuel.core.IHasStomach;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class MixinBlazeBurnerTileEntity extends SmartBlockEntity implements IHasStomach {
    @Unique public SmartFluidTank createliquidfuel$stomach;

    public MixinBlazeBurnerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    public SmartFluidTank getCapability() {
        return createliquidfuel$stomach;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        createliquidfuel$stomach = new SmartFluidTank(1000, (FluidStack contents)->{})
        {
            @Override
            public boolean isFluidValid(@NotNull FluidStack stack) {
                return BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.containsKey(stack.getFluid());
            }
        };
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo info) {
        BurnerStomachHandler.tick(this);
    }

    @Inject(method = "read", at = @At("TAIL"))
    public void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (createliquidfuel$stomach != null) {
            createliquidfuel$stomach.readFromNBT(registries, compound.getCompound("Stomach"));
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (createliquidfuel$stomach != null) {
            compound.put("Stomach", createliquidfuel$stomach.writeToNBT(registries, new CompoundTag()));
        }
    }

    @Inject(method = "tryUpdateFuel", at = @At("HEAD"), cancellable = true)
    public void tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        BurnerStomachHandler.tryUpdateFuel(this, itemStack, forceOverflow, simulate, cir);
    }
}
