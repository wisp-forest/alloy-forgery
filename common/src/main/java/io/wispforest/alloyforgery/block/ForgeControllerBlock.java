package io.wispforest.alloyforgery.block;

import com.mojang.serialization.MapCodec;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import io.wispforest.alloyforgery.forges.ForgeFuelDataLoader;

public class ForgeControllerBlock extends BlockWithEntity {

    public static final BooleanProperty LIT = Properties.LIT;
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public final Identifier forgeDefinitionId;

    public ForgeControllerBlock(Identifier forgeDefinitionId, Settings settings) {
        super(settings);

        this.forgeDefinitionId = forgeDefinitionId;
        this.setDefaultState(this.getStateManager().getDefaultState().with(LIT, false));
    }

    public static ForgeControllerBlock of(Identifier forgeDefinitionId, Identifier blockId) {
        return new ForgeControllerBlock(forgeDefinitionId, Settings.copy(Blocks.BLACKSTONE).registryKey(RegistryKey.of(RegistryKeys.BLOCK, blockId)));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CodecUtils.toMapCodec(
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.fieldOf("forge_definition", s -> forgeDefinitionId),
                CodecUtils.toEndec(AbstractBlock.Settings.CODEC).fieldOf("properties", AbstractBlock::getSettings),
                ForgeControllerBlock::new
            )
        );
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack playerStack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (!(world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity controller)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }

        if (!controller.verifyMultiblock()) {
            player.sendMessage(AlloyForgery.translation("message", "invalid_multiblock").formatted(Formatting.GRAY), true);
            return ActionResult.SUCCESS;
        }

        final var fuelDefinition = ForgeFuelDataLoader.getFuelForItem(playerStack.getItem());

        if (fuelDefinition.hasReturnType() && controller.canAddFuel(fuelDefinition)) {
            var returnStack = new ItemStack(fuelDefinition.returnType());
            controller.addFuel(fuelDefinition.fuel());
            if (!player.getAbilities().creativeMode) {
                playerStack.decrement(1);
                player.getInventory().offerOrDrop(returnStack);
                return ActionResult.SUCCESS.withNewHandStack(returnStack);
            }
        } else if (!GeneralPlatformUtils.INSTANCE.interactWithFluidStorage(controller, player, hand)) {
            final var screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (screenHandlerFactory != null) {
                GeneralPlatformUtils.INSTANCE.openHandledScreen(player, controller, screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() == newState.getBlock()) return;

        if (world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity forgeController) {
            ItemScatterer.spawn(world, pos, forgeController);
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), forgeController.getFuelStack());
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    //@Environment(EnvType.CLIENT)
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
        return world.isClient ? null : validateTicker(type, ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick());
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos, ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY)
            .map(ForgeControllerBlockEntity::getCompartorOutput)
            .orElse(0);
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
