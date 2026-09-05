package com.forsteri.createliquidfuel.mixin;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.forsteri.createliquidfuel.core.IHasStomach;
import com.forsteri.createliquidfuel.mixin.accessors.BlazeBurnerAccessor;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
public abstract class BlazeBurnerBlockEntityMixin extends SmartBlockEntity implements IHasStomach, IHaveGoggleInformation {
    @Unique
    public SmartFluidTank clf$stomach;

    @Unique
    private long clf$syncedTotalTicks;
    @Unique
    private long clf$syncedGameTime;

    public BlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public SmartFluidTank getCapability() {
        return clf$stomach;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (clf$stomach == null) return false;

        long totalTicks = clf$syncedTotalTicks;
        if (getLevel() != null) {
            long elapsed = getLevel().getGameTime() - clf$syncedGameTime;
            totalTicks = Math.max(0, clf$syncedTotalTicks - elapsed);
        }

        FluidStack stomachFluid = clf$stomach.getFluid();
        if (totalTicks <= 0 && stomachFluid.isEmpty()) return false;

        CreateLang.translate("gui.goggles.liquid_fuel")
                .forGoggles(tooltip);

        if (!stomachFluid.isEmpty()) {
            LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");

            CreateLang.fluidName(stomachFluid)
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);

            CreateLang.builder()
                    .add(CreateLang.number(stomachFluid.getAmount())
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(clf$stomach.getCapacity())
                            .add(mb)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);
        }

        if (totalTicks > 0) {
            int totalSeconds = (int) (totalTicks / 20);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            CreateLang.translate("gui.goggles.liquid_fuel.burn_time_left")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);

            CreateLang.text(String.format("%d:%02d", minutes, seconds))
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip, 2);
        }

        return true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        clf$stomach = new SmartFluidTank(1000, (s) -> sendData()) {
            @Override
            public boolean isFluidValid(@NotNull FluidStack stack) {
                return BurnerStomachHandler.getFuelEntry(stack.getFluid()) != null;
            }
        };
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo info) {
        BurnerStomachHandler.tick(this);
    }

    @Inject(method = "read", at = @At("TAIL"))
    public void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (clf$stomach != null) {
            clf$stomach.readFromNBT(registries, compound.getCompound("Stomach"));
        }

        int remaining = ((BlazeBurnerAccessor) this).clf$getRemainingBurnTime();
        clf$syncedTotalTicks = BurnerStomachHandler.computeTotalBurnTicks(clf$stomach, remaining);
        clf$syncedGameTime = getLevel() != null ? getLevel().getGameTime() : 0;
    }

    @Inject(method = "write", at = @At("TAIL"))
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (clf$stomach != null) {
            compound.put("Stomach", clf$stomach.writeToNBT(registries, new CompoundTag()));
        }
    }

    @Inject(method = "tryUpdateFuel", at = @At("HEAD"), cancellable = true)
    public void tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        BurnerStomachHandler.tryUpdateFuel(this, itemStack, forceOverflow, simulate, cir);
    }
}