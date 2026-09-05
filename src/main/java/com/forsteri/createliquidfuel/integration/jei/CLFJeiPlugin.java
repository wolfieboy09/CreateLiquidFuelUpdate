package com.forsteri.createliquidfuel.integration.jei;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import com.forsteri.createliquidfuel.integration.jei.category.LiquidFuelCategory;
import com.forsteri.createliquidfuel.registry.CLFDataMaps;
import com.simibubi.create.foundation.fluid.FluidHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
@JeiPlugin
public class CLFJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return CreateLiquidFuel.asResource("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new LiquidFuelCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<LiquidFuelCategory.FuelRecipe> recipes = new ArrayList<>();
        Set<Fluid> handled = new HashSet<>();

        BuiltInRegistries.FLUID.getDataMap(CLFDataMaps.LIQUID_FUEL)
                .keySet()
                .forEach(key -> addFuelRecipe(recipes, handled, key));

        registration.addRecipes(LiquidFuelCategory.RECIPE_TYPE, recipes);
    }

    private void addFuelRecipe(List<LiquidFuelCategory.FuelRecipe> recipes, Set<Fluid> handled, ResourceKey<Fluid> key) {
        Fluid fluid = BuiltInRegistries.FLUID.get(key);
        if (fluid == null) return;
        Fluid still = FluidHelper.convertToStill(new FluidStack(fluid, 1).getFluid());

        if (!handled.add(still))
            return;

        LiquidFuelEntry fuel = BurnerStomachHandler.getFuelEntry(still);
        if (fuel == null)
            return;

        recipes.add(new LiquidFuelCategory.FuelRecipe(still, fuel));
    }
}