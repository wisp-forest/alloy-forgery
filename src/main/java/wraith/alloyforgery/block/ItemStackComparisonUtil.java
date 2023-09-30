package wraith.alloyforgery.block;

import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemStackComparisonUtil {

    /**
     * Compares two lists of item stacks and returns true if they are different from one another
     */
    public static boolean itemsChanged(List<ItemStack> list1, List<ItemStack> list2) {
        if (list1.size() != list2.size()) return true;

        for (int i = 0; i < list1.size(); i++) {
            ItemStack stack1 = list1.get(i);
            ItemStack stack2 = list2.get(i);

            if (!ItemStack.areEqual(stack1, stack2)) {
                return true;
            }
        }

        return false;
    }
}
