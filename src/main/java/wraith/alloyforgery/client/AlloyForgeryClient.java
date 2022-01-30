package wraith.alloyforgery.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import wraith.alloyforgery.AlloyForgery;

@Environment(EnvType.CLIENT)
public class AlloyForgeryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(AlloyForgery.ALLOY_FORGE_SCREEN_HANDLER_TYPE, AlloyForgeScreen::new);
    }

}
