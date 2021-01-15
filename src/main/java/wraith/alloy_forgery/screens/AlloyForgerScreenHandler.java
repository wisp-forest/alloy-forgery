package wraith.alloy_forgery.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import wraith.alloy_forgery.blocks.ForgeControllerBlockEntity;
import wraith.alloy_forgery.registry.ScreenHandlerRegistry;
import wraith.alloy_forgery.screens.slots.AlloyOutputSlot;
import wraith.alloy_forgery.screens.slots.LavaInputSlot;

public class AlloyForgerScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate delegate;
    public final PlayerEntity player;

    public AlloyForgerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(12), new ArrayPropertyDelegate(4));
    }

    public AlloyForgerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {
        super(ScreenHandlerRegistry.SCREEN_HANDLERS.get("alloy_forger"), syncId);
        this.delegate = delegate;
        this.player = playerInventory.player;
        this.addProperties(this.delegate);
        this.inventory = inventory;
        this.addSlot(new LavaInputSlot(inventory, 0, 8, 58)); //Fuel
        this.addSlot(new AlloyOutputSlot(inventory, 1, 145, 34)); //Alloy Output

        for (int y = 0; y < 2; ++y) {
            for (int x = 0; x < 5; ++x) {
                this.addSlot(new Slot(inventory,2 + y * 5 + x, 44 + x * 18, 27 + y * 18)); //Slot Generator, generates 5 slots, then moves a row down and makes 5 more
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 91 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 149));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (originalStack.getItem() == Items.LAVA_BUCKET) {
                if (!this.insertItem(originalStack, 0, 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 2, this.inventory.size(), false)) {
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

    @Environment(EnvType.CLIENT)
    public int getHeatProgress() {
        int i = this.delegate.get(1);
        if (i == 0) {
            i = 200;
        }
        return this.delegate.get(0) * 48 / i;
    }

    @Environment(EnvType.CLIENT)
    public boolean isHeating() {
        return this.delegate.get(0) > 0;
    }

    public void updateResult(int syncId, World world, PlayerEntity player) {
        if (world.isClient || !(inventory instanceof ForgeControllerBlockEntity)) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
        ItemStack recipe = ((ForgeControllerBlockEntity)inventory).getRecipe();
        if (recipe == null) {
            recipe = ItemStack.EMPTY;
        }
        this.inventory.setStack(1, recipe);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, 1, recipe));
    }

}
