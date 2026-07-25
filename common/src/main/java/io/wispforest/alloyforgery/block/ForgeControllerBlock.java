package io.wispforest.alloyforgery.block;

import com.mojang.serialization.MapCodec;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.util.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import io.wispforest.alloyforgery.forges.ForgeFuelDataLoader;

public class ForgeControllerBlock extends BaseEntityBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public final Identifier forgeDefinitionId;

    public ForgeControllerBlock(Identifier forgeDefinitionId, Properties settings) {
        super(settings);

        this.forgeDefinitionId = forgeDefinitionId;
        this.registerDefaultState(this.getStateDefinition().any().setValue(LIT, false));
    }

    public static ForgeControllerBlock of(Identifier forgeDefinitionId, Identifier blockId) {
        return new ForgeControllerBlock(forgeDefinitionId, Properties.ofFullCopy(Blocks.BLACKSTONE).setId(ResourceKey.create(Registries.BLOCK, blockId)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CodecUtils.toMapCodec(
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.fieldOf("forge_definition", s -> forgeDefinitionId),
                CodecUtils.toEndec(BlockBehaviour.Properties.CODEC).fieldOf("properties", BlockBehaviour::properties),
                ForgeControllerBlock::new
            )
        );
    }

    @Override
    protected InteractionResult useItemOn(ItemStack playerStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity controller)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!controller.verifyMultiblock()) {
            player.sendOverlayMessage(AlloyForgery.translation("message", "invalid_multiblock").withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }

        final var fuelDefinition = ForgeFuelDataLoader.getFuelForItem(playerStack.getItem());

        if (fuelDefinition.hasReturnType() && controller.canAddFuel(fuelDefinition)) {
            var returnStack = new ItemStack(fuelDefinition.returnType());
            controller.addFuel(fuelDefinition.fuel());
            if (!player.getAbilities().instabuild) {
                playerStack.shrink(1);
                player.getInventory().placeItemBackInInventory(returnStack);
                return InteractionResult.SUCCESS.heldItemTransformedTo(returnStack);
            }
        } else if (!GeneralPlatformUtils.INSTANCE.interactWithFluidStorage(controller, player, hand)) {
            final var screenHandlerFactory = state.getMenuProvider(world, pos);
            if (screenHandlerFactory != null) {
                GeneralPlatformUtils.INSTANCE.openHandledScreen(player, controller, screenHandlerFactory);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity forgeController) {
            Containers.dropContents(world, pos, forgeController);
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), forgeController.getFuelStack());
        }

        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    //@Environment(EnvType.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        final BlockPos center = pos.relative(state.getValue(FACING).getOpposite());

        ClientParticles.setParticleCount(2);
        ClientParticles.setVelocity(new Vec3(0, 0.1, 0));
        ClientParticles.spawnWithinBlock(ParticleTypes.CAMPFIRE_COSY_SMOKE, world, center);

        ClientParticles.setParticleCount(5);
        ClientParticles.setVelocity(new Vec3(0, 0.1, 0));
        ClientParticles.spawnWithinBlock(ParticleTypes.LARGE_SMOKE, world, center);

        if (random.nextDouble() > 0.65) {
            ClientParticles.setParticleCount(1);
            ClientParticles.setVelocity(new Vec3(0, 0.01, 0));
            ClientParticles.spawnWithinBlock(ParticleTypes.CAMPFIRE_COSY_SMOKE, world, center);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? null : createTickerHelper(type, ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        return world.getBlockEntity(pos, ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY)
            .map(ForgeControllerBlockEntity::getCompartorOutput)
            .orElse(0);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ForgeControllerBlockEntity(pos, state);
    }
}
