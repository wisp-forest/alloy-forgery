package wraith.alloy_forgery;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.alloy_forgery.registry.BlockEntityRegistry;
import wraith.alloy_forgery.registry.ItemRegistry;
import wraith.alloy_forgery.registry.BlockRegistry;
import wraith.alloy_forgery.registry.ScreenHandlerRegistry;
import wraith.alloy_forgery.utils.Config;
import wraith.alloy_forgery.utils.Utils;

import java.io.File;

public class AlloyForgery implements ModInitializer {

    public static final String MOD_ID = "alloy_forgery";
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        Utils.saveFilesFromJar("configs/", "", true);
        Forge.readSmeltriesFromJson(Config.getJsonObject(Config.readFile(new File("config/alloy_forgery/smelteries.json"))));
        Forge.readMaterialsFromJson(Config.getJsonObject(Config.readFile(new File("config/alloy_forgery/material_worth.json"))));
        Forge.readRecipesFromJson(Config.getJsonObject(Config.readFile(new File("config/alloy_forgery/recipes.json"))));

        BlockRegistry.loadBlocks();
        BlockRegistry.registerBlocks();

        ItemRegistry.loadItems();
        ItemRegistry.registerItems();

        BlockEntityRegistry.loadBlockEntities();
        BlockEntityRegistry.registerBlockEntities();

        ScreenHandlerRegistry.registerScreenHandlers();
        LOGGER.info("[Alloy Forgery] has been initiated.");
    }

}
