package com.forsteri.createliquidfuel.integration.kubejs;

import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import com.forsteri.createliquidfuel.integration.kubejs.events.CLFEvents;
import com.forsteri.createliquidfuel.integration.kubejs.events.handlers.LiquidFuelBuilder;
import com.forsteri.createliquidfuel.integration.kubejs.events.handlers.LiquidFuelModification;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public class CLFKubePlugin implements KubeJSPlugin {
    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow(LiquidFuelEntry.class);
        filter.allow(CLFDataMaps.class);
        filter.allow(LiquidFuelBuilder.class);
        filter.allow(LiquidFuelModification.class);

        filter.deny("com.forsteri.createliquidfuel");
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(CLFEvents.GROUP);
    }

    @Override
    public void generateData(KubeDataGenerator generator) {
        KubeBridge.postModification(generator);
    }
}