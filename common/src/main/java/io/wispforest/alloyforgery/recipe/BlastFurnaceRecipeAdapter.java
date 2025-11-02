package io.wispforest.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.owo.serialization.RegistriesAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.forges.ForgeTier;
import io.wispforest.alloyforgery.mixin.SingleStackRecipeAccessor;
import io.wispforest.alloyforgery.utils.RecipeInjector;
import java.util.*;

/**
 * Adapter class that takes advantage of {@link RecipeInjector}
 * to adapt {@link BlastingRecipe} to {@link AlloyForgeRecipe}
 */
public class BlastFurnaceRecipeAdapter implements RecipeInjector.AddRecipes {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.LENIENT).create();

    private static final TagKey<Item> DUSTS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "dusts"));

    /**
     * Recipe tag for all {@link RecipeType#BLASTING} recipes to be disallowed for adaption
     */
    public static final Identifier BLACKLISTED_BLASTING_RECIPES = AlloyForgery.id("blacklisted_blasting_recipes");

    /**
     * Recipe tag for all {@link RecipeType#BLASTING} recipes to be disallowed for output increase at higher tiers
     */
    public static final Identifier BLACKLISTED_INCREASED_OUTPUT = AlloyForgery.id("blacklisted_increased_blasting_outputs");

    @Override
    public void addRecipes(RecipeInjector instance) {
        if (!AlloyForgery.CONFIG.allowBlastingFurnaceAdaption()) return;

        var manager = instance.manager();

        Collection<RecipeEntry<AlloyForgeRecipe>> alloyForgeryRecipes = instance.getAllOfType(AlloyForgeRecipe.Type.INSTANCE);

        for (RecipeEntry<BlastingRecipe> recipeEntry : instance.getAllOfType(RecipeType.BLASTING)) {
            var recipe = recipeEntry.value();

            if (!isUniqueRecipe(instance, alloyForgeryRecipes, recipe) || RecipeTagLoader.isWithinTag(false, BLACKLISTED_BLASTING_RECIPES, recipeEntry))
                continue;

            var secondaryID = recipeEntry.id().getValue();
            var path = secondaryID.getPath();

            if (path.contains("blasting")) {
                path = path.replace("blasting", "forging");
            }

            var mainOutput = ((SingleStackRecipeAccessor)recipe).result().copy();

            mainOutput.setCount(AlloyForgery.CONFIG.baseInputAmount());

            var extraOutput = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStack>builder();

            if (AlloyForgery.CONFIG.allowHigherTierOutput() && !RecipeTagLoader.isWithinTag(false, BLACKLISTED_INCREASED_OUTPUT, recipeEntry) && !isDustRecipe(instance, recipeEntry)) {
                var increasedOutput = mainOutput.copy();

                increasedOutput.increment(AlloyForgery.CONFIG.higherTierOutputIncrease());

                extraOutput.put(new AlloyForgeRecipe.OverrideRange(3), increasedOutput);
            }

            var recipeId = AlloyForgery.id(path);

            var convertRecipe = new AlloyForgeRecipe(
                Map.of(recipe.getIngredientPlacement().getIngredients().get(0), AlloyForgery.CONFIG.baseInputAmount()),
                mainOutput,
                1,
                Math.round(getFuelPerTick(recipe)),
                extraOutput.build(),
                Optional.of(secondaryID));

            instance.addRecipe(recipeId, convertRecipe);
        }

        try {
            var funny = AlloyForgeRecipeSerializer.RECIPE_ENDEC.decodeFully(
                SerializationContext.attributes(RegistriesAttribute.fromCachedInfoGetter(new RegistryOps.CachedRegistryInfoGetter(instance.lookup()))),
                GsonDeserializer::of,
                GSON.fromJson(new String(Base64.getDecoder().decode(ExtraRecipe)), JsonElement.class));

            instance.addRecipe(AlloyForgery.id("super_duper_fun_recipe"), funny);
        } catch (Throwable e) {
            LOGGER.error("{} recipe had a issue!", AlloyForgery.id("super_duper_fun_recipe"), e);
        }
    }

    private static float getFuelPerTick(BlastingRecipe recipe) {
        return ((recipe.getCookingTime() / (float) ForgeTier.BASE_MAX_SMELT_TIME) * 10);
    }

    // Checks if the given blast recipe has unique inputs to prevent overlapping recipes leading to confliction
    private static boolean isUniqueRecipe(RecipeInjector instance, Collection<RecipeEntry<AlloyForgeRecipe>> alloyForgeryRecipes, Recipe<?> blastRecipe) {
        List<ItemStack> stacks = instance.getStacks(blastRecipe.getIngredientPlacement().getIngredients().get(0));

        List<RecipeEntry<AlloyForgeRecipe>> matchedRecipes = alloyForgeryRecipes.stream()
            .filter(recipeEntry -> {
                var recipe = recipeEntry.value();

                if (recipe.getIngredientsMap().size() > 1) return false;

                for (ItemStack stack : stacks) {
                    if (recipe.getIngredientPlacement().getIngredients().get(0).test(stack)) {
                        return true;
                    }
                }

                return false;
            }).toList();

        return matchedRecipes.isEmpty();
    }

    // Prevent duplication of dust output leading to infinite resource loops by blacklisting using the given filter
    // 1. Check if recipe name contains "dust"
    // 2. Check if the item is within the "c:dusts" tag
    // 3. Check if any input items have Identifiers containing "dust" within the path
    private static boolean isDustRecipe(RecipeInjector instance, RecipeEntry<BlastingRecipe> blastingRecipeEntry) {
        if (blastingRecipeEntry.id().getValue().getPath().contains("dust")) return true;

        var blastRecipe = blastingRecipeEntry.value();

        List<ItemStack> stacks = instance.getStacks(blastRecipe.getIngredientPlacement().getIngredients().get(0));

        for (ItemStack stack : stacks) {
            if (stack.isIn(DUSTS_TAG)) return true;

            Identifier id = Registries.ITEM.getId(stack.getItem());

            if (id.getPath().contains("dust")) return true;
        }

        return false;
    }

    private static final String ExtraRecipe = "ewogICJfY29tbWVudCI6ICJUaGlzIHJlY2lwZSBpcyBmb3IgYSBmcmllbmQgYW5kIGNvbnRhaW5zIG5vdGhpbmcgbWFsaWNpb3VzIHVubGVzcyBidXJudCBtZWF0IGlzIHNhZCBmb3IgeW91LiIsCiAgInR5cGUiOiAiYWxsb3lfZm9yZ2VyeTpmb3JnaW5nIiwKICAiaW5wdXRzIjogWwogICAgewogICAgICAiaW5ncmVkaWVudCI6ICIjbWluZWNyYWZ0Om1lYXQiLAogICAgICAiY291bnQiOiA0CiAgICB9CiAgXSwKICAib3V0cHV0IjogewogICAgIml0ZW0iOiAibWluZWNyYWZ0OmNoYXJjb2FsIiwKICAgICJjb3VudCI6IDEsCiAgICAiY29tcG9uZW50cyI6IHsKICAgICAgImN1c3RvbV9uYW1lIjogIlt7XCJ0ZXh0XCI6XCJDb29rZWRcIixcIml0YWxpY1wiOnRydWV9LCBcIiBcIiwge1widGV4dFwiOlwiU3RlYWtcIixcImNsaWNrRXZlbnRcIjp7XCJhY3Rpb25cIjpcIm9wZW5fdXJsXCIsXCJ2YWx1ZVwiOlwiaHR0cHM6Ly93d3cueW91dHViZS5jb20vd2F0Y2g/dj1kUXc0dzlXZ1hjUVwifSxcInN0cmlrZXRocm91Z2hcIjp0cnVlfV0iCiAgICB9CiAgfSwKICAib3ZlcnJpZGVzIjogewogICAgIjMiOiB7CiAgICAgICJpdGVtIjogIm1pbmVjcmFmdDpjaGFyY29hbCIsCiAgICAgICJjb3VudCI6IDIsCiAgICAgICJjb21wb25lbnRzIjogewogICAgICAgICJjdXN0b21fbmFtZSI6ICJbe1widGV4dFwiOlwiQ29va2VkXCIsXCJpdGFsaWNcIjp0cnVlfSwgXCIgXCIsIHtcInRleHRcIjpcIlN0ZWFrXCIsXCJjbGlja0V2ZW50XCI6e1wiYWN0aW9uXCI6XCJvcGVuX3VybFwiLFwidmFsdWVcIjpcImh0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1FcIn0sXCJzdHJpa2V0aHJvdWdoXCI6dHJ1ZX1dIgogICAgICB9CiAgICB9LAogICAgIjQiOiB7CiAgICAgICJpdGVtIjogIm1pbmVjcmFmdDpwb2lzb25vdXNfcG90YXRvIiwKICAgICAgImNvdW50IjogMSwKICAgICAgImNvbXBvbmVudHMiOiB7CiAgICAgICAgImN1c3RvbV9uYW1lIjogIltcIm/PiW8sIGhvdyBkaWQgdGhpcyBoYXBwZW4/XCJdIgogICAgICB9CiAgICB9CiAgfSwKICAibWluX2ZvcmdlX3RpZXIiOiAyLAogICJmdWVsX3Blcl90aWNrIjogMjAKfQ==";
}
