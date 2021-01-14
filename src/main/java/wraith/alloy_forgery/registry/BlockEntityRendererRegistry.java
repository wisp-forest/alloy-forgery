package wraith.alloy_forgery.registry;

import wraith.alloy_forgery.blocks.ForgeControllerBlockEntityRenderer;

public class BlockEntityRendererRegistry {

    public static void RegisterBlockEntityRenderers() {
        net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry.INSTANCE.register(BlockEntityRegistry.FORGE_CONTROLLER, ForgeControllerBlockEntityRenderer::new);
    }

}