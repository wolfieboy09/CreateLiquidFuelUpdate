package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class CLFTags {
    private CLFTags() {}

    public static final TagKey<Fluid> LAVA = other("minecraft", "lava");

    public static final TagKey<Fluid> BIOFUEL = common("biofuel");
    public static final TagKey<Fluid> PLANT_OIL = common("plantoil");

    private static TagKey<Fluid> common(String name) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", name));
    }

    private static TagKey<Fluid> other(String namespace, String name) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, name));
    }

    private static TagKey<Fluid> tag(String name) {
        return TagKey.create(Registries.FLUID, CreateLiquidFuel.asResource(name));
    }
}