package wraith.alloyforgery.recipe.handlers;

import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.util.ImplementedInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import wraith.alloyforgery.block.ItemStackComparisonUtil;
import wraith.alloyforgery.forges.ForgeDefinition;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class ForgeRecipeHandler<R extends Recipe<Inventory>> {

    protected Optional<R> lastRecipe = Optional.empty();

    protected final DefaultedList<ItemStack> previousItems = DefaultedList.of();

    public final boolean isRecipePresent(RecipeContext context) {
        var items = context.inventory.getItems();

        if (ItemStackComparisonUtil.itemsChanged(items, previousItems)) {
            if (lastRecipe.isEmpty() || !lastRecipe.get().matches(context.inventory(), context.world())) {
                lastRecipe = gatherRecipe(context);
            }

            this.previousItems.clear();
            this.previousItems.addAll(items.stream().map(ItemStack::copy).toList());
        }

        return lastRecipe.isPresent();
    }

    public abstract Optional<R> gatherRecipe(RecipeContext context);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canSmelt(RecipeContext context) {
        final var outputStack = context.inventory.getStack(10);

        return outputStack.isEmpty() || ItemOps.canStack(outputStack, recipeOutput(context, lastRecipe.get()));
    }

    public abstract ItemStack recipeOutput(RecipeContext context, R recipe);

    public abstract float getFuelRequirement(RecipeContext context);

    public void craftRecipe(RecipeContext context, Consumer<DefaultedList<ItemStack>> remainderConsumer) {
        var outputStack = context.inventory.getStack(10);
        var recipeOutput = recipeOutput(context, lastRecipe.get());

        if (outputStack.isEmpty()) {
            context.inventory.setStack(10, recipeOutput);
        } else {
            outputStack.increment(recipeOutput.getCount());
        }
    }

    public record RecipeContext(World world, ImplementedInventory inventory, ForgeDefinition forgeDefinition) {
    }
}
