package wraith.alloy_forgery.registry;

import wraith.alloy_forgery.screens.AlloyForgerScreen;

public class ScreenRegistry {

    public static void registerScreens() {
        net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry.register(ScreenHandlerRegistry.SCREEN_HANDLERS.get("alloy_forger"), AlloyForgerScreen::new);
    }

}
