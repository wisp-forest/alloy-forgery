package wraith.alloy_forgery.blocks;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.alloy_forgery.*;
import wraith.alloy_forgery.api.ForgeFuels;
import wraith.alloy_forgery.api.ForgeRecipes;
import wraith.alloy_forgery.api.Forges;
import wraith.alloy_forgery.api.MaterialWorths;
import wraith.alloy_forgery.registry.BlockEntityRegistry;
import wraith.alloy_forgery.screens.AlloyForgerScreenHandler;
import wraith.alloy_forgery.screens.ImplementedInventory;
import wraith.alloy_forgery.utils.Utils;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

// Tickable removed, likely replaced by BlockEntityTicker.getTicker
// Otherwise minor refactors for CompoundTag to NbtCompound

public class ForgeControllerBlockEntity extends LockableContainerBlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, BlockEntityClientSerializable {

    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(12, ItemStack.EMPTY);
    private Map.Entry<HashMap<String, Integer>, RecipeOutput> recipe = null;

    private AlloyForgerScreenHandler handler;

    private int timer = 0;

    private int heat = 0;
    private int maxHeat = -1;

    private int smeltingTime = 0;
    private int smeltingTimeMax = 200; //10 seconds

    public float getForgeTier() {
        String id = Registry.BLOCK.getId(getCachedState().getBlock()).getPath();
        Forge forge = Forges.getForge(id);
        if (forge == null) {
            return -1;
        } else {
            return forge.tier;
        }
    }

    public void setMaxHeat(int maxHeat) {
        this.maxHeat = maxHeat;
    }

    public void syncGUI() {
        if (world != null && !world.isClient && handler != null && handler.player != null) {
            PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
            NbtCompound tag = new NbtCompound();
            tag.putInt("heat", this.heat);
            tag.putInt("smelting_time", this.smeltingTime);
            packet.writeNbt(tag);
            ServerPlayNetworking.send((ServerPlayerEntity) handler.player, Utils.ID("update_gui"), packet);
        }
    }

    public void updateHeat(int heat) {
        syncGUI();
        this.heat = heat;
    }
    public void updateSmeltingTime(int smeltingTime) {
        syncGUI();
        this.smeltingTime = smeltingTime;
    }


    public ForgeControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FORGE_CONTROLLER, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container." + AlloyForgery.MOD_ID + ".alloy_forge");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        AlloyForgerScreenHandler screen = null;
        if (isValidMultiblock()) {
            screen = new AlloyForgerScreenHandler(syncId, inv, this);
        } else {
            player.sendMessage(new TranslatableText("message.alloy_forgery.invalid_multiblock"), false);
            player.playSound(SoundEvents.BLOCK_BASALT_FALL, SoundCategory.BLOCKS, 1.0F, 0.2F);
        }
        this.handler = screen;
        return screen;
    }

    public static BlockPos getBackPos(BlockState state, BlockPos pos) {
        return pos.offset(state.get(ForgeControllerBlock.FACING).getOpposite());
    }

    public static BlockPos getFrontPos(BlockState state, BlockPos pos) {
        return pos.offset(state.get(ForgeControllerBlock.FACING));
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return createMenu(syncId, playerInventory, playerInventory.player);
    }

    public boolean isValidMultiblock() {
        String controllerId = Registry.BLOCK.getId(world.getBlockState(pos).getBlock()).getPath();
        if (!Forges.hasForge(controllerId)) {
            return false;
        }
        Forge forge = Forges.getForge(controllerId);
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
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return isValidMultiblock() && player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    public static <T extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, ForgeControllerBlockEntity forge) {
        forge.tick();
    }

    public void tick() {
        renderSmoke();
        Map.Entry<HashMap<String, Integer>, RecipeOutput> currentRecipe = getRecipe();
        if (!isValidMultiblock()) {
            return;
        }
        if (this.heat > 0) {
            if (this.smeltingTime <= 0 || this.recipe != currentRecipe) {
                if (this.smeltingTime <= 0 && this.recipe != null && this.inventory.get(1).getCount() + this.recipe.getValue().outputAmount <= this.inventory.get(1).getMaxCount()) {
                    int outputAmount = this.recipe.getValue().outputAmount;
                    Item outputItem = this.recipe.getValue().getOutputAsItem();
                    if (this.inventory.get(1).getItem() == outputItem) {
                        outputAmount += this.inventory.get(1).getCount();
                    }
                    this.inventory.set(1, new ItemStack(outputItem, outputAmount));
                    if (this.handler != null) {
                        this.handler.updateItems(currentRecipe);
                    }
                }
                this.recipe = currentRecipe;
                this.updateSmeltingTime(this.smeltingTimeMax);
            } else if (this.recipe != null &&
                    (this.recipe.getValue().heatAmount < this.heat) && (this.recipe.getValue().requiredTier <= getForgeTier()) &&
                    (this.inventory.get(1).isEmpty() || this.inventory.get(1).getItem() == this.recipe.getValue().getOutputAsItem()) &&
                    this.inventory.get(1).getCount() + this.recipe.getValue().outputAmount <= this.inventory.get(1).getMaxCount()) { //If is smelting:
                updateHeat(Math.max(this.heat - this.recipe.getValue().heatAmount, 0));
                this.updateSmeltingTime(Math.max(this.smeltingTime - 1, 0));
            }
        }

        String itemID = Registry.ITEM.getId(inventory.get(0).getItem()).toString();
        ForgeFuel fuel = ForgeFuels.FUELS.getOrDefault(itemID, null);
        if (fuel != null && increaseHeat(fuel.getCookTime())) {
            inventory.get(0).decrement(1);
            if (fuel.hasReturnableItem()) {
                assert world != null;
                Block.dropStack(world, getFrontPos(getCachedState(), pos), new ItemStack(Registry.ITEM.get(new Identifier(fuel.getReturnableItem()))));
            }
        }
        if (this.isHeating()) {
            assert this.world != null;
            this.world.setBlockState(this.pos, this.world.getBlockState(pos).with(ForgeControllerBlock.LIT, this.isHeating()));
        }
    }

    public boolean increaseHeat(int amount) {
        if (this.heat + amount <= this.maxHeat) {
            updateHeat(Math.min(this.heat + amount, this.maxHeat));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.heat = tag.getInt("HeatTime");
        this.smeltingTime = tag.getInt("SmeltingTime");
        this.inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        Inventories.readNbt(tag, inventory);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("HeatTime", this.heat);
        tag.putInt("SmeltingTime", this.smeltingTime);
        Inventories.writeNbt(tag, this.inventory);
        return tag;
    }

    public boolean isHeating() {
        return this.heat != 0;
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
        for (Map.Entry<HashMap<String, Integer>, RecipeOutput> recipe : ForgeRecipes.getRecipes()) {
            HashMap<String, Integer> modifiedItems = new HashMap<>(items);
            boolean isRightRecipe = false;
            //For each input ingredient
            for (Map.Entry<String, Integer> input : recipe.getKey().entrySet()) {
                isRightRecipe = false;
                String material = input.getKey();
                if (material.contains(":")) {
                    boolean isRightIngredient = modifiedItems.containsKey(material) && modifiedItems.get(material) >= input.getValue();
                    if (isRightIngredient) {
                        modifiedItems.remove(material);
                        isRightRecipe = true;
                    }
                    else if (material.startsWith("#")) {
                        for (Item item : TagRegistry.item(new Identifier(material.substring(1))).values()) {
                            String id = Registry.ITEM.getId(item).toString();
                            if (modifiedItems.containsKey(id) && modifiedItems.get(id) >= input.getValue()) {
                                modifiedItems.remove(id);
                                isRightRecipe = true;
                                break;
                            }
                        }
                    }
                } else {
                    for (Map.Entry<String, MaterialWorth> ingredient : MaterialWorths.getMaterialWorthMapEntries(material)) {
                        if (ingredient.getKey().startsWith("#")) {
                            for (Item item : TagRegistry.item(new Identifier(ingredient.getKey().substring(1))).values()) {
                                String id = Registry.ITEM.getId(item).toString();
                                if (modifiedItems.containsKey(id) && modifiedItems.get(id) * ingredient.getValue().worth >= input.getValue()) {
                                    modifiedItems.remove(id);
                                    isRightRecipe = true;
                                }
                            }
                        }
                        if (modifiedItems.containsKey(ingredient.getKey()) && modifiedItems.get(ingredient.getKey()) * ingredient.getValue().worth >= input.getValue()) {
                            modifiedItems.remove(ingredient.getKey());
                            isRightRecipe = true;
                            break;
                        }
                    }
                }
                if (!isRightRecipe) {
                    break;
                }
            }
            if (isRightRecipe && modifiedItems.isEmpty()) {
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
    public void markDirty() {
        super.markDirty();
        assert world != null;
        if (!world.isClient) {
            sync();
        }
    }

    public void renderSmoke() {
        if (!isValidMultiblock()) {
            return;
        }
        BlockPos center = getBackPos(getCachedState(), pos);
        if (timer % 20 == 0) {
            assert world != null;
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

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        NbtCompound tag = new NbtCompound();
        tag.putInt("heat", this.heat);
        tag.putInt("max_heat", this.maxHeat);
        tag.putInt("smelting_time", this.smeltingTime);
        tag.putInt("smelting_time_max", this.smeltingTimeMax);
        buf.writeNbt(tag);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        this.heat = tag.getInt("HeatTime");
        this.smeltingTime = tag.getInt("SmeltingTime");
        this.inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        Inventories.readNbt(tag, inventory);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        tag.putInt("HeatTime", this.heat);
        tag.putInt("SmeltingTime", this.smeltingTime);
        Inventories.writeNbt(tag, this.inventory);
        return tag;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{2,3,4,5,6,7,8,9,10,11};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot > 1;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 1;
    }


}