package wraith.alloyforgery.block;

import com.google.common.collect.ImmutableList;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.util.ImplementedInventory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.AlloyForgeScreenHandler;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.forges.*;
import wraith.alloyforgery.mixin.HopperBlockEntityAccessor;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class ForgeControllerBlockEntity extends BlockEntity implements ImplementedInventory, SidedInventory, NamedScreenHandlerFactory, InsertionOnlyStorage<FluidVariant> {

    private static final int[] DOWN_SLOTS = new int[]{10, 11};
    private static final int[] RIGHT_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] LEFT_SLOTS = new int[]{11};

    public static final int INVENTORY_SIZE = 12;
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private final DefaultedList<ItemStack> previousItems = DefaultedList.of();
    private boolean checkForRecipes = true;

    private Optional<RecipeEntry<AlloyForgeRecipe>> recipeCache = Optional.empty();

    private int requiredTierToCraft = -1;

    private final FluidHolder fluidHolder = new FluidHolder();

    private final ForgeDefinition forgeDefinition;
    private final ImmutableList<BlockPos> multiblockPositions;
    private final Direction facing;

    private float fuel;
    private int currentSmeltTime;

    private int smeltProgress;
    private int fuelProgress;
    private int lavaProgress;

    public ForgeControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AlloyForgery.FORGE_CONTROLLER_BLOCK_ENTITY, pos, state);
        forgeDefinition = ((ForgeControllerBlock) state.getBlock()).forgeDefinition;
        facing = state.get(ForgeControllerBlock.FACING);

        multiblockPositions = generateMultiblockPositions(pos.toImmutable(), state.get(ForgeControllerBlock.FACING));
    }

    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> smeltProgress;
                case 1 -> fuelProgress;
                case 2 -> lavaProgress;
                default -> requiredTierToCraft;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int size() {
            return 4;
        }
    };

    @Override
    public void readNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt, items);

        this.currentSmeltTime = nbt.getInt("CurrentSmeltTime");
        this.fuel = nbt.getInt("Fuel");

        final var fluidNbt = nbt.getCompound("FuelFluidInput");
        this.fluidHolder.amount = fluidNbt.getLong("Amount");
        this.fluidHolder.variant = FluidVariant.fromNbt(nbt.getCompound("Variant"));
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, items);

        nbt.putInt("Fuel", Math.round(fuel));
        nbt.putInt("CurrentSmeltTime", currentSmeltTime);

        final var fluidNbt = new NbtCompound();
        fluidNbt.putLong("Amount", this.fluidHolder.amount);
        fluidNbt.put("Variant", this.fluidHolder.variant.toNbt());
        nbt.put("FuelFluidInput", fluidNbt);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    public ItemStack getFuelStack() {
        return getStack(11);
    }

    public boolean canAddFuel(int fuel) {
        return this.fuel + fuel <= forgeDefinition.fuelCapacity();
    }

    public void addFuel(int fuel) {
        this.fuel += fuel;
    }

    public int getSmeltProgress() {
        return smeltProgress;
    }

    public int getCurrentSmeltTime() {
        return currentSmeltTime;
    }

    public ForgeDefinition getForgeDefinition() {
        return this.forgeDefinition;
    }

    @Override
    public void markDirty() {
        if (ItemStackComparisonUtil.itemsChanged(items, previousItems)) {
            this.previousItems.clear();
            this.previousItems.addAll(items.stream().map(ItemStack::copy).toList());

            this.checkForRecipes = true;
        }

        super.markDirty();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ImplementedInventory.super.setStack(slot, stack);

        this.markDirty();
    }

    public void tick() {
        this.smeltProgress = Math.round((this.currentSmeltTime / (float) forgeDefinition.maxSmeltTime()) * 19);
        this.fuelProgress = Math.round((this.fuel / (float) forgeDefinition.fuelCapacity()) * 48);
        this.lavaProgress = Math.round((this.fluidHolder.getAmount() / (float) FluidConstants.BUCKET) * 50);

        world.updateComparators(pos, getCachedState().getBlock());

        if (!this.verifyMultiblock()) {
            this.currentSmeltTime = 0;

            final var currentState = world.getBlockState(pos);
            if (currentState.get(ForgeControllerBlock.LIT)) {
                world.setBlockState(pos, currentState.with(ForgeControllerBlock.LIT, false));
            }

            return;
        }

        if (!this.getFuelStack().isEmpty()) {
            final var fuelStack = this.getFuelStack();
            final var fuelDefinition = ForgeFuelRegistry.getFuelForItem(fuelStack.getItem());

            if (fuelDefinition != ForgeFuelRegistry.ForgeFuelDefinition.EMPTY && canAddFuel(fuelDefinition.fuel())) {
                this.getFuelStack().decrement(1);

                attemptInsertOnIndex(11, fuelDefinition.hasReturnType() ? new ItemStack(fuelDefinition.returnType()) : ItemStack.EMPTY);

                this.fuel += fuelDefinition.fuel();
            }
        }

        final var emptyFuelSpace = this.forgeDefinition.fuelCapacity() - this.fuel;

        if (this.fluidHolder.amount >= 81 && emptyFuelSpace > 0f) {
            final float fuelInsertAmount = Math.min((this.fluidHolder.amount / 81f) * 24, ((emptyFuelSpace) / 24) * 24);

            this.fuel += fuelInsertAmount;
            this.fluidHolder.amount -= (fuelInsertAmount / 24) * 81;
        }

        final var currentBlockState = this.world.getBlockState(pos);
        if (this.fuel > 100 && !currentBlockState.get(ForgeControllerBlock.LIT)) {
            this.world.setBlockState(pos, currentBlockState.with(ForgeControllerBlock.LIT, true));
        } else if (fuel < 100 && currentBlockState.get(ForgeControllerBlock.LIT)) {
            this.world.setBlockState(pos, currentBlockState.with(ForgeControllerBlock.LIT, false));
        }

        // 1: Check if the inventory is full
        // 2: Prevent crafting when we know that there is not enough fuel to craft at all
        // 3: Prevent recipe checking if the inventory has not changed
        if (this.isEmpty()) {
            this.currentSmeltTime = 0;

            return;
        }

        if (this.fuel < 5 || !this.checkForRecipes) {
            this.currentSmeltTime = 0;

            return;
        }

        //--

        if (this.recipeCache.isEmpty() || !this.recipeCache.get().value().matches(this, this.world)) {
            this.recipeCache = this.world.getRecipeManager().getFirstMatch(AlloyForgeRecipe.Type.INSTANCE, this, this.world);
        }

        if (this.recipeCache.isEmpty() && this.requiredTierToCraft != -1) {
            this.requiredTierToCraft = -1;
        }

        if (this.recipeCache.isEmpty() || !canSmelt(this.recipeCache.get().value())) {
            this.checkForRecipes = false;
            this.currentSmeltTime = 0;
            return;
        }

        //--

        var recipe = recipeCache.get().value();

        if (this.currentSmeltTime < this.forgeDefinition.maxSmeltTime()) {
            final float fuelRequirement = recipe.getFuelPerTick() * this.forgeDefinition.speedMultiplier();

            if (this.fuel - fuelRequirement < 0) {
                this.currentSmeltTime = 0;
                return;
            }

            this.currentSmeltTime++;
            this.fuel -= fuelRequirement;

            if (this.world.random.nextDouble() > 0.75) {
                AlloyForgery.FORGE_PARTICLES.spawn(this.world, Vec3d.of(this.pos), this.facing);
            }
        } else {
            var remainderList = AlloyForgeRecipe.gatherRemainders(recipeCache.get(), this);

            if (remainderList != null) this.handleForgingRemainders(remainderList);

            var outputStack = this.getStack(10);
            var recipeOutput = recipe.craft(this, this.world.getRegistryManager());

            recipe.consumeIngredients(this);

            if (outputStack.isEmpty()) {
                this.setStack(10, recipeOutput);
            } else {
                outputStack.increment(recipeOutput.getCount());
            }

            this.currentSmeltTime = 0;
        }
    }

    private boolean canSmelt(AlloyForgeRecipe recipe) {
        final var outputStack = this.getStack(10);
        final var recipeOutput = recipe.getResult(this.forgeDefinition.forgeTier());

        if (recipe.getMinForgeTier() > this.forgeDefinition.forgeTier()) {
            this.requiredTierToCraft = recipe.getMinForgeTier();

            return false;
        } else if (requiredTierToCraft != -1) {
            this.requiredTierToCraft = -1;
        }

        return outputStack.isEmpty() || ItemOps.canStack(outputStack, recipeOutput);
    }

    private void handleForgingRemainders(DefaultedList<ItemStack> remainderList) {
        for (int i = 0; i < remainderList.size(); ++i) {
            attemptInsertOnIndex(i, remainderList.get(i));
        }
    }

    public void attemptInsertOnIndex(int i, ItemStack itemstack) {
        if (itemstack.isEmpty()) return;

        var slotStack = this.getStack(i);

        if (slotStack.isEmpty()) {
            this.setStack(i, itemstack);
        } else if (ItemStack.areItemsEqual(slotStack, itemstack) && ItemStack.canCombine(slotStack, itemstack)) {
            itemstack.increment(slotStack.getCount());

            if (itemstack.getCount() > itemstack.getMaxCount()) {
                int excess = itemstack.getCount() - itemstack.getMaxCount();
                itemstack.decrement(excess);

                var insertStack = itemstack.copy();
                insertStack.setCount(excess);

                if (!this.attemptToInsertIntoHopper(insertStack)) {
                    var frontForgePos = pos.offset(getCachedState().get(ForgeControllerBlock.FACING));

                    world.playSound(null, frontForgePos.getX(), frontForgePos.getY(), frontForgePos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 0.2F);
                    ItemScatterer.spawn(world, frontForgePos.getX(), frontForgePos.getY(), frontForgePos.getZ(), insertStack);
                }
            }

            this.setStack(i, itemstack);
        } else if (!this.attemptToInsertIntoHopper(itemstack)) {
            var frontForgePos = pos.offset(getCachedState().get(ForgeControllerBlock.FACING));

            world.playSound(null, frontForgePos.getX(), frontForgePos.getY(), frontForgePos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 0.2F);
            ItemScatterer.spawn(world, frontForgePos.getX(), frontForgePos.getY(), frontForgePos.getZ(), itemstack);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean attemptToInsertIntoHopper(ItemStack remainderStack) {
        if (remainderStack.isEmpty()) return true;

        HopperBlockEntity blockEntity = null;

        for (int y = 1; y <= 2; y++) {
            if (world.getBlockEntity(this.pos.down(y)) instanceof HopperBlockEntity hopperBlockEntity) {
                blockEntity = hopperBlockEntity;

                break;
            }
        }

        if (blockEntity != null) {
            var isHopperEmpty = blockEntity.isEmpty();

            for (int slotIndex = 0; slotIndex < blockEntity.size(); ++slotIndex) {
                if (remainderStack.isEmpty()) break;

                if (!blockEntity.getStack(slotIndex).isEmpty()) {
                    final var itemStack = blockEntity.getStack(slotIndex);

                    if (itemStack.isEmpty()) {
                        blockEntity.setStack(slotIndex, remainderStack);
                        remainderStack = ItemStack.EMPTY;
                    } else if (ItemOps.canStack(itemStack, remainderStack)) {
                        int availableSpace = itemStack.getMaxCount() - itemStack.getCount();
                        int j = Math.min(itemStack.getCount(), availableSpace);
                        remainderStack.decrement(j);
                        itemStack.increment(j);
                    }
                } else {
                    blockEntity.setStack(slotIndex, remainderStack);
                    break;
                }
            }

            if (isHopperEmpty && !((HopperBlockEntityAccessor) blockEntity).alloyForge$isDisabled()) {
                ((HopperBlockEntityAccessor) blockEntity).alloyForge$setTransferCooldown(8);
            }

            blockEntity.markDirty();

            return true;
        }

        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean verifyMultiblock() {
        final BlockState belowController = world.getBlockState(multiblockPositions.get(0));
        if (!(belowController.isOf(Blocks.HOPPER) || forgeDefinition.isBlockValid(belowController.getBlock())))
            return false;

        for (int i = 1; i < multiblockPositions.size(); i++) {
            if (!forgeDefinition.isBlockValid(world.getBlockState(multiblockPositions.get(i)).getBlock())) return false;
        }

        return true;
    }

    private static ImmutableList<BlockPos> generateMultiblockPositions(BlockPos controllerPos, Direction controllerFacing) {
        final List<BlockPos> posses = new ArrayList<>();
        final BlockPos center = controllerPos.offset(controllerFacing.getOpposite());

        for (BlockPos pos : BlockPos.iterate(center.add(1, -1, 1), center.add(-1, -1, -1))) {
            posses.add(pos.toImmutable());
        }

        posses.remove(controllerPos.down());
        posses.add(0, controllerPos.down());

        for (int i = 0; i < 2; i++) {
            final var newCenter = center.add(0, i, 0);

            posses.add(newCenter.east());
            posses.add(newCenter.west());
            posses.add(newCenter.north());
            posses.add(newCenter.south());
        }

        posses.remove(controllerPos);
        return ImmutableList.copyOf(posses);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return DOWN_SLOTS;
        } else if (side == facing.rotateYClockwise()) {
            return LEFT_SLOTS;
        } else if (side == facing.rotateYCounterclockwise() && this.currentSmeltTime == 0) {
            return RIGHT_SLOTS;
        } else {
            return new int[0];
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 11) return ForgeFuelRegistry.hasFuel(stack.getItem());

        var slotStack = getStack(slot);

        return slotStack.isEmpty() || ItemOps.canStack(slotStack, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 10 || (slot == 11 && !ForgeFuelRegistry.hasFuel(stack.getItem()));
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.alloy_forgery.forge_controller");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AlloyForgeScreenHandler(syncId, inv, this, properties);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return this.fluidHolder.insert(resource, maxAmount, transaction);
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return this.fluidHolder.iterator();
    }

    private class FluidHolder extends SingleVariantStorage<FluidVariant> implements InsertionOnlyStorage<FluidVariant> {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return FluidConstants.BUCKET + 81;
        }

        @Override
        protected void onFinalCommit() {
            ForgeControllerBlockEntity.this.markDirty();
        }

        @Override
        protected boolean canInsert(FluidVariant variant) {
            return variant.isOf(Fluids.LAVA);
        }

        @Override
        protected boolean canExtract(FluidVariant variant) {
            return false;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator() {
            return InsertionOnlyStorage.super.iterator();
        }
    }

    public static class Type extends BlockEntityType<ForgeControllerBlockEntity> {

        public static Type INSTANCE = new Type();

        private Type() {
            super(ForgeControllerBlockEntity::new, ForgeRegistry.controllerBlocksView(), null);
        }
    }
}
