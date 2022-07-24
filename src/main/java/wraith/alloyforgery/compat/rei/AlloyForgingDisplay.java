package wraith.alloyforgery.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

import java.util.*;

public class AlloyForgingDisplay implements Display {

    private final List<EntryIngredient> inputs;
    private final EntryIngredient output;

    public final int minForgeTier;
    public final int requiredFuel;

    public final Map<AlloyForgeRecipe.OverrideRange, ItemStack> overrides;

    public AlloyForgingDisplay(AlloyForgeRecipe recipe) {
        List<EntryIngredient> convertedInputs = new ArrayList<>();

        for (Map.Entry<Ingredient, Integer> entry : recipe.getIngredientsMap().entrySet()) {
            for (int i = entry.getValue(); i > 0; ) {
                int stackCount = Math.min(i, 64);

                convertedInputs.add(
                        EntryIngredients.ofItemStacks(Arrays.stream(entry.getKey().getMatchingStacks())
                                .map(ItemStack::copy)
                                .peek(stack -> stack.setCount(stackCount))
                                .toList()));

                i -= stackCount;
            }
        }

        this.inputs = convertedInputs;
        this.output = EntryIngredients.of(recipe.getOutput());

        this.minForgeTier = recipe.getMinForgeTier();
        this.requiredFuel = recipe.getFuelPerTick();

        this.overrides = recipe.getTierOverrides();
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AlloyForgingCategory.ID;
    }
}
