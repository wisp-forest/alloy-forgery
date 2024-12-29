package wraith.alloyforgery.utils;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import wraith.alloyforgery.AlloyForgeScreenHandler;

public class ForgeInputSlot extends Slot {
    private final AlloyForgeScreenHandler handler;

    public ForgeInputSlot(Inventory inventory, int index, int x, int y, AlloyForgeScreenHandler handler) {
        super(inventory, index, x, y);

        this.handler = handler;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !this.handler.isSlotDisabled(this) && super.canInsert(stack);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.handler.onContentChanged(this.inventory);
    }
}
