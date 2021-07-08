package wraith.alloyforgery;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import wraith.alloyforgery.forges.ForgeFuelRegistry;

public class AlloyForgeScreenHandler extends ScreenHandler {

    private final Inventory controllerInventory;
    private final PropertyDelegate propertyDelegate;

    public AlloyForgeScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(12), new ArrayPropertyDelegate(2));
    }

    public AlloyForgeScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(AlloyForgery.ALLOY_FORGE_SCREEN_HANDLER_TYPE, syncId);

        this.controllerInventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);

        int m, l;

        //Fuel Slot
        this.addSlot(new Slot(controllerInventory, 11, 8, 58) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return ForgeFuelRegistry.hasFuel(stack.getItem());
            }
        });

        //Recipe Output
        this.addSlot(new Slot(controllerInventory, 10, 145, 34) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        //Recipe Inputs
        for (m = 0; m < 2; m++) {
            for (l = 0; l < 5; l++) {
                this.addSlot(new Slot(controllerInventory, l + m * 5, 44 + l * 18, 27 + m * 18));
            }
        }

        //Player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 91 + m * 18));
            }
        }
        //Player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 149));
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.controllerInventory.size()) {
                if (!this.insertItem(originalStack, this.controllerInventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.controllerInventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    public int getSmeltProgress() {
        return propertyDelegate.get(0);
    }

    public int getFuelProgress() {
        return propertyDelegate.get(1);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.controllerInventory.canPlayerUse(player);
    }
}
