package wraith.alloy_forgery.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import wraith.alloy_forgery.RecipeOutput;

import java.util.*;
import java.util.stream.Collectors;

public class AlloyForgeDisplay implements Display {

    private final HashMap<List<Ingredient>, Integer> inputs;
    private final RecipeOutput output;

    public AlloyForgeDisplay(HashMap<List<Ingredient>, Integer> inputs, RecipeOutput output) {
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        List<EntryIngredient> inputStacks = new ArrayList<>();
        inputs.forEach((ingredients, integer) -> inputStacks.add(EntryIngredients.ofItemStacks(ingredients.stream().map(Ingredient::getMatchingStacksClient).flatMap(Arrays::stream).collect(Collectors.toList()))));
        return inputStacks;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(EntryIngredients.of(new ItemStack(output.getOutputAsItem(), output.outputAmount)));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AlloyForgeryREIPlugin.ALLOY_FORGE_CATEGORY_ID;
    }

    public RecipeOutput getOutput() {
        return output;
    }
}
