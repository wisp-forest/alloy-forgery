package wraith.alloy_forgery;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.alloy_forgery.registry.BlockEntityRegistry;
import wraith.alloy_forgery.registry.BlockRegistry;
import wraith.alloy_forgery.registry.ItemRegistry;
import wraith.alloy_forgery.registry.FortniteScreenHandlerRegistry;
import wraith.alloy_forgery.utils.Config;

public class AlloyForgery implements ModInitializer {

    public static final String MOD_ID = "alloy_forgery";
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        if (!Config.isLoaded()) {
            Config.init();
        }

        BlockRegistry.loadBlocks();
        BlockRegistry.registerBlocks();

        ItemRegistry.loadItems();
        ItemRegistry.registerItems();

        BlockEntityRegistry.loadBlockEntities();
        BlockEntityRegistry.registerBlockEntities();

        FortniteScreenHandlerRegistry.registerScreenHandlers();

        LOGGER.info("[Alloy Forgery] has been initiated.");
    }
}
