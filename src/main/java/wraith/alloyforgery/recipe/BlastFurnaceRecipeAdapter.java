package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.forges.ForgeDefinition;
import wraith.alloyforgery.utils.RecipeInjector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Adapter class that takes advantage of {@link RecipeInjector}
 * to adapt {@link BlastingRecipe} to {@link AlloyForgeRecipe}
 */
public class BlastFurnaceRecipeAdapter implements RecipeInjector.AddRecipes {

    private static final TagKey<Item> DUSTS_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "dusts"));

    public static final Identifier BLACKLISTED_BLASTING_RECIPES = AlloyForgery.id("blacklisted_blasting_recipes");
    public static final Identifier BLACKLISTED_INCREASED_OUTPUT = AlloyForgery.id("blacklisted_increased_output");

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

            if(!recipe.isIn(BLACKLISTED_INCREASED_OUTPUT) && !isDustRecipe(recipe)){
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

    private static float getFuelPerTick(BlastingRecipe recipe) {
        return ((recipe.getCookTime() / (float) ForgeDefinition.BASE_MAX_SMELT_TIME) * 10);
    }

    // Checks if the given blast recipe has unique inputs to prevent overlapping recipes leading to confliction
    private static boolean isUniqueRecipe(List<AlloyForgeRecipe> alloyForgeryRecipes, Recipe<?> blastRecipe) {
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

    // Prevent duplication of dust output leading to infinite resource loops by blacklisting using the given filter
    // 1. Check if recipe name contains "dust"
    // 2. Check if the item is within the "c:dusts" tag
    // 3. Check if any input items have Identifiers containing "dust" within the path
    private static boolean isDustRecipe(Recipe<?> blastRecipe){
        if(blastRecipe.getId().getPath().contains("dust")) return true;

        var inputIngredient = blastRecipe.getIngredients().get(0);

        for (ItemStack stack : inputIngredient.getMatchingStacks()) {
            if(stack.isIn(DUSTS_TAG)) return true;

            Identifier id = Registries.ITEM.getId(stack.getItem());

            if(id.getPath().contains("dust")) return true;
        }

        return false;
    }
}
