package wraith.alloyforgery.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AlloyForgingDisplay implements Display {

    private final List<EntryIngredient> inputs;
    private final EntryIngredient output;

    public final int minForgeTier;
    public final int requiredFuel;

    public final Map<AlloyForgeRecipe.OverrideRange, ItemStack> overrides;

    public AlloyForgingDisplay(AlloyForgeRecipe recipe) {
        this.inputs = recipe.getIngredients().stream().map(EntryIngredients::ofIngredient).toList();
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
