package wraith.alloy_forgery.blocks;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.Forge;
import wraith.alloy_forgery.RecipeOutput;
import wraith.alloy_forgery.registry.BlockEntityRegistry;
import wraith.alloy_forgery.screens.AlloyForgerScreenHandler;
import wraith.alloy_forgery.screens.ImplementedInventory;
import wraith.alloy_forgery.utils.Utils;

import java.util.*;

public class ForgeControllerBlockEntity extends LockableContainerBlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, Tickable, BlockEntityClientSerializable {

    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(12, ItemStack.EMPTY);
    private Map.Entry<HashMap<String, Integer>, RecipeOutput> recipe = null;

    private AlloyForgerScreenHandler handler;

    private int timer = 0;

    private int heatTime = 0;
    private int heatTimeMax = -1;

    private int smeltingTime = 0;
    private int smeltingTimeMax = 200; //10 seconds

    private boolean lastHeatStatus = false;

    public float getForgeTier() {
        String id = Registry.BLOCK.getId(getCachedState().getBlock()).getPath();
        Forge forge = Forge.FORGES.getOrDefault(id, null);
        if (forge == null) {
            return -1;
        } else {
            return forge.tier;
        }
    }

    public void setMaxHeat(int maxHeat) {
        this.heatTimeMax = maxHeat;
    }

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
        AlloyForgerScreenHandler screen = null;
        if (isValidMultiblock()) {
            screen = new AlloyForgerScreenHandler(syncId, inv, this, propertyDelegate, getFrontPos(getCachedState(), pos));
        } else {
            player.sendMessage(new TranslatableText("message.alloy_forgery.invalid_multiblock"), false);
            player.playSound(SoundEvents.BLOCK_BASALT_FALL, SoundCategory.BLOCKS, 1.0F, 0.2F);
        }
        this.handler = screen;
        return screen;
    }

    public static BlockPos getBackPos(BlockState state, BlockPos pos) {
        switch (state.get(ForgeControllerBlock.FACING)) {
            case NORTH:
                return pos.south();
            case SOUTH:
                return pos.north();
            case WEST:
                return pos.east();
            case EAST:
                return pos.west();
            case UP:
                return pos.down();
            case DOWN:
                return pos.up();
            default:
                return pos;
        }
    }

    public static BlockPos getFrontPos(BlockState state, BlockPos pos) {
        switch (state.get(ForgeControllerBlock.FACING)) {
            case NORTH:
                return pos.north();
            case SOUTH:
                return pos.south();
            case WEST:
                return pos.west();
            case EAST:
                return pos.east();
            case UP:
                return pos.up();
            case DOWN:
                return pos.down();
            default:
                return pos;
        }
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
        String blockId;
        Block block;
        BlockPos center = getBackPos(getCachedState(), pos);

        block = world.getBlockState(new BlockPos(center.north())).getBlock();
        blockId = Registry.BLOCK.getId(block).toString();
        if (!forge.materials.contains(blockId) && !(block instanceof ForgeControllerBlock)) {
            return false;
        }
        block = world.getBlockState(new BlockPos(center.west())).getBlock();
        blockId = Registry.BLOCK.getId(block).toString();
        if (!forge.materials.contains(blockId) && !(block instanceof ForgeControllerBlock)) {
            return false;
        }
        block = world.getBlockState(new BlockPos(center.south())).getBlock();
        blockId = Registry.BLOCK.getId(block).toString();
        if (!forge.materials.contains(blockId) && !(block instanceof ForgeControllerBlock)) {
            return false;
        }
        block = world.getBlockState(new BlockPos(center.east())).getBlock();
        blockId = Registry.BLOCK.getId(block).toString();
        if (!forge.materials.contains(blockId) && !(block instanceof ForgeControllerBlock)) {
            return false;
        }

        if (world.getBlockState(center).getBlock() != Blocks.AIR) {
            return false;
        }
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                blockId = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX() + x, center.getY() - 1, center.getZ() + z)).getBlock()).toString();
                if (!forge.materials.contains(blockId)) {
                    return false;
                }
            }
        }
        blockId = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX() + 1, center.getY() + 1, center.getZ())).getBlock()).toString();
        if (!forge.materials.contains(blockId)) {
            return false;
        }
        blockId = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX() - 1, center.getY() + 1, center.getZ())).getBlock()).toString();
        if (!forge.materials.contains(blockId)) {
            return false;
        }
        blockId = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX(), center.getY() + 1, center.getZ() + 1)).getBlock()).toString();
        if (!forge.materials.contains(blockId)) {
            return false;
        }
        blockId = Registry.BLOCK.getId(world.getBlockState(new BlockPos(center.getX(), center.getY() + 1, center.getZ() - 1)).getBlock()).toString();
        if (!forge.materials.contains(blockId)) {
            return false;
        }

        return true;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
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
            return isValidMultiblock() && player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }


    @Override
    public void tick() {
        renderSmoke();
        Map.Entry<HashMap<String, Integer>, RecipeOutput> currentRecipe = getRecipe();
        if (this.smeltingTime <= 0 || this.recipe != currentRecipe || (this.recipe != null && this.recipe.getValue().heatAmount > this.heatTime && this.recipe.getValue().requiredTier <= getForgeTier())) {
            if (this.smeltingTime <= 0 && this.recipe != null && this.inventory.get(1).getCount() + this.recipe.getValue().outputAmount <= this.inventory.get(1).getMaxCount()) {
                int outputAmount = this.recipe.getValue().outputAmount;
                String outputItemId = this.recipe.getValue().outputItem;
                Item outputItem;
                if (outputItemId.startsWith("#")) {
                    List<Item> tags = new ArrayList<>(TagRegistry.item(new Identifier(outputItemId.substring(1))).values());
                    tags.sort(Comparator.comparing(Registry.ITEM::getId));
                    outputItem = tags.get(0);
                } else {
                    outputItem = Registry.ITEM.get(new Identifier(outputItemId));
                }
                if (this.inventory.get(1).getItem() == outputItem) {
                    outputAmount += this.inventory.get(1).getCount();
                }
                this.inventory.set(1, new ItemStack(outputItem, outputAmount));
                if (this.handler != null) {
                    this.handler.updateItems(currentRecipe);
                }
            }
            this.recipe = currentRecipe;
            this.smeltingTime = this.smeltingTimeMax;
        } else if (this.recipe != null) { //If is smelting:
            this.heatTime = Math.max(this.heatTime - this.recipe.getValue().heatAmount, 0);
            this.smeltingTime = Math.max(this.smeltingTime - 1, 0);
        }

        if (inventory.get(0).getItem() == Items.LAVA_BUCKET && increaseHeat(10000)) {
            this.inventory.set(0, new ItemStack(Items.BUCKET));
        }

        boolean isHeating = this.isHeating();
        if (lastHeatStatus != isHeating) {
            lastHeatStatus = isHeating;
            this.world.setBlockState(this.pos, this.world.getBlockState(pos).with(ForgeControllerBlock.LIT, isHeating));
        }
    }

    public boolean increaseHeat(int amount) {
        if (this.heatTime + amount <= this.heatTimeMax) {
            this.heatTime = Math.min(this.heatTime + amount, this.heatTimeMax);
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

    public Map.Entry<HashMap<String, Integer>, RecipeOutput> getRecipe() {
        HashMap<String, Integer> items = new HashMap<>();
        for (int i = 2; i < size(); ++i) {
            if (inventory.get(i).isEmpty()) {
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
        for (Map.Entry<HashMap<String, Integer>, RecipeOutput> recipe : Forge.FORGE_RECIPES.entrySet()) {
            if (recipe.getKey().size() != items.size()) {
                continue;
            }
            boolean isRightRecipe = true;
            //For each input ingredient
            for (Map.Entry<String, Integer> input : recipe.getKey().entrySet()) {
                String material = input.getKey();
                boolean isRightIngredient = false;
                if (material.contains(":")) {
                    isRightIngredient = items.containsKey(material) && items.get(material) >= input.getValue();
                    if (material.startsWith("#")) {
                        for (Item item : TagRegistry.item(new Identifier(material.substring(1))).values()) {
                            String id = Registry.ITEM.getId(item).toString();
                            if (items.containsKey(id) && items.get(id) >= input.getValue()) {
                                isRightIngredient = true;
                                break;
                            }
                        }
                    }
                } else {
                    for (Map.Entry<String, Integer> ingredient : Forge.MATERIAL_WORTH.get(material).entrySet()) {
                        if (ingredient.getKey().startsWith("#")) {
                            for (Item item : TagRegistry.item(new Identifier(ingredient.getKey().substring(1))).values()) {
                                String id = Registry.ITEM.getId(item).toString();
                                if (items.containsKey(id) && items.get(id) >= input.getValue()) {
                                    isRightIngredient = true;
                                    break;
                                }
                            }
                        }
                        if (items.containsKey(ingredient.getKey()) && items.get(ingredient.getKey()) * ingredient.getValue() >= input.getValue()) {
                            isRightIngredient = true;
                            break;
                        }
                    }
                }
                if (!isRightIngredient) {
                    isRightRecipe = false;
                    break;
                }
            }
            if (isRightRecipe) {
                String output = recipe.getValue().outputItem;
                if (!output.startsWith("#") || TagRegistry.item(new Identifier(output.substring(1))).values().size() > 0) {
                    return recipe;
                }
                return null;
            }
        }
        return null;
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
    public void markDirty() {
        super.markDirty();
    }

    public void renderSmoke() {
        if (!isValidMultiblock()) {
            return;
        }
        BlockPos center = getBackPos(getCachedState(), pos);
        if (timer % 20 == 0) {
            if (Utils.getRandomIntInRange(1, 4) == 1) {
                world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.getX() + 0.25, center.getY(), center.getZ() + 0.25, 0, 0.08, 0);
            }
            if (Utils.getRandomIntInRange(1, 4) == 1) {
                world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.getX() + 0.75, center.getY(), center.getZ() + 0.25, 0, 0.08, 0);
            }
            if (Utils.getRandomIntInRange(1, 4) == 1) {
                world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.getX() + 0.25, center.getY(), center.getZ() + 0.75, 0, 0.08, 0);
            }
            if (Utils.getRandomIntInRange(1, 4) == 1) {
                world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, center.getX() + 0.75, center.getY(), center.getZ() + 0.75, 0, 0.08, 0);
            }
            world.addParticle(ParticleTypes.LARGE_SMOKE, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0, 0.05, 0);
            world.addParticle(ParticleTypes.SMOKE, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0, 0.05, 0);
            timer = 0;
        } else {
            ++timer;
        }
    }
}