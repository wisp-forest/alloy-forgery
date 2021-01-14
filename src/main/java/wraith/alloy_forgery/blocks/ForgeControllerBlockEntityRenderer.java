package wraith.alloy_forgery.blocks;

import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;

public class ForgeControllerBlockEntityRenderer extends BlockEntityRenderer<ForgeControllerBlockEntity> {

    public ForgeControllerBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(ForgeControllerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.isValidMultiblock()) {
            return;
        }
        BlockPos center;
        switch(entity.getCachedState().get(HorizontalFacingBlock.FACING).asString()) {
            case "north":
                center = entity.getPos().south();
                break;
            case "east":
                center = entity.getPos().west();
                break;
            case "south":
                center = entity.getPos().north();
                break;
            default:
                center = entity.getPos().east();
                break;
        }
        entity.getWorld().addImportantParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.getX() + 0.25, center.getY(), center.getZ() + 0.25, 0, 0.08, 0);
        entity.getWorld().addImportantParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.getX() + 0.75, center.getY(), center.getZ() + 0.25, 0, 0.08, 0);
        entity.getWorld().addImportantParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.getX() + 0.25, center.getY(), center.getZ() + 0.75, 0, 0.08, 0);
        entity.getWorld().addImportantParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.getX() + 0.75, center.getY(), center.getZ() + 0.75, 0, 0.08, 0);
        entity.getWorld().addImportantParticle(ParticleTypes.LARGE_SMOKE, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0, 0.05, 0);
        entity.getWorld().addImportantParticle(ParticleTypes.SMOKE, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0, 0.05, 0);
    }

}
