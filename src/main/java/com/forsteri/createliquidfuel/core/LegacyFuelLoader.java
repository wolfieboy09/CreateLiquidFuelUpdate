package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LegacyFuelLoader implements PreparableReloadListener {
    public static final LegacyFuelLoader INSTANCE = new LegacyFuelLoader();

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller1, Executor executor, Executor executor1) {
        return CompletableFuture.runAsync(() -> {
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                    "blaze_burner_fuel",
                    path -> path.getPath().endsWith(".json")
            );

            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation id = entry.getKey();
                if (!id.getNamespace().equals(CreateLiquidFuel.MOD_ID)) continue;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(entry.getValue().open()))) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    if (!json.has("fluid")) {
                        CreateLiquidFuel.LOGGER.warn("Legacy fuel entry {} is missing 'fluid' field, skipping", id);
                        continue;
                    }

                    ResourceLocation fluidId = ResourceLocation.parse(json.get("fluid").getAsString());
                    Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);

                    var fluidKey = BuiltInRegistries.FLUID.getResourceKey(fluid);
                    if (fluidKey.isEmpty()) continue;

                    Map<ResourceKey<Fluid>, LiquidFuelEntry> var3 = BuiltInRegistries.FLUID.getDataMap(CLFDataMaps.LIQUID_FUEL);
                    if (var3.containsKey(fluidKey.get())) continue;

                    LiquidFuelEntry fuelEntry = LiquidFuelEntry.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(msg -> new IllegalStateException("Failed to decode legacy fuel entry " + id + ": " + msg));

                    var3.put(fluidKey.get(), fuelEntry);
                    CreateLiquidFuel.LOGGER.info("Migrated legacy fuel entry {} for fluid {}", id, fluidId);
                } catch (Exception e) {
                    CreateLiquidFuel.LOGGER.error("Failed to load legacy fuel entry {}", id, e);
                }
            }
        }, executor).thenCompose(barrier::wait);
    }
}
