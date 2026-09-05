package com.forsteri.createliquidfuel.core.datagen;

import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class LiquidFuelProvider implements DataProvider {
    private final PackOutput output;
    private final String modId;
    private final Map<ResourceLocation, Builder> entries = new LinkedHashMap<>();
    private final List<String> removals = new ArrayList<>();

    public LiquidFuelProvider(PackOutput output, String modId) {
        this.output = output;
        this.modId = modId;
    }

    protected abstract void generate();

    protected Builder add(Fluid fluid) {
        return add(BuiltInRegistries.FLUID.wrapAsHolder(fluid));
    }

    protected Builder add(FlowingFluid fluid) {
        return add(BuiltInRegistries.FLUID.wrapAsHolder(fluid.getSource()));
    }

    protected Builder add(TagKey<Fluid> tag) {
        ResourceLocation id = tag.location();

        if (entries.containsKey(id)) {
            throw new IllegalStateException("Duplicate liquid fuel entry " + id);
        }

        Builder builder = new Builder(id, true);
        entries.put(id, builder);
        return builder;
    }

    protected void remove(Fluid fluid) {
        remove(BuiltInRegistries.FLUID.getKey(fluid), false);
    }

    protected void remove(FlowingFluid fluid) {
        remove(BuiltInRegistries.FLUID.getKey(fluid.getSource()), false);
    }

    protected void remove(TagKey<Fluid> tag) {
        remove(tag.location(), true);
    }

    private void remove(ResourceLocation id, boolean tag) {
        removals.add((tag ? "#" : "") + id);
    }

    // Private to prevent having a flowing fluid get generated
    private Builder add(Holder<Fluid> fluid) {
        ResourceLocation id = fluid.unwrapKey()
                .orElseThrow(() -> new IllegalStateException("Fluid holder has no registry key: " + fluid))
                .location();

        if (entries.containsKey(id)) {
            throw new IllegalStateException("Duplicate liquid fuel entry " + id);
        }

        Builder builder = new Builder(id, false);
        entries.put(id, builder);
        return builder;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        entries.clear();
        removals.clear();
        generate();

        JsonObject values = new JsonObject();
        entries.forEach((id, builder) -> {
            LiquidFuelEntry entry = builder.build();
            values.add(builder.jsonKey(), LiquidFuelEntry.CODEC
                    .encodeStart(JsonOps.INSTANCE, entry)
                    .getOrThrow(msg -> new IllegalStateException("Failed to encode liquid fuel entry " + id + ": " + msg)));
        });

        JsonObject root = new JsonObject();
        root.add("values", values);

        if (!removals.isEmpty()) {
            JsonArray remove = new JsonArray();
            removals.forEach(remove::add);
            root.add("remove", remove);
        }

        ResourceLocation mapId = CLFDataMaps.LIQUID_FUEL.id();
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(mapId.getNamespace())
                .resolve("data_maps")
                .resolve("fluid")
                .resolve(mapId.getPath() + ".json");

        return DataProvider.saveStable(cache, root, path);
    }

    @Override
    public String getName() {
        return "Create Liquid Fuel for " + modId;
    }

    @CanIgnoreReturnValue
    public static final class Builder {
        private final ResourceLocation id;
        private final boolean tag;
        private int burnTime = 10;
        private int consumedPerTick = 1;
        private boolean superHeat = false;

        private Builder(ResourceLocation id, boolean tag) {
            this.id = id;
            this.tag = tag;
        }

        private String jsonKey() {
            return tag ? "#" + id : id.toString();
        }

        public Builder burnTime(int ticks) {
            this.burnTime = ticks;
            return this;
        }

        public Builder consumedPerTick(int consumedPerTick) {
            this.consumedPerTick = consumedPerTick;
            return this;
        }

        public Builder superHeats() {
            this.superHeat = true;
            return this;
        }

        private LiquidFuelEntry build() {
            if (burnTime < 0) {
                throw new IllegalStateException("Liquid fuel entry " + id + " is missing a burnTime");
            }
            return new LiquidFuelEntry(burnTime, consumedPerTick, superHeat);
        }
    }
}