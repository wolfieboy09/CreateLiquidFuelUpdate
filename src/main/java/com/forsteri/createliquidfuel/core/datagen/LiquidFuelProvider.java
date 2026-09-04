package com.forsteri.createliquidfuel.core.datagen;

import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class LiquidFuelProvider implements DataProvider {
    private final PackOutput output;
    private final String modId;
    private final Map<ResourceLocation, Builder> entries = new LinkedHashMap<>();

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

    // Private to prevent having a flowing fluid get generated
    private Builder add(Holder<Fluid> fluid) {
        ResourceLocation id = fluid.unwrapKey()
                .orElseThrow(() -> new IllegalStateException("Fluid holder has no registry key: " + fluid))
                .location();

        if (entries.containsKey(id)) {
            throw new IllegalStateException("Duplicate liquid fuel entry " + id);
        }

        Builder builder = new Builder(id, fluid);
        entries.put(id, builder);
        return builder;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        entries.clear();
        generate();

        Path root = output.getOutputFolder(PackOutput.Target.DATA_PACK);

        CompletableFuture<?>[] futures = entries.entrySet().stream()
                .map(e -> {
                    ResourceLocation id = e.getKey();
                    LiquidFuelEntry entry = e.getValue().build();

                    Path path = root.resolve(id.getNamespace())
                            .resolve("blaze_burner_fuel")
                            .resolve(id.getPath() + ".json");

                    return DataProvider.saveStable(
                            cache,
                            LiquidFuelEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry)
                                    .getOrThrow(msg -> new IllegalStateException(
                                            "Failed to encode liquid fuel entry " + id + ": " + msg)),
                            path
                    );
                })
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Create Liquid Fuel for " + modId;
    }

    @CanIgnoreReturnValue
    public static final class Builder {
        private final ResourceLocation id;
        private final Holder<Fluid> fluid;
        private int burnTime = -1;
        private int amountConsumedPerTick = 1;
        private boolean superHeat = false;

        private Builder(ResourceLocation id, Holder<Fluid> fluid) {
            this.id = id;
            this.fluid = fluid;
        }

        public Builder burnTime(int burnTime) {
            this.burnTime = burnTime;
            return this;
        }

        public Builder consumedPerTick(int amountConsumedPerTick) {
            this.amountConsumedPerTick = amountConsumedPerTick;
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
            return new LiquidFuelEntry(fluid, burnTime, amountConsumedPerTick, superHeat);
        }
    }
}