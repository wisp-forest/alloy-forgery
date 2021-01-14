package wraith.alloy_forgery.blocks;

import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.registry.BlockEntityRegistry;
import wraith.alloy_forgery.screens.AlloyForgerScreenHandler;
import wraith.alloy_forgery.screens.ImplementedInventory;

public class ForgeControllerBlockEntity extends LockableContainerBlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, Tickable {

    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(12, ItemStack.EMPTY);

    private int heatTime = 0;
    private int heatTimeMax = 18000; //15 minutes

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return heatTime;
                case 1:
                    return heatTimeMax;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    heatTime = value;
                    break;
                case 1:
                    heatTimeMax = value;
                    break;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public ForgeControllerBlockEntity() {
        super(BlockEntityRegistry.BLOCK_ENTITIES.get("forge_controller"));
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container." + AlloyForgery.MOD_ID + ".alloy_forge");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        ScreenHandler screen = null;
        if (isValidMultiblock()) {
            screen = new AlloyForgerScreenHandler(syncId, playerInventory, this, propertyDelegate);
        }
        return screen;
    }

    private boolean isValidMultiblock() {
        /*
        switch(this.world.getBlockState(pos).get(HorizontalFacingBlock.FACING).asString()) {
            case "north":
                this.world.getBlockState(pos.south(2));
                break;
        }
         */
        return true;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public int size() {
        return 12;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public void recreateInventory() {}

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return isValidMultiblock() && player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

}
