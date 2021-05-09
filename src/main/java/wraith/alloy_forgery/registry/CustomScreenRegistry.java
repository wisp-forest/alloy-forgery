package wraith.alloy_forgery.registry;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import wraith.alloy_forgery.screens.AlloyForgerScreen;

public class CustomScreenRegistry {

    public static void registerScreens() {
        ScreenRegistry.register(CustomScreenHandlerRegistry.SCREEN_HANDLERS.get("alloy_forger"), AlloyForgerScreen::new);
    }

}
