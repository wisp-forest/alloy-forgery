package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.data.RecipeTagLoader;
import wraith.alloyforgery.forges.ForgeDefinition;
import wraith.alloyforgery.utils.RecipeInjector;

import java.util.*;

public class BlastFurnaceRecipeAdapter implements RecipeInjector.AddRecipes {

    public static final Identifier BLACKLISTED_BLASTING_RECIPES = AlloyForgery.id("blacklisted_blasting_recipes");

    public static final Identifier BLACKLISTED_INCREASED_OUTPUT = AlloyForgery.id("blacklisted_increased_output.json");

    @Override
    public void addRecipes(RecipeInjector instance) {
        var manager = instance.manager();

        List<AlloyForgeRecipe> alloyForgeryRecipes = manager.listAllOfType(AlloyForgeRecipe.Type.INSTANCE);

        for (BlastingRecipe recipe : manager.listAllOfType(RecipeType.BLASTING)) {
            if(!isUniqueRecipe(alloyForgeryRecipes, recipe) || recipe.isIn(BLACKLISTED_BLASTING_RECIPES)) continue;

            var secondaryID = recipe.getId();
            var path = secondaryID.getPath();

            if(path.contains("blasting")){
                path = path.replace("blasting", "forging");
            }

            var mainOutput = recipe.getOutput(null);

            var extraOutput = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStack>builder();

            if(!recipe.isIn(BLACKLISTED_INCREASED_OUTPUT)){
                var increasedOutput = mainOutput.copy();

                increasedOutput.increment(1);

                extraOutput.put(new AlloyForgeRecipe.OverrideRange(2), increasedOutput);
            }

            var convertRecipe = new AlloyForgeRecipe(AlloyForgery.id(path),
                    Map.of(recipe.getIngredients().get(0), 1),
                    mainOutput,
                    1,
                    Math.round(getFuelPerTick(recipe)),
                    extraOutput.build()
            ).setSecondaryID(secondaryID);

            instance.addRecipe(convertRecipe);
        }
    }

    public static float getFuelPerTick(BlastingRecipe recipe) {
        return ((recipe.getCookTime() / (float) ForgeDefinition.BASE_MAX_SMELT_TIME) * 10);
    }

    public static boolean isUniqueRecipe(List<AlloyForgeRecipe> alloyForgeryRecipes, Recipe<?> blastRecipe) {
        ItemStack[] stacks = blastRecipe.getIngredients().get(0).getMatchingStacks();

        List<AlloyForgeRecipe> matchedRecipes = alloyForgeryRecipes.stream()
                .filter(recipe -> {
                    if(recipe.getIngredientsMap().size() > 1) return false;

                    for (ItemStack stack : stacks) {
                        if (recipe.getIngredients().get(0).test(stack)) {
                            return true;
                        }
                    }

                    return false;
                }).toList();

        return matchedRecipes.isEmpty();
    }
}
