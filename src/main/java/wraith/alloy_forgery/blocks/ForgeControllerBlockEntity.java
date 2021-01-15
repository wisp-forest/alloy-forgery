package wraith.alloy_forgery.blocks;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.Forge;
import wraith.alloy_forgery.registry.BlockEntityRegistry;
import wraith.alloy_forgery.screens.AlloyForgerScreenHandler;
import wraith.alloy_forgery.screens.ImplementedInventory;

import java.util.HashMap;
import java.util.Map;

public class ForgeControllerBlockEntity extends LockableContainerBlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, Tickable, BlockEntityClientSerializable {

    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(12, ItemStack.EMPTY);
    private AlloyForgerScreenHandler handler;

    private int heatTime = 0;
    private int heatTimeMax = 72000; //1 hour, or 5 minutes per bucket

    private int smeltingTime = 0;
    private int smeltingTimeMax = 18000; //15 minutes

    private boolean lastHeatStatus = false;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return heatTime;
                case 1:
                    return heatTimeMax;
                case 2:
                    return smeltingTime;
                case 3:
                    return smeltingTimeMax;
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
                case 2:
                    smeltingTime = value;
                    break;
                case 3:
                    smeltingTimeMax = value;
                default:
                    break;
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public ForgeControllerBlockEntity() {
        super(BlockEntityRegistry.FORGE_CONTROLLER);
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container." + AlloyForgery.MOD_ID + ".alloy_forge");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        ScreenHandler screen = null;
        if (isValidMultiblock()) {
            screen = new AlloyForgerScreenHandler(syncId, inv, this, propertyDelegate);
        }
        this.handler = (AlloyForgerScreenHandler) screen;
        return screen;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return createMenu(syncId, playerInventory, playerInventory.player);
    }

    public boolean isValidMultiblock() {
        String controllerId = Registry.BLOCK.getId(world.getBlockState(pos).getBlock()).getPath();
        if (!Forge.FORGES.containsKey(controllerId)) {
            return false;
        }
        Forge forge = Forge.FORGES.get(controllerId);
        String block;
        BlockPos center;
        switch(getCachedState().get(HorizontalFacingBlock.FACING).asString()) {
            case "north":
                center = pos.south(1);

                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.south())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.east())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.west())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }

                break;
            case "east":
                center = pos.west(1);
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.south())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.north())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.west())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                break;
            case "south":
                center = pos.north(1);
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.north())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.east())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.west())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                break;
            default:
                center = pos.east(1);
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.south())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.east())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.north())).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
                break;
        }
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z){
                block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX() + x, center.getY() - 1, center.getZ() + z)).getBlock()).toString();
                if (!forge.materials.contains(block)) {
                    return false;
                }
            }
        }
        block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX() + 1, center.getY() + 1, center.getZ())).getBlock()).toString();
        if (!forge.materials.contains(block)) {
            return false;
        }
        block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX() - 1, center.getY() + 1, center.getZ())).getBlock()).toString();
        if (!forge.materials.contains(block)) {
            return false;
        }
        block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX(), center.getY() + 1, center.getZ() + 1)).getBlock()).toString();
        if (!forge.materials.contains(block)) {
            return false;
        }
        block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX(), center.getY() + 1, center.getZ() - 1)).getBlock()).toString();
        if (!forge.materials.contains(block)) {
            return false;
        }

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
        if (this.heatTime > 0) {
            --this.heatTime;
        } else if (this.heatTime < 0) {
            this.heatTime = 0;
        }

        if (this.smeltingTime > 0) {
            --this.smeltingTime;
        } else if (this.smeltingTime < 0) {
            this.smeltingTime = 0;
        }

        if (inventory.get(0).getItem() == Items.LAVA_BUCKET && increaseHeat(1)){
            this.inventory.set(0, new ItemStack(Items.BUCKET));
        }

        boolean isHeating = this.isHeating();
        if (lastHeatStatus != isHeating) {
            lastHeatStatus = isHeating;
            this.world.setBlockState(this.pos, this.world.getBlockState(pos).with(ForgeControllerBlock.LIT, isHeating));
        }
    }

    public boolean increaseHeat(int amount) {
        int heatTime = 6000 * amount;
        if (this.heatTime + heatTime <= this.heatTimeMax) {
            this.heatTime = Math.min(this.heatTime + heatTime, this.heatTimeMax);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.heatTime = tag.getInt("HeatTime");
        this.smeltingTime = tag.getInt("SmeltingTime");
        this.inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        Inventories.fromTag(tag, inventory);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putInt("HeatTime", this.heatTime);
        tag.putInt("SmeltingTime", this.smeltingTime);
        Inventories.toTag(tag, this.inventory);
        return tag;
    }

    public boolean isHeating() {
        return this.heatTime > 0;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public ItemStack getRecipe() {
        HashMap<String, Integer> items = new HashMap<>();
        for (int i = 2; i < size(); ++i) {
            if (inventory.get(i) == ItemStack.EMPTY) {
                continue;
            }
            String itemId = Registry.ITEM.getId(inventory.get(i).getItem()).toString();
            if (items.containsKey(itemId)) {
                items.put(itemId, items.get(itemId) + inventory.get(i).getCount());
            } else {
                items.put(itemId, inventory.get(i).getCount());
            }
        }
        //For each recipe
        for (Map.Entry<HashMap<String, Integer>, Pair<String, Integer>> recipe : Forge.FORGE_RECIPES.entrySet()) {
            if (recipe.getKey().size() != items.size()) {
                continue;
            }
            boolean isRightRecipe = true;
            //For each input ingredient
            for (Map.Entry<String, Integer> input : recipe.getKey().entrySet()) {
                String material = input.getKey();
                boolean isRightIngredient = false;
                for (Map.Entry<String, Integer> ingredient : Forge.MATERIAL_WORTH.get(material).entrySet()) {
                    if (items.containsKey(ingredient.getKey()) && items.get(ingredient.getKey()) * ingredient.getValue() >= input.getValue()) {
                        isRightIngredient = true;
                        break;
                    }
                }
                if (!isRightIngredient) {
                    isRightRecipe = false;
                    break;
                }
            }
            if (isRightRecipe) {
                return new ItemStack(Registry.ITEM.get(new Identifier(recipe.getValue().getFirst())), recipe.getValue().getSecond());
            }
        }
        return null;
    }

    public PropertyDelegate getDelegate() {
        return this.propertyDelegate;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        BlockState state = world.getBlockState(pos);
        fromTag(state, tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return toTag(tag);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        if (slot != 1) {
            markDirty();
        }
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty() && slot != 1) {
            markDirty();
        }
        return result;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.handler != null) {
            this.handler.updateResult(this.handler.syncId, this.world, this.handler.player);
        }
    }
}
