package io.wispforest.alloyforgery.fabric;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.fabric.data.IdentifiableResourceReloadListenerImpl;
import io.wispforest.alloyforgery.forges.ForgeTierDataLoader;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import io.wispforest.alloyforgery.networking.TierDataSync;
import io.wispforest.alloyforgery.utils.DataPackEvents;
import io.wispforest.alloyforgery.utils.RecipeInjector;
import io.wispforest.owo.util.OwoFreezer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.Item;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.math.Direction;

import java.util.Iterator;

public class AlloyForgeryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AlloyForgery.init();

        ServerLifecycleEvents.SERVER_STARTED.register(RecipeInjector::injectRecipes);

        DataPackEvents.BEFORE_SYNC.register(RecipeInjector::injectRecipes);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
            .registerReloadListener(new IdentifiableResourceReloadListenerImpl(RecipeTagLoader.ID, RecipeTagLoader.INSTANCE));

        ItemStorage.SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
            if (context == Direction.DOWN && world.getBlockEntity(pos.up()) instanceof ForgeControllerBlockEntity froge){
                return InventoryStorage.of(froge, Direction.DOWN);
            }

            return null;
        });

        OwoFreezer.registerFreezeCallback(() -> {
            FluidStorage.SIDED.registerForBlockEntities(
                (blockEntity, context) -> {
                    return (blockEntity instanceof ForgeControllerBlockEntity be)
                        ? be.<FluidHolderImpl>getFluidHolder()
                        : null;
                },
                AlloyForgery.FORGE_CONTROLLER_BLOCK_ENTITY
            );
        });

        AlloyForgery.registerBlockEntities();
        AlloyForgery.registerRecipeTypes();
        AlloyForgery.registerRecipeSerializers();
        AlloyForgery.registerScreenHandlerType();
        AlloyForgery.registerSlotDisplays();

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            AlloyForgeNetworking.CHANNEL.serverHandle(player).send(ForgeTierDataLoader.createSyncPacket());
        });

        initEvents();
    }

    public void initEvents() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(RecipeTagLoader.INSTANCE::endDataPackReload);
        ServerLifecycleEvents.SERVER_STARTED.register(RecipeTagLoader.INSTANCE::onServerStarted);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> RecipeTagLoader.INSTANCE.sendTagPacket(handler.player));
    }
}
