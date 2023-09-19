package wraith.alloyforgery.block;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;

import java.util.List;

public class ItemStackComparisonUtil {

    public static boolean isEqual(List<ItemStack> stackList1, List<ItemStack> stackList2){
        if(stackList1.size() != stackList2.size()) return false;

        for (int i = 0; i < stackList1.size(); i++) {
            ItemStack stack1 = stackList1.get(i);
            ItemStack stack2 = stackList2.get(i);

            if(!ItemStack.areEqual(stack1, stack2)){
                return false;
            }
        }

        return true;
    }
}
