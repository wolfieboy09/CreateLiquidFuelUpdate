package com.forsteri.createliquidfuel.integration.jei.category;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LiquidFuelCategory extends AbstractRecipeCategory<LiquidFuelCategory.FuelRecipe> {
    public static final RecipeType<FuelRecipe> RECIPE_TYPE =
            RecipeType.create(CreateLiquidFuel.MOD_ID, "liquid_fuel", FuelRecipe.class);

    private static final int WIDTH = 172;
    private static final int HEIGHT = 36;

    public LiquidFuelCategory(IGuiHelper guiHelper) {
        super(
                RECIPE_TYPE,
                Component.translatable("createliquidfuel.jei.category.liquid_fuel"),
                blazeBurnerIcon(guiHelper),
                WIDTH,
                HEIGHT
        );
    }

    private static IDrawable blazeBurnerIcon(IGuiHelper guiHelper) {
        ItemStack blazeBurner = new ItemStack(AllBlocks.BLAZE_BURNER);
        return guiHelper.createDrawableItemStack(blazeBurner);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FuelRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addFluidStack(recipe.fluid(), FluidType.BUCKET_VOLUME)
                .setStandardSlotBackground();
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, FuelRecipe recipe, IFocusGroup focuses) {
        LiquidFuelEntry fuel = recipe.fuel();

        int ticksPerBucket = fuel.burnTime() * 1000 / Math.max(1, fuel.amountConsumedPerTick());
        int secondsPerBucket = ticksPerBucket / 20;

        addLine(builder, 24, 4, Component.translatable(
                "createliquidfuel.jei.info.burn_time",
                Component.literal(String.format("%02d", secondsPerBucket / 60))
                        .withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE));

        addLine(builder, 24, 15, Component.translatable(
                "createliquidfuel.jei.info.consumed_per_tick",
                Component.literal(String.valueOf(fuel.amountConsumedPerTick())).withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.WHITE));

        addLine(builder, 24, 26, Component.translatable(
                "createliquidfuel.jei.info.superheats",
                Component.translatable(fuel.superHeats() ? "createliquidfuel.jei.info.yes" : "createliquidfuel.jei.info.no")
                        .withStyle(fuel.superHeats() ? ChatFormatting.BLUE : ChatFormatting.RED)).withStyle(ChatFormatting.WHITE));
    }

    private static void addLine(IRecipeExtrasBuilder builder, int x, int y, Component component) {
        builder.addText(component, 140, 12)
                .setPosition(x, y)
                .setShadow(true);
    }

    public record FuelRecipe(Fluid fluid, LiquidFuelEntry fuel) {
    }
}