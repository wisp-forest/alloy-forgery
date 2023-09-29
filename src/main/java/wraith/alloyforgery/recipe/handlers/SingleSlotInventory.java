package wraith.alloyforgery.recipe.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class SingleSlotInventory implements Inventory {

    private int index = 0;
    private Inventory wrappedInventory = new SimpleInventory();

    public SingleSlotInventory() {
    }

    public SingleSlotInventory changeIndex(int index, Inventory wrappedInventory) {
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
    public void clear() {
    }
}
