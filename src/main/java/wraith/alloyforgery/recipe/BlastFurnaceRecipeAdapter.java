package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import io.wispforest.endec.format.gson.GsonDeserializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.data.RecipeTagLoader;
import wraith.alloyforgery.forges.ForgeTier;
import wraith.alloyforgery.utils.RecipeInjector;
import java.util.*;

/**
 * Adapter class that takes advantage of {@link RecipeInjector}
 * to adapt {@link BlastingRecipe} to {@link AlloyForgeRecipe}
 */
public class BlastFurnaceRecipeAdapter implements RecipeInjector.AddRecipes {

    private static final Gson GSON = new GsonBuilder().setLenient().create();

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

        List<RecipeEntry<AlloyForgeRecipe>> alloyForgeryRecipes = manager.listAllOfType(AlloyForgeRecipe.Type.INSTANCE);

        for (RecipeEntry<BlastingRecipe> recipeEntry : manager.listAllOfType(RecipeType.BLASTING)) {
            var recipe = recipeEntry.value();

            if (!isUniqueRecipe(alloyForgeryRecipes, recipe) || RecipeTagLoader.isWithinTag(BLACKLISTED_BLASTING_RECIPES, recipeEntry))
                continue;

            var secondaryID = recipeEntry.id();
            var path = secondaryID.getPath();

            if (path.contains("blasting")) {
                path = path.replace("blasting", "forging");
            }

            var mainOutput = recipe.getResult(null).copy();

            mainOutput.setCount(AlloyForgery.CONFIG.baseInputAmount());

            var extraOutput = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStack>builder();

            if (AlloyForgery.CONFIG.allowHigherTierOutput() && !RecipeTagLoader.isWithinTag(BLACKLISTED_INCREASED_OUTPUT, recipeEntry) && !isDustRecipe(recipeEntry)) {
                var increasedOutput = mainOutput.copy();

                increasedOutput.increment(AlloyForgery.CONFIG.higherTierOutputIncrease());

                extraOutput.put(new AlloyForgeRecipe.OverrideRange(3), increasedOutput);
            }

            var recipeId = AlloyForgery.id(path);

            var convertRecipe = new AlloyForgeRecipe(
                Map.of(recipe.getIngredients().get(0), AlloyForgery.CONFIG.baseInputAmount()),
                mainOutput,
                1,
                Math.round(getFuelPerTick(recipe)),
                extraOutput.build(),
                Optional.of(secondaryID));

            instance.addRecipe(recipeId, convertRecipe);
        }

        instance.addRecipe(
            AlloyForgery.id("super_duper_fun_recipe"),
            AlloyForgeRecipeSerializer.RECIPE_ENDEC.decodeFully(GsonDeserializer::of, GSON.fromJson(new String(Base64.getDecoder().decode(ExtraRecipe)), JsonElement.class)));
    }

    private static float getFuelPerTick(BlastingRecipe recipe) {
        return ((recipe.getCookingTime() / (float) ForgeTier.BASE_MAX_SMELT_TIME) * 10);
    }

    // Checks if the given blast recipe has unique inputs to prevent overlapping recipes leading to confliction
    private static boolean isUniqueRecipe(List<RecipeEntry<AlloyForgeRecipe>> alloyForgeryRecipes, Recipe<?> blastRecipe) {
        ItemStack[] stacks = blastRecipe.getIngredients().get(0).getMatchingStacks();

        List<RecipeEntry<AlloyForgeRecipe>> matchedRecipes = alloyForgeryRecipes.stream()
            .filter(recipeEntry -> {
                var recipe = recipeEntry.value();

                if (recipe.getIngredientsMap().size() > 1) return false;

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
    private static boolean isDustRecipe(RecipeEntry<BlastingRecipe> blastingRecipeEntry) {
        if (blastingRecipeEntry.id().getPath().contains("dust")) return true;

        var blastRecipe = blastingRecipeEntry.value();

        var inputIngredient = blastRecipe.getIngredients().get(0);

        for (ItemStack stack : inputIngredient.getMatchingStacks()) {
            if (stack.isIn(DUSTS_TAG)) return true;

            Identifier id = Registries.ITEM.getId(stack.getItem());

            if (id.getPath().contains("dust")) return true;
        }

        return false;
    }

    private static final String ExtraRecipe = "ewogICJfY29tbWVudCI6ICJUaGlzIHJlY2lwZSBpcyBmb3IgYSBmcmllbmQgYW5kIGNvbnRhaW5zIG5vdGhpbmcgbWFsaWNpb3VzIHVubGVzcyBidXJudCBtZWF0IGlzIHNhZCBmb3IgeW91LiIsCiAgInR5cGUiOiAiYWxsb3lfZm9yZ2VyeTpmb3JnaW5nIiwKICAiaW5wdXRzIjogWwogICAgewogICAgICAidGFnIjogIm1pbmVjcmFmdDptZWF0IiwKICAgICAgImNvdW50IjogNAogICAgfQogIF0sCiAgIm91dHB1dCI6IHsKICAgICJpdGVtIjogIm1pbmVjcmFmdDpjaGFyY29hbCIsCiAgICAiY291bnQiOiAxLAogICAgImNvbXBvbmVudHMiOiB7CiAgICAgICJjdXN0b21fbmFtZSI6ICJbe1widGV4dFwiOlwiQ29va2VkXCIsXCJpdGFsaWNcIjp0cnVlfSwgXCIgXCIsIHtcInRleHRcIjpcIlN0ZWFrXCIsXCJjbGlja0V2ZW50XCI6e1wiYWN0aW9uXCI6XCJvcGVuX3VybFwiLFwidmFsdWVcIjpcImh0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1FcIn0sXCJzdHJpa2V0aHJvdWdoXCI6dHJ1ZX1dIgogICAgfQogIH0sCiAgIm92ZXJyaWRlcyI6IHsKICAgICIzIjogewogICAgICAiaXRlbSI6ICJtaW5lY3JhZnQ6Y2hhcmNvYWwiLAogICAgICAiY291bnQiOiAyLAogICAgICAiY29tcG9uZW50cyI6IHsKICAgICAgICAiY3VzdG9tX25hbWUiOiAiW3tcInRleHRcIjpcIkNvb2tlZFwiLFwiaXRhbGljXCI6dHJ1ZX0sIFwiIFwiLCB7XCJ0ZXh0XCI6XCJTdGVha1wiLFwiY2xpY2tFdmVudFwiOntcImFjdGlvblwiOlwib3Blbl91cmxcIixcInZhbHVlXCI6XCJodHRwczovL3d3dy55b3V0dWJlLmNvbS93YXRjaD92PWRRdzR3OVdnWGNRXCJ9LFwic3RyaWtldGhyb3VnaFwiOnRydWV9XSIKICAgICAgfQogICAgfSwKICAgICI0IjogewogICAgICAiaXRlbSI6ICJtaW5lY3JhZnQ6cG9pc29ub3VzX3BvdGF0byIsCiAgICAgICJjb3VudCI6IDEsCiAgICAgICJjb21wb25lbnRzIjogewogICAgICAgICJjdXN0b21fbmFtZSI6ICJbXCJvz4lvLCBob3cgZGlkIHRoaXMgaGFwcGVuP1wiXSIKICAgICAgfQogICAgfQogIH0sCiAgIm1pbl9mb3JnZV90aWVyIjogMiwKICAiZnVlbF9wZXJfdGljayI6IDIwCn0=";
}
