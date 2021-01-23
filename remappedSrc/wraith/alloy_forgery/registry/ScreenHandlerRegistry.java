package wraith.alloy_forgery.registry;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.screens.AlloyForgerScreenHandler;
import wraith.alloy_forgery.utils.Utils;

import java.util.HashMap;

public class ScreenHandlerRegistry {

    public static HashMap<String, ScreenHandlerType<? extends ScreenHandler>> SCREEN_HANDLERS = new HashMap<>();

    public static void registerScreenHandlers() {
        SCREEN_HANDLERS.put("alloy_forger", net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.registerSimple(Utils.ID("alloy_forger"), AlloyForgerScreenHandler::new));
    }

}
