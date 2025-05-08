package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.util.Triplet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class LiquidBurnerFuelJsonLoader extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("createliquidfuel", "drainable_fuel_loader");

    private static final Gson GSON = new Gson();

    public static final LiquidBurnerFuelJsonLoader INSTANCE = new LiquidBurnerFuelJsonLoader();

    public LiquidBurnerFuelJsonLoader() {
        super(GSON, "compat");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> p_10793_, @NotNull ResourceManager p_10794_, @NotNull ProfilerFiller p_10795_) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : p_10793_.entrySet()) {
            JsonElement element = entry.getValue();
            if (element.isJsonObject()) {
                ResourceLocation id = entry.getKey();
                JsonObject object = element.getAsJsonObject();
                JsonElement fluidElement = object.get("fluid");

                if (fluidElement != null) {
                    try {
                        Optional<Fluid> value = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(fluidElement.getAsString()));
                        if (value.isPresent()) {
                            BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.put(value.get(),
                                    Pair.of(
                                            IDENTIFIER,
                                            Triplet.of(
                                                object.has("burnTime") ?
                                                        object.get("burnTime").getAsInt() :
                                                        object.has("superHeat") && object.get("superHeat").getAsBoolean() ?
                                                                32 : 20
                                                    // Lava Burn Time per Mb is 20, create mod codes sets that 32 * 1000 / 10 for any superheat
                                                    // This is from BlazeBurnerBlockEntity#tryUpdateFuel (Line 193)
                                                    , object.has("superHeat") && object.get("superHeat").getAsBoolean() // default not to superheat
                                                    , object.has("amountConsumedPerTick") ?
                                                            object.get("amountConsumedPerTick").getAsInt() :
                                                            object.has("superHeat") && object.get("superHeat").getAsBoolean() ?
                                                                    10 : 1 // default not to consume 10 if superHeat, 1 if not
                                            )
                                    )
                                    );
                        }
                    } catch (ResourceLocationException e) {
                        throw new RuntimeException("Fluid liquid burner fuel " + id + " has invalid fluid: " + fluidElement.getAsString());
                    }
                } else {
                    throw new RuntimeException("No fluid specified for liquid burner fuel: " + id);
                }
            }
        }
    }
}
