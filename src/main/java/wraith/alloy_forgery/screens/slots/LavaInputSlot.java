package wraith.alloy_forgery.screens.slots;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class LavaInputSlot extends Slot {
    public LavaInputSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() == Items.LAVA_BUCKET;
    }
}
