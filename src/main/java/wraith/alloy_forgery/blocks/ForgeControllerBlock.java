package wraith.alloy_forgery.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import wraith.alloy_forgery.Forge;
import wraith.alloy_forgery.ForgeFuel;
import wraith.alloy_forgery.api.ForgeFuels;
import wraith.alloy_forgery.api.Forges;
import wraith.alloy_forgery.registry.BlockEntityRegistry;

import java.util.Objects;

public class ForgeControllerBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public ForgeControllerBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    @Nullable
    // Changed from BlockView to BlockPos + BlockState
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ForgeControllerBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (!(world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity entity)) {
                return ActionResult.FAIL;
            }

            String id = Registry.BLOCK.getId(this).getPath();
            Forge forge = Forges.getForge(id);

            entity.setMaxHeat(forge.maxHeat);

            String heldItemID = Registry.ITEM.getId(player.getStackInHand(hand).getItem()).toString();
            ForgeFuel fuel = ForgeFuels.FUELS.getOrDefault(heldItemID, null);
            if (fuel != null && fuel.isRightclickable()) {
                if (entity.isValidMultiblock() && entity.increaseHeat(fuel.getCookTime()) && !player.isCreative()) {
                    player.getStackInHand(hand).decrement(1);
                    if (fuel.hasReturnableItem()) {
                        // No longer specifies world
                        player.getInventory().offerOrDrop(new ItemStack(Registry.ITEM.get(new Identifier(fuel.getReturnableItem()))));
                    }
                } else {
                    return ActionResult.FAIL;
                }
            } else {
                NamedScreenHandlerFactory screenHandlerFactory = this.createScreenHandlerFactory(state, world, pos);
                if (screenHandlerFactory != null) {
                    player.openHandledScreen((NamedScreenHandlerFactory) (world.getBlockEntity(pos)));
                } else {
                    return ActionResult.FAIL;
                }
            }
        }
        Objects.requireNonNull(world.getBlockEntity(pos)).markDirty();
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity entity) {

            String id = Registry.BLOCK.getId(this).getPath();
            Forge forge = Forges.getForge(id);

            entity.setMaxHeat(forge.maxHeat);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ForgeControllerBlockEntity) {
            if(!player.isCreative()) {
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Registry.ITEM.get(Registry.BLOCK.getId(this)))));
            }
            ItemScatterer.spawn(world, pos, (ForgeControllerBlockEntity)blockEntity);
            world.updateComparators(pos,this);
            super.onBreak(world, pos, state, player);
        }
    }

    // New methods implemented from BlockEntity
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BlockEntityRegistry.FORGE_CONTROLLER, ForgeControllerBlockEntity::ticker);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getGameEventListener(World world, T blockEntity) {
        return super.getGameEventListener(world, blockEntity);
    }
}
