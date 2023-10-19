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


    @Override
    public void addRecipes(RecipeInjector instance) {
        var manager = instance.manager();

        List<AlloyForgeRecipe> alloyForgeryRecipes = manager.listAllOfType(AlloyForgeRecipe.Type.INSTANCE);

        for (BlastingRecipe recipe : manager.listAllOfType(RecipeType.BLASTING)) {
            if(!isUniqueRecipe(alloyForgeryRecipes, recipe) || RecipeTagLoader.isWithinTag(BLACKLISTED_BLASTING_RECIPES, recipe)) continue;

            var secondaryID = recipe.getId();
            var path = secondaryID.getPath();

            if(path.contains("blasting")){
                path = path.replace("blasting", "forging");
            }

            var id = AlloyForgery.id(path);

            var convertRecipe = new AlloyForgeRecipe(id,
                    Map.of(recipe.getIngredients().get(0), 1),
                    recipe.getOutput(null),
                    1,
                    Math.round(getFuelPerTick(recipe)),
                    ImmutableMap.of()
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
