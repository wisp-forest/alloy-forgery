package io.wispforest.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleTestModFabricInit implements ModInitializer {

    public static final String MODID = "example_test_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info(MODID + " is now loading!");
    }
}
