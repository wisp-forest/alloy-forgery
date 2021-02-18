package wraith.alloy_forgery;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.alloy_forgery.registry.BlockEntityRegistry;
import wraith.alloy_forgery.registry.BlockRegistry;
import wraith.alloy_forgery.registry.ItemRegistry;
import wraith.alloy_forgery.registry.ScreenHandlerRegistry;
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

        ScreenHandlerRegistry.registerScreenHandlers();

        registerEvents();

        LOGGER.info("[Alloy Forgery] has been initiated.");
    }

    private void registerEvents() {
        //TODO - FIX LOOT TABLE FOR THE FORGE
        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, supplier, setter) -> {
            if (id.getNamespace().contains(MOD_ID)) {
                FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
                        .rolls(ConstantLootTableRange.create(1))
                        .withCondition(SurvivesExplosionLootCondition.builder().build())
                        .withEntry(ItemEntry.builder(ItemRegistry.ITEMS.get(id.toString())).build());
                supplier.withPool(poolBuilder.build());
            }
        });
    }

}
