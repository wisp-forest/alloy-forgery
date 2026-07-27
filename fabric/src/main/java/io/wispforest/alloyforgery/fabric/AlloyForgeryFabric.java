package io.wispforest.alloyforgery.fabric;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.forges.ForgeDefinition;
import io.wispforest.alloyforgery.forges.ForgeTierDataLoader;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import io.wispforest.alloyforgery.utils.RecipeInjector;
import io.wispforest.owo.util.OwoFreezer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.PackType;

public class AlloyForgeryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AlloyForgery.registerBlockEntities();
        AlloyForgery.registerRecipeTypes();
        AlloyForgery.registerRecipeSerializers();
        AlloyForgery.registerScreenHandlerType();
        AlloyForgery.registerSlotDisplays();
        AlloyForgery.registerItemGroup();

        ForgeDefinition.runDataLoaders();

        AlloyForgery.init();

        ServerLifecycleEvents.SERVER_STARTED.register(RecipeInjector::injectRecipes);

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(RecipeTagLoader.ID, RecipeTagLoader.INSTANCE);

        ItemStorage.SIDED.registerFallback((level, pos, state, blockEntity, context) -> {
            if (context == Direction.DOWN && level.getBlockEntity(pos.above()) instanceof ForgeControllerBlockEntity froge){
                return ItemStorage.SIDED.find(level, pos, state, froge, Direction.DOWN);
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
                ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY
            );
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            AlloyForgeNetworking.CHANNEL.serverHandle(player).send(ForgeTierDataLoader.createSyncPacket());
        });

        initEvents();
    }

    public void initEvents() {
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> RecipeTagLoader.INSTANCE.sendPlayerPacketAfterDataLoad(player));
        ServerLifecycleEvents.SERVER_STARTED.register(RecipeTagLoader.INSTANCE::onServerStarted);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> RecipeTagLoader.INSTANCE.sendTagPacket(handler.player));
    }
}
