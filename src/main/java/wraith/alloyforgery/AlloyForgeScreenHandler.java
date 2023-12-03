package wraith.alloyforgery;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;
import wraith.alloyforgery.forges.ForgeFuelRegistry;

public class AlloyForgeScreenHandler extends ScreenHandler {

    private final Inventory controllerInventory;
    private final PropertyDelegate propertyDelegate;

    public AlloyForgeScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(ForgeControllerBlockEntity.INVENTORY_SIZE), new ArrayPropertyDelegate(3));
    }

    public AlloyForgeScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(AlloyForgery.ALLOY_FORGE_SCREEN_HANDLER_TYPE, syncId);

        this.controllerInventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);

        //Fuel Slot
        this.addSlot(new Slot(controllerInventory, 11, 8, 74) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return ForgeFuelRegistry.hasFuel(stack.getItem());
            }
        });

        //Recipe Output
        this.addSlot(new Slot(controllerInventory, 10, 145, 50) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        SlotGenerator.begin(this::addSlot, 44, 43)
                .grid(controllerInventory, 0, 5, 2)
                .moveTo(8, 107)
                .playerInventory(playerInventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        return ScreenUtils.handleSlotTransfer(this, invSlot, this.controllerInventory.size());
    }

    public int getSmeltProgress() {
        return propertyDelegate.get(0);
    }

    public int getFuelProgress() {
        return propertyDelegate.get(1);
    }

    public int getLavaProgress() {
        return propertyDelegate.get(2);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.controllerInventory.canPlayerUse(player);
    }

    public Inventory getControllerInventory(){
        return this.controllerInventory;
    }

    /**
     * TODO: This needs to somehow check for whether the recipe is valid.
     *       From a glance this can be done with a new property delegate, although a boolean as an int is a bit strange
     *       Maybe using it as a way to track the tiers is possible? Ideally this should all happen in the lower levels
     */
    public boolean getValidRecipeTier() {
        return false;
    }
}
