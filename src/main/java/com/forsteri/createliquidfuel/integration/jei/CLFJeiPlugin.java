package com.forsteri.createliquidfuel.integration.jei;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;

// TODO - get fuel data to show in JEI
@JeiPlugin
public class CLFJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return CreateLiquidFuel.asResource("jei_plugin");
    }
}
