package wraith.alloyforgery.forges;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * A way to view an {@link Inventory} as a Slot-less representation of the base items within the said Inventory.
 * <p> This dose ignore NBT data and only the Base {@link Item} of a stack will be used to store Info</p>
 */
public class UnifiedInventoryView {

    private final Inventory mainInventory;

    private Map<Item, Integer> unifiedInv = new HashMap<>();

    public UnifiedInventoryView(Inventory inventory){
        this.mainInventory = inventory;

        updateUnifiedInv();
    }

    /**
     * @return the current Unified Inventory View of the given Inventory
     */
    public Map<Item, Integer> getUnifiedInventory(){
        return unifiedInv;
    }

    /**
     * Removes the given Item with a certain amount from the given {@link #mainInventory}.
     * <p>If the given removalAmount is 0 or less, it will remove all instances of the item from the inventory</p>
     * @param item The item to be removed
     * @param removalAmount The amount to be removed
     * @return If the given amount wanted to be removed was actually removed
     */
    public boolean removeItems(Item item, int removalAmount){
        boolean hasRemovedGivenCount = false;

        if(item != Items.AIR && getUnifiedInventory().containsKey(item)) {
            final int currentItemCount = getUnifiedInventory().get(item);
            final int remainder = currentItemCount - removalAmount;

            if (remainder > 0) {
                getUnifiedInventory().replace(item, remainder);

                int leftToRemove = removalAmount;

                for(int i = 0; i <= 9; i++) {
                    ItemStack stack = mainInventory.getStack(i);

                    if(stack.isOf(item)) {
                        if(leftToRemove - stack.getCount() < 0){
                            stack.setCount(stack.getCount() - leftToRemove);

                            break;
                        } else {
                            mainInventory.removeStack(i);

                            leftToRemove = leftToRemove - stack.getCount();
                        }
                    }
                }

                if(leftToRemove < 0){
                    hasRemovedGivenCount = true;
                }
            } else {
                getUnifiedInventory().remove(item);

                for(int i = 0; i <= 9; i++) {
                    final var stack = mainInventory.getStack(i);

                    if(stack.isOf(item)) {
                        mainInventory.removeStack(i);
                    }
                }

                hasRemovedGivenCount = true;
            }

            updateUnifiedInv();
        }

        return hasRemovedGivenCount;
    }

    /**
     * Method that updates {@link #unifiedInv}. Should be called when the Inventory is changed in anyway
     */
    public void updateUnifiedInv() {
        this.unifiedInv = new HashMap<>();

        for(int i = 0; i <= 9; i++) {
            final var stack = mainInventory.getStack(i);

            if (!stack.isEmpty()) {
                final var item = stack.getItem();

                if (unifiedInv.containsKey(item)) {
                    unifiedInv.replace(item, unifiedInv.get(item) + stack.getCount());
                } else {
                    unifiedInv.put(item, stack.getCount());
                }
            }
        }
    }

    /**
     * Checks if the {@link #unifiedInv} is empty
     */
    public boolean isUnifiedInvEmpty(){
        return this.getUnifiedInventory().isEmpty();
    }
}
