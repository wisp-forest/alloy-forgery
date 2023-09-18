package wraith.alloyforgery.recipe.handlers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

import java.util.Optional;
import java.util.function.Consumer;

public class AlloyForgeRecipeHandler extends ForgeRecipeHandler<AlloyForgeRecipe> {

    @Override
    public Optional<AlloyForgeRecipe> gatherRecipe(RecipeContext context) {
        return context.world().getRecipeManager().getFirstMatch(AlloyForgeRecipe.Type.INSTANCE, context.inventory(), context.world());
    }

    @Override
    public ItemStack recipeOutput(RecipeContext context, AlloyForgeRecipe recipe) {
        return recipe.getOutput(context.forgeDefinition().forgeTier());
    }

    @Override
    public boolean isAbleToSmelt(RecipeContext context) {
        final var recipe = this.lastRecipe.get();

        if (recipe.getMinForgeTier() > context.forgeDefinition().forgeTier()) return false;

        return super.isAbleToSmelt(context);
    }

    @Override
    public float getFuelRequirement(RecipeContext context) {
        return this.lastRecipe.get().getFuelPerTick() * context.forgeDefinition().speedMultiplier();
    }

    @Override
    public void craftRecipe(RecipeContext context, Consumer<DefaultedList<ItemStack>> remainderConsumer) {
        var recipe = this.lastRecipe.get();

        var remainderList = recipe.gatherRemainders(context.inventory());

        if (remainderList != null) remainderConsumer.accept(remainderList);

        recipe.craft(context.inventory(), context.world().getRegistryManager());

        super.craftRecipe(context, remainderConsumer);
    }
}
