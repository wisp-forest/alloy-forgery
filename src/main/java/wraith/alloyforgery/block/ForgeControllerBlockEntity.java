package wraith.alloyforgery.block;

import com.glisco.owo.ops.ItemOps;
import com.glisco.owo.particles.ServerParticles;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.AlloyForgeScreenHandler;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.forges.ForgeDefinition;
import wraith.alloyforgery.forges.ForgeFuelRegistry;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

import java.util.ArrayList;
import java.util.List;

public class ForgeControllerBlockEntity extends BlockEntity implements ImplementedInventory, SidedInventory, NamedScreenHandlerFactory {

    private static final int[] DOWN_SLOTS = new int[]{10, 11};
    private static final int[] RIGHT_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] LEFT_SLOTS = new int[]{11};

    private final DefaultedList<ItemStack> ITEMS = DefaultedList.ofSize(12, ItemStack.EMPTY);

    private final ForgeDefinition forgeDefinition;
    private final ImmutableList<BlockPos> multiblockPositions;
    private final BlockPos passthroughPos;
    private final Direction facing;

    private int smeltProgress;
    private int fuelProgress;

    private int fuel;
    private int currentSmeltTime;

    public ForgeControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AlloyForgery.FORGE_CONTROLLER_BLOCK_ENTITY, pos, state);
        forgeDefinition = ((ForgeControllerBlock) state.getBlock()).forgeDefinition;
        facing = state.get(ForgeControllerBlock.FACING);

        passthroughPos = pos.down();
        multiblockPositions = generateMultiblockPositions(pos.toImmutable(), state.get(ForgeControllerBlock.FACING));
    }

    private final PropertyDelegate PROPERTIES = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return index == 0 ? smeltProgress : fuelProgress;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                smeltProgress = value;
            } else {
                fuelProgress = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    @Override
    public void readNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt, ITEMS);

        this.currentSmeltTime = nbt.getInt("CurrentSmeltTime");
        this.fuel = nbt.getInt("Fuel");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, ITEMS);

        nbt.putInt("Fuel", fuel);
        nbt.putInt("CurrentSmeltTime", currentSmeltTime);
        return super.writeNbt(nbt);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return ITEMS;
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

    public void tick() {
        this.smeltProgress = Math.round((this.currentSmeltTime / (float) forgeDefinition.maxSmeltTime()) * 19);
        this.fuelProgress = Math.round((this.fuel / (float) forgeDefinition.fuelCapacity()) * 48);

        world.updateComparators(pos, Blocks.AIR);

        if (!verifyMultiblock()) {
            this.currentSmeltTime = 0;

            final var currentState = world.getBlockState(pos);
            if (currentState.get(ForgeControllerBlock.LIT)) world.setBlockState(pos, currentState.with(ForgeControllerBlock.LIT, false));

            return;
        }

        if (!getFuelStack().isEmpty()) {
            final var fuelStack = getFuelStack();
            final var fuelDefinition = ForgeFuelRegistry.getFuelForItem(fuelStack.getItem());

            if (fuelDefinition != ForgeFuelRegistry.ForgeFuelDefinition.EMPTY && canAddFuel(fuelDefinition.fuel())) {

                if (!ItemOps.emptyAwareDecrement(getFuelStack())) {
                    setStack(11, fuelDefinition.hasReturnType() ? new ItemStack(fuelDefinition.returnType()) : ItemStack.EMPTY);
                }

                this.fuel += fuelDefinition.fuel();
            }
        }

        final var currentBlockState = this.world.getBlockState(pos);
        if (this.fuel > 100 && !currentBlockState.get(ForgeControllerBlock.LIT)) {
            this.world.setBlockState(pos, currentBlockState.with(ForgeControllerBlock.LIT, true));
        } else if (fuel < 100 && currentBlockState.get(ForgeControllerBlock.LIT)) {
            this.world.setBlockState(pos, currentBlockState.with(ForgeControllerBlock.LIT, false));
        }

        final var recipeOptional = world.getRecipeManager().getFirstMatch(AlloyForgeRecipe.Type.INSTANCE, this, world);

        if (recipeOptional.isEmpty()) {
            this.currentSmeltTime = 0;
        } else {
            final var recipe = recipeOptional.get();
            if (recipe.getMinForgeTier() > forgeDefinition.forgeTier()) {
                this.currentSmeltTime = 0;
                return;
            }

            final var outputStack = this.getStack(10);
            final var recipeOutput = recipe.getOutput(forgeDefinition.forgeTier());

            if (!outputStack.isEmpty() && (!ItemOps.canStack(outputStack, recipeOutput) || outputStack.getCount() + recipeOutput.getCount() > outputStack.getMaxCount())) {
                this.currentSmeltTime = 0;
                return;
            }

            if (this.currentSmeltTime < forgeDefinition.maxSmeltTime()) {

                final float fuelRequirement = recipe.getFuelPerTick() * forgeDefinition.speedMultiplier();
                if (this.fuel - fuelRequirement < 0) {
                    this.currentSmeltTime = 0;
                    return;
                }

                this.currentSmeltTime += forgeDefinition.speedMultiplier();
                this.fuel -= fuelRequirement;

                if (world.random.nextDouble() > 0.75) {
                    ServerParticles.issueEvent((ServerWorld) world, pos, AlloyForgery.id("smelting_particles"), buf -> buf.writeVarInt(facing.ordinal()));
                }

            } else {

                for (int i = 0; i < 10; i++) {
                    if (!ItemOps.emptyAwareDecrement(this.ITEMS.get(i))) this.ITEMS.set(i, ItemStack.EMPTY);
                }

                if (outputStack.isEmpty()) {
                    this.setStack(10, recipeOutput);
                } else {
                    outputStack.increment(recipeOutput.getCount());
                }

                this.currentSmeltTime = 0;
                markDirty();
            }
        }

    }

    public boolean verifyMultiblock() {

        final BlockState belowController = world.getBlockState(multiblockPositions.get(0));
        if (!(belowController.isOf(Blocks.HOPPER) || forgeDefinition.isBlockValid(belowController.getBlock()))) return false;

        for (int i = 1; i < multiblockPositions.size(); i++) {
            if (!forgeDefinition.isBlockValid(world.getBlockState(multiblockPositions.get(i)).getBlock())) return false;
        }

        return true;
    }

    public static ImmutableList<BlockPos> generateMultiblockPositions(BlockPos controllerPos, Direction controllerFacing) {

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
        } else if (side == facing.rotateYCounterclockwise()) {
            return RIGHT_SLOTS;
        } else {
            return new int[0];
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == 11 ? ForgeFuelRegistry.hasFuel(stack.getItem()) : getStack(slot).isEmpty();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 10 || (slot == 11 && !ForgeFuelRegistry.hasFuel(stack.getItem()));
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container.alloy_forgery.forge_controller");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AlloyForgeScreenHandler(syncId, inv, this, PROPERTIES);
    }

    public static void ticker(World world, BlockPos blockPos, BlockState state, ForgeControllerBlockEntity controller) {
        controller.tick();
    }
}
