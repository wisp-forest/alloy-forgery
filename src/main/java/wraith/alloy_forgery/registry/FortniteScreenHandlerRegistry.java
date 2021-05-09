package wraith.alloy_forgery.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import wraith.alloy_forgery.screens.AlloyForgerScreenHandler;
import wraith.alloy_forgery.utils.Utils;

import java.util.HashMap;

public class FortniteScreenHandlerRegistry {

    public static HashMap<String, ScreenHandlerType<? extends ScreenHandler>> SCREEN_HANDLERS = new HashMap<>();

    public static void registerScreenHandlers() {
        SCREEN_HANDLERS.put("alloy_forger", ScreenHandlerRegistry.registerExtended(Utils.ID("alloy_forger"), AlloyForgerScreenHandler::new));
    }

}
