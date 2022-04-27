package wraith.alloyforgery.forges;

import dev.architectury.registry.registries.Registries;
import io.wispforest.owo.util.ImplementedInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UnifiedInventory extends ImplementedInventory {

    String UNIFIED_INV_KEY = "UnifiedInventory";

    Map<Item, Integer> getUnifiedInventory();

    void setUnifiedInv(Map<Item, Integer> inv);

    default void removeItems(Item item, int count){
        if(item != Items.AIR && getUnifiedInventory().containsKey(item)) {
            int currentCount = getUnifiedInventory().get(item);
            int remainder = currentCount - count;

            if (remainder > 0) {
                getUnifiedInventory().replace(item, remainder);

                int leftToRemove = count;

                for(int i = 0; i <= 9; i++){
                    ItemStack stack = getStack(i);

                    if(stack.isOf(item)){
                        if(leftToRemove - stack.getCount() < 0){
                            stack.setCount(stack.getCount() - leftToRemove);

                            break;
                        }else{
                            ImplementedInventory.super.removeStack(i);

                            leftToRemove = leftToRemove - stack.getCount();
                        }
                    }
                }
            } else {
                getUnifiedInventory().remove(item);

                for(int i = 0; i <= 9; i++){
                    ItemStack stack = getStack(i);

                    if(stack.isOf(item)){
                        ImplementedInventory.super.removeStack(i);
                    }
                }
            }
        }

        markDirty();
    }

    @Override
    default void markDirty() {
        Map<Item, Integer> unifiedInv = new HashMap<>();

        for(int i = 0; i <= 9; i++) {
            ItemStack stack = getStack(i);

            if (!stack.isEmpty()) {
                final var item = stack.getItem();

                if (unifiedInv.containsKey(item)) {
                    unifiedInv.replace(item, unifiedInv.get(item) + stack.getCount());
                } else {
                    unifiedInv.put(item, stack.getCount());
                }
            }

        }

        setUnifiedInv(unifiedInv);
    }

    default boolean isUnifiedInvEmpty(){
        return this.getUnifiedInventory().isEmpty();
    }

    default void readUnifiedInv(NbtCompound nbt){
        Map<Item, Integer> inv = new HashMap<>();

        for(NbtElement nbtEntry : nbt.getList(UNIFIED_INV_KEY, NbtElement.COMPOUND_TYPE)){
            Item item = Registry.ITEM.get(Identifier.tryParse(((NbtCompound)nbtEntry).getString("Item")));
            int count = ((NbtCompound)nbtEntry).getInt("Count");

            inv.put(item, count);
        }

        setUnifiedInv(inv);
    }

    default void writeUnifiedInv(NbtCompound nbt, List<ItemStack> baseInv){
        NbtList list = new NbtList();

        if(!getUnifiedInventory().isEmpty()) {
            for (Map.Entry<Item, Integer> entry : getUnifiedInventory().entrySet()) {
                NbtCompound nbtEntry = new NbtCompound();

                nbtEntry.putString("Item", Registry.ITEM.getId(entry.getKey()).toString());
                nbtEntry.putInt("Count", entry.getValue());

                list.add(nbtEntry);
            }
        }else{
            for(int i = 0; i < 9; i++){
                ItemStack stack = baseInv.get(i);

                if(stack != ItemStack.EMPTY && stack.getItem() != Items.AIR) {
                    NbtCompound nbtEntry = new NbtCompound();

                    nbtEntry.putString("Item", Registry.ITEM.getId(stack.getItem()).toString());
                    nbtEntry.putInt("Count", stack.getCount());

                    list.add(nbtEntry);
                }
            }
        }

        nbt.put(UNIFIED_INV_KEY, list);
    }
}
