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

    private final int beginIndex;
    private final int endIndex;

    private final Map<Item, Integer> unifiedInv = new HashMap<>();

    private boolean isDirty = false;

    public UnifiedInventoryView(Inventory inventory){
        this(inventory, 0, inventory.size() - 1);
    }

    /**
     * Note: The given begin and end Indices are just to tell the code what slots it can view from within the {@link #mainInventory}
     */
    public UnifiedInventoryView(Inventory inventory, int beginIndex, int endIndex){
        this.mainInventory = inventory;

        this.beginIndex = beginIndex;
        this.endIndex = endIndex;

        markDirty();
    }

    /**
     * @return the current Unified Inventory View of the given Inventory
     */
    public Map<Item, Integer> getUnifiedInventory(){
        if(isDirty){
            this.unifiedInv.clear();

            for(int i = beginIndex; i <= endIndex; i++) {
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

            isDirty = false;
        }

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

                for(int i = beginIndex; i <= endIndex; i++) {
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

                for(int i = beginIndex; i <= endIndex; i++) {
                    final var stack = mainInventory.getStack(i);

                    if(stack.isOf(item)) {
                        mainInventory.removeStack(i);
                    }
                }

                hasRemovedGivenCount = true;
            }

            markDirty();
        }

        return hasRemovedGivenCount;
    }

    /**
     * Method used to tell the {@link #getUnifiedInventory()} that it needs to re-cache the {@link #unifiedInv} Map from the linked {@link #mainInventory}
     */
    public void markDirty() {
        this.isDirty = true;
    }

    /**
     * Checks if the {@link #unifiedInv} is empty
     */
    public boolean isUnifiedInvEmpty(){
        return this.getUnifiedInventory().isEmpty();
    }
}
