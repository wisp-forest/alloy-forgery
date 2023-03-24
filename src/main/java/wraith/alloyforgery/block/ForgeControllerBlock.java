package wraith.alloyforgery.block;

import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.forges.ForgeDefinition;
import wraith.alloyforgery.forges.ForgeFuelRegistry;

public class ForgeControllerBlock extends BlockWithEntity {

    public static final BooleanProperty LIT = Properties.LIT;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public final ForgeDefinition forgeDefinition;

    public ForgeControllerBlock(ForgeDefinition forgeDefinition) {
        super(FabricBlockSettings.copyOf(Blocks.BLACKSTONE));
        this.forgeDefinition = forgeDefinition;
        this.setDefaultState(this.getStateManager().getDefaultState().with(LIT, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            final var playerStack = player.getStackInHand(hand);

            final var fuelDefinition = ForgeFuelRegistry.getFuelForItem(playerStack.getItem());
            if (!(world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity controller)) return ActionResult.PASS;

            if (fuelDefinition.hasReturnType() && controller.canAddFuel(fuelDefinition.fuel())) {
                if (!player.getAbilities().creativeMode) {
                    player.getStackInHand(hand).decrement(1);
                    player.getInventory().offerOrDrop(new ItemStack(fuelDefinition.returnType()));
                }
                controller.addFuel(fuelDefinition.fuel());
            } else {
                if (!controller.verifyMultiblock()) {
                    player.sendMessage(Text.translatable("message.alloy_forgery.invalid_multiblock").formatted(Formatting.GRAY), true);
                    return ActionResult.SUCCESS;
                }

                final var screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
                if (screenHandlerFactory != null) {
                    player.openHandledScreen(screenHandlerFactory);
                }
            }

        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity forgeController) {
                ItemScatterer.spawn(world, pos, forgeController);
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), forgeController.getFuelStack());
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(LIT)) return;

        final BlockPos center = pos.offset(state.get(FACING).getOpposite());

        ClientParticles.setParticleCount(2);
        ClientParticles.setVelocity(new Vec3d(0, 0.1, 0));
        ClientParticles.spawnWithinBlock(ParticleTypes.CAMPFIRE_COSY_SMOKE, world, center);

        ClientParticles.setParticleCount(5);
        ClientParticles.setVelocity(new Vec3d(0, 0.1, 0));
        ClientParticles.spawnWithinBlock(ParticleTypes.LARGE_SMOKE, world, center);

        if (random.nextDouble() > 0.65) {
            ClientParticles.setParticleCount(1);
            ClientParticles.setVelocity(new Vec3d(0, 0.01, 0));
            ClientParticles.spawnWithinBlock(ParticleTypes.CAMPFIRE_COSY_SMOKE, world, center);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, AlloyForgery.FORGE_CONTROLLER_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick());
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity controller)) return 0;
        return controller.getCurrentSmeltTime() == 0 ? 0 : Math.max(1, Math.round(controller.getSmeltProgress() * 0.46875f));
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ForgeControllerBlockEntity(pos, state);
    }
}
