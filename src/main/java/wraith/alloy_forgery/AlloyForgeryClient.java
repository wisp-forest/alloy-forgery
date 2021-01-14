package wraith.alloy_forgery;

import net.fabricmc.api.ClientModInitializer;
import wraith.alloy_forgery.registry.BlockEntityRendererRegistry;
import wraith.alloy_forgery.registry.ScreenRegistry;
import wraith.alloy_forgery.utils.Utils;

public class AlloyForgeryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Utils.saveFilesFromJar("configs/textures", "textures", true);
        ScreenRegistry.registerScreens();
        BlockEntityRendererRegistry.RegisterBlockEntityRenderers();
    }
}
