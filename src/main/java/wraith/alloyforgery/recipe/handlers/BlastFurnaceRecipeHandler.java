package wraith.alloyforgery.recipe.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;
import wraith.alloyforgery.forges.ForgeDefinition;

import java.util.Optional;
import java.util.function.Consumer;

public class BlastFurnaceRecipeHandler extends ForgeRecipeHandler<BlastingRecipe> {

    private final SingleSlotInventory emulatedInv = new SingleSlotInventory();

    @Override
    public Optional<BlastingRecipe> gatherRecipe(RecipeContext context) {
        Optional<BlastingRecipe> possibleRecipe = Optional.empty();

        for (int i = 0; i < 10; i++) {
            possibleRecipe = context.world().getRecipeManager().getFirstMatch(RecipeType.BLASTING, emulatedInv.changeIndex(i, context.inventory()), context.world());

            if (possibleRecipe.isPresent()) break;
        }

        return possibleRecipe;
    }

    @Override
    public ItemStack recipeOutput(RecipeContext context, BlastingRecipe recipe) {
        var stack = recipe.getOutput(context.world().getRegistryManager()).copy();

        //if(context.forgeDefinition().forgeTier() >= 3) stack.increment(1);

        return stack;
    }

    @Override
    public float getFuelRequirement(RecipeContext context) {
        return getFuelPerTick(this.lastRecipe.get()) * context.forgeDefinition().speedMultiplier();
    }

    public static float getFuelPerTick(BlastingRecipe recipe){
        return ((recipe.getCookTime() / (float) ForgeDefinition.BASE_MAX_SMELT_TIME) * 10);
    }

    @Override
    public void craftRecipe(RecipeContext context, Consumer<DefaultedList<ItemStack>> remainderConsumer) {
        var inputStack = emulatedInv.getStack(0);

        inputStack.decrement(recipeOutput(context, this.lastRecipe.get()).getCount());

        if (inputStack.isEmpty()) emulatedInv.setStack(0, inputStack);

        super.craftRecipe(context, remainderConsumer);
    }

    private static class SingleSlotInventory implements Inventory {

        public int index = 0;
        public Inventory wrappedInventory = new SimpleInventory();

        public SingleSlotInventory(){}

        public SingleSlotInventory changeIndex(int index, Inventory wrappedInventory){
            this.index = index;
            this.wrappedInventory = wrappedInventory;

            return this;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return this.wrappedInventory.getStack(index).isEmpty();
        }

        @Override
        public ItemStack getStack(int slot) {
            return this.wrappedInventory.getStack(index);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            return this.wrappedInventory.removeStack(index, amount);
        }

        @Override
        public ItemStack removeStack(int slot) {
            return this.wrappedInventory.removeStack(index);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            this.wrappedInventory.setStack(index, stack);
        }

        @Override
        public void markDirty() {
            this.wrappedInventory.markDirty();
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return this.wrappedInventory.canPlayerUse(player);
        }

        @Override
        public void clear() {}
    }
}
