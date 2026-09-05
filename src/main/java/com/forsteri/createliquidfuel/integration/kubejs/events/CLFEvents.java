package com.forsteri.createliquidfuel.integration.kubejs.events;

import com.forsteri.createliquidfuel.integration.kubejs.events.handlers.LiquidFuelModification;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface CLFEvents {
    EventGroup GROUP = EventGroup.of("LiquidFuel");

    EventHandler modifyFuels = GROUP.server("modify", () -> LiquidFuelModification.class);
}
