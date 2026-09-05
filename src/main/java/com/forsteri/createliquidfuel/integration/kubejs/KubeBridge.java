package com.forsteri.createliquidfuel.integration.kubejs;

import com.forsteri.createliquidfuel.integration.kubejs.events.CLFEvents;
import com.forsteri.createliquidfuel.integration.kubejs.events.handlers.LiquidFuelBuilder;
import com.forsteri.createliquidfuel.integration.kubejs.events.handlers.LiquidFuelModification;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Map;

public final class KubeBridge {
    private KubeBridge() {
    }

    public static void postModification(KubeDataGenerator generator) {
        if (!CLFEvents.modifyFuels.hasListeners()) {
            return;
        }

        LiquidFuelModification modification = new LiquidFuelModification();
        CLFEvents.modifyFuels.post(modification);

        if (modification.getFuelsToAdd().isEmpty()
                && modification.getFuelsToModify().isEmpty()
                && modification.getFuelsToRemove().isEmpty()
        ) {
            return;
        }

        generator.dataMap(CLFDataMaps.LIQUID_FUEL, file -> {
            for (Map.Entry<Either<Fluid, TagKey<Fluid>>, LiquidFuelBuilder> entry : modification.getFuelsToAdd().entrySet()) {
                entry.getKey().ifLeft(fluid -> {
                    Fluid normalized = normalize(fluid);
                    if (isValid(normalized)) {
                        file.add(normalized, entry.getValue().build());
                    }
                }).ifRight(tag -> file.addTag(tag, entry.getValue().build()));
            }

            for (Map.Entry<Either<Fluid, TagKey<Fluid>>, LiquidFuelBuilder> entry : modification.getFuelsToModify().entrySet()) {
                entry.getKey().ifLeft(fluid -> {
                    Fluid normalized = normalize(fluid);
                    if (isValid(normalized)) {
                        file.add(normalized, entry.getValue().build(), true);
                    }
                }).ifRight(tag -> file.addTag(tag, entry.getValue().build(), true));
            }

            for (Either<Fluid, TagKey<Fluid>> key : modification.getFuelsToRemove()) {
                key.ifLeft(fluid -> {
                    Fluid normalized = normalize(fluid);
                    if (isValid(normalized)) {
                        file.remove(normalized);
                    }
                }).ifRight(file::removeTag);
            }
        });
    }

    private static boolean isValid(Fluid fluid) {
        if (fluid == Fluids.EMPTY) return false;
        BuiltInRegistries.FLUID.getKey(fluid);
        return true;
    }

    private static Fluid normalize(Fluid fluid) {
        if (fluid instanceof FlowingFluid flowing && !flowing.defaultFluidState().isSource()) {
            return flowing.getSource();
        }
        return fluid;
    }
}