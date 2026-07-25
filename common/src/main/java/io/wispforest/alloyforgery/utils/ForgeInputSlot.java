package io.wispforest.alloyforgery.utils;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import io.wispforest.alloyforgery.AlloyForgeScreenHandler;

public class ForgeInputSlot extends Slot {
    private final AlloyForgeScreenHandler handler;

    public ForgeInputSlot(Container inventory, int index, int x, int y, AlloyForgeScreenHandler handler) {
        super(inventory, index, x, y);

        this.handler = handler;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !this.handler.isSlotDisabled(this) && super.mayPlace(stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.handler.slotsChanged(this.container);
    }
}
