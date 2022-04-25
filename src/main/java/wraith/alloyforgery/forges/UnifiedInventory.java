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

    @Override
    default ItemStack removeStack(int slot, int count) {
        if(slot <= 9)
            this.removeItem(this.getStack(slot).getItem(), count);

        return ImplementedInventory.super.removeStack(slot);
    }

    @Override
    default ItemStack removeStack(int slot) {
        ItemStack stack = this.getStack(slot);

        if(slot <= 9)
            this.removeItem(stack.getItem(), stack.getCount());

        return ImplementedInventory.super.removeStack(slot);
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        ItemStack slotStack = getStack(slot);

        if(slot <= 9) {
            if (slotStack != ItemStack.EMPTY) {
                removeItem(slotStack.getItem(), slotStack.getCount());
            }

            if (stack != ItemStack.EMPTY) {
                addItem(stack.getItem(), stack.getCount());
            }
        }

        ImplementedInventory.super.setStack(slot, stack);
    }

    default void removeItem(Item item, int count){
        if(item != Items.AIR) {
            int currentCount = getUnifiedInventory().get(item);
            int remainder = currentCount - count;

            if (remainder > 0) {
                getUnifiedInventory().replace(item, remainder);
            } else {
                getUnifiedInventory().remove(item);
            }
        }
    }

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
    }

    default void addItem(Item item, int count){
        if(getUnifiedInventory().containsKey(item)) {
            getUnifiedInventory().replace(item, getUnifiedInventory().get(item) + count);
        }else{
            getUnifiedInventory().put(item, count);
        }
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

    default Map<Item, Integer> readUnifiedInv(NbtCompound nbt){
        Map<Item, Integer> inv = new HashMap<>();

        for(NbtElement nbtEntry : nbt.getList(UNIFIED_INV_KEY, NbtElement.COMPOUND_TYPE)){
            Item item = Registry.ITEM.get(Identifier.tryParse(((NbtCompound)nbtEntry).getString("Item")));
            int count = ((NbtCompound)nbtEntry).getInt("Count");

            inv.put(item, count);
        }

        return inv;
    }

    default void writeUnifiedInv(NbtCompound nbt, List<ItemStack> items){
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
                ItemStack stack = items.get(i);

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
