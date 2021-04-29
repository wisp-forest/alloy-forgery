package wraith.alloy_forgery.screens.slots;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.api.ForgeFuels;

public class FuelInputSlot extends Slot {

    public FuelInputSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return ForgeFuels.FUELS.containsKey(Registry.ITEM.getId(stack.getItem()).toString());
    }

}
