package wraith.alloy_forgery.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.alloy_forgery.Forge;

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
    public @Nullable BlockEntity createBlockEntity(BlockView world) {
        return new ForgeControllerBlockEntity();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (!(world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity)) {
                return ActionResult.FAIL;
            }
            ForgeControllerBlockEntity entity = (ForgeControllerBlockEntity) world.getBlockEntity(pos);

            String id = Registry.BLOCK.getId(this).getPath();
            Forge forge = Forge.FORGES.getOrDefault(id, null);

            entity.setMaxHeat(forge.maxHeat);
            if (player.getStackInHand(hand).getItem() == Items.LAVA_BUCKET) {
                if (entity.increaseHeat(10000) && !player.isCreative()) {
                    player.setStackInHand(hand, new ItemStack(Items.BUCKET));
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
        world.getBlockEntity(pos).markDirty();
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity) {
            ForgeControllerBlockEntity entity = (ForgeControllerBlockEntity) world.getBlockEntity(pos);

            String id = Registry.BLOCK.getId(this).getPath();
            Forge forge = Forge.FORGES.getOrDefault(id, null);

            entity.setMaxHeat(forge.maxHeat);
        }
    }
}
