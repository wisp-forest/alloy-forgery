package wraith.alloy_forgery.compat.rei;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import wraith.alloy_forgery.RecipeOutput;

import java.util.*;
import java.util.stream.Collectors;

public class AlloyForgeDisplay implements RecipeDisplay {

    private final HashMap<List<Ingredient>, Integer> inputs;
    private final RecipeOutput output;

    public AlloyForgeDisplay(HashMap<List<Ingredient>, Integer> inputs, RecipeOutput output) {
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {

        List<List<EntryStack>> inputStacks = new ArrayList<>();

        inputs.forEach((ingredientList, integer) -> {

            List<EntryStack> entry = new ArrayList<>();

            ingredientList.forEach(ingredient -> {
                entry.addAll(Arrays.stream(ingredient.getMatchingStacksClient()).map(itemStack -> {
                    ItemStack actual = itemStack.copy();
                    actual.setCount(integer);
                    return EntryStack.create(actual);
                }).collect(Collectors.toList()));
            });

            inputStacks.add(entry);

        });

        return inputStacks;
    }

    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(Collections.singletonList(EntryStack.create(new ItemStack(output.getOutputAsItem(), output.outputAmount))));
    }

    @Override
    public @NotNull Identifier getRecipeCategory() {
        return AlloyForgeryREIPlugin.ALLOY_FORGE_CATEGORY_ID;
    }

    public RecipeOutput getOutput() {
        return output;
    }
}
