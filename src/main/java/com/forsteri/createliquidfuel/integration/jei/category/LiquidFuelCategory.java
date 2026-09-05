package com.forsteri.createliquidfuel.integration.jei.category;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.core.LiquidFuelEntry;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LiquidFuelCategory extends AbstractRecipeCategory<LiquidFuelCategory.FuelRecipe> {
    public static final RecipeType<FuelRecipe> RECIPE_TYPE = RecipeType.create(CreateLiquidFuel.MOD_ID, "liquid_fuel", FuelRecipe.class);

    private final AnimatedBlazeBurner blazeBurner = new AnimatedBlazeBurner();

    public LiquidFuelCategory(IGuiHelper guiHelper) {
        super(
                RECIPE_TYPE,
                Component.translatable("createliquidfuel.jei.category.liquid_fuel"),
                blazeBurnerIcon(guiHelper),
                190,
                60
        );
    }

    private static IDrawable blazeBurnerIcon(IGuiHelper guiHelper) {
        ItemStack blazeBurner = new ItemStack(AllBlocks.BLAZE_BURNER);
        return guiHelper.createDrawableItemStack(blazeBurner);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FuelRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 75, 5)
                .addFluidStack(recipe.fluid(), FluidType.BUCKET_VOLUME)
                .setStandardSlotBackground();

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addFluidStack(recipe.fluid(), FluidType.BUCKET_VOLUME);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, FuelRecipe recipe, IFocusGroup focuses) {
        LiquidFuelEntry fuel = recipe.fuel();

        int ticksPerBucket = fuel.burnTime() * 1000 / Math.max(1, fuel.amountConsumedPerTick());
        int secondsPerBucket = ticksPerBucket / 20;

        builder.addText(Component.literal(String.format("%02dm", secondsPerBucket / 60)).withStyle(ChatFormatting.YELLOW), 114, 12)
                .setPosition(145, 35);
    }

    @Override
    public void draw(FuelRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_HEAT_BAR.render(graphics, 8, 30);
        AllGuiTextures.JEI_LIGHT.render(graphics, 85, 40);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 95, 5);

        HeatCondition requiredHeat = recipe.fuel().superHeats() ? HeatCondition.SUPERHEATED : HeatCondition.HEATED;

        graphics.drawString(
                Minecraft.getInstance().font,
                CreateLang.translateDirect(requiredHeat.getTranslationKey()),
                13, 35,
                requiredHeat.getColor()
        );

        blazeBurner.withHeat(requiredHeat.visualizeAsBlazeBurner()).draw(graphics, 95, 8);
    }

    public record FuelRecipe(Fluid fluid, LiquidFuelEntry fuel) {
    }
}