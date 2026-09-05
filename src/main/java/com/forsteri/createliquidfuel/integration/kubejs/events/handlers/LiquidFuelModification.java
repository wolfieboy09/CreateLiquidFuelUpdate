package com.forsteri.createliquidfuel.integration.kubejs.events.handlers;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class LiquidFuelModification implements KubeEvent {
    private static final Map<ResourceKey<Fluid>, LiquidFuelEntry> DEFAULT_FUELS = new HashMap<>();
    private static final Map<TagKey<Fluid>, LiquidFuelEntry> DEFAULT_TAG_FUELS = new HashMap<>();

    static {
        loadShippedFuels();
    }

    private final transient Map<Either<Fluid, TagKey<Fluid>>, LiquidFuelBuilder> fuelsToAdd = new HashMap<>();
    private final transient Map<Either<Fluid, TagKey<Fluid>>, LiquidFuelBuilder> fuelsToModify = new HashMap<>();
    private final transient Set<Either<Fluid, TagKey<Fluid>>> fuelsToRemove = new HashSet<>();

    public LiquidFuelBuilder create(KubeResourceLocation id) {
        Either<Fluid, TagKey<Fluid>> key = resolve(id.wrapped());
        LiquidFuelBuilder builder = new LiquidFuelBuilder(key);
        this.fuelsToAdd.put(key, builder);
        return builder;
    }

    public LiquidFuelBuilder modify(KubeResourceLocation id) {
        Either<Fluid, TagKey<Fluid>> key = resolve(id.wrapped());
        LiquidFuelEntry current = key.map(LiquidFuelModification::currentEntry, DEFAULT_TAG_FUELS::get);
        LiquidFuelBuilder builder = current == null ? new LiquidFuelBuilder(key) : new LiquidFuelBuilder(key, current);
        this.fuelsToModify.put(key, builder);
        return builder;
    }

    public void remove(KubeResourceLocation id) {
        this.fuelsToRemove.add(resolve(id.wrapped()));
    }

    private static Either<Fluid, TagKey<Fluid>> resolve(ResourceLocation id) {
        if (BuiltInRegistries.FLUID.containsKey(id)) {
            return Either.left(BuiltInRegistries.FLUID.get(id));
        }
        return Either.right(TagKey.create(Registries.FLUID, id));
    }

    private static LiquidFuelEntry currentEntry(Fluid fluid) {
        ResourceKey<Fluid> key = BuiltInRegistries.FLUID.getResourceKey(fluid).orElse(null);
        if (key == null) return null;

        LiquidFuelEntry entry = BuiltInRegistries.FLUID.getDataMap(CLFDataMaps.LIQUID_FUEL).get(key);
        if (entry != null) return entry;
        return DEFAULT_FUELS.get(key);
    }

    private static void loadShippedFuels() {
        String path = "data/" + CreateLiquidFuel.MOD_ID + "/data_maps/fluid/" + CLFDataMaps.LIQUID_FUEL.id().getPath() + ".json";

        try (InputStream stream = LiquidFuelModification.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return;
            }

            JsonObject values = JsonParser
                    .parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject()
                    .getAsJsonObject("values");

            for (String id : values.keySet()) {
                LiquidFuelEntry entry = LiquidFuelEntry.CODEC
                        .parse(JsonOps.INSTANCE, values.get(id))
                        .getOrThrow(msg -> new IllegalStateException("Failed to decode default liquid fuel " + id + ": " + msg));

                if (id.startsWith("#")) {
                    DEFAULT_TAG_FUELS.put(TagKey.create(Registries.FLUID, ResourceLocation.parse(id.substring(1))), entry);
                } else {
                    DEFAULT_FUELS.put(ResourceKey.create(Registries.FLUID, ResourceLocation.parse(id)), entry);
                }
            }
        } catch (Exception e) {
            CreateLiquidFuel.LOGGER.warn("Could not read default liquid fuels for KubeJS modifications", e);
        }
    }

    @HideFromJS
    public Map<Either<Fluid, TagKey<Fluid>>, LiquidFuelBuilder> getFuelsToAdd() {
        return this.fuelsToAdd;
    }

    @HideFromJS
    public Map<Either<Fluid, TagKey<Fluid>>, LiquidFuelBuilder> getFuelsToModify() {
        return this.fuelsToModify;
    }

    @HideFromJS
    public Set<Either<Fluid, TagKey<Fluid>>> getFuelsToRemove() {
        return this.fuelsToRemove;
    }

    @HideFromJS
    public void clearCache() {
        this.fuelsToAdd.clear();
        this.fuelsToModify.clear();
        this.fuelsToRemove.clear();
    }
}