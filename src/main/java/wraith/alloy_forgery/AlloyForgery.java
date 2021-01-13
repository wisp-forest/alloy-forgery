package wraith.alloy_forgery;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.alloy_forgery.registry.ModRegistry;
import wraith.alloy_forgery.registry.ScreenHandlerRegistry;

public class AlloyForgery implements ModInitializer {

    public static final String MOD_ID = "alloy_forgery";
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        // Register all blocks and items
        ModRegistry.loadBlocks();
        ModRegistry.loadItems();
        // Registry
        ModRegistry.registerBlocks();
        ModRegistry.registerItems();
        ScreenHandlerRegistry.registerScreenHandlers();
        LOGGER.info("[Alloy Forgery] has been initiated.");
    }

}
