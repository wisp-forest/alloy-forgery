package io.wispforest.alloyforgery.neoforge;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.forges.ForgeDefinition;
import io.wispforest.alloyforgery.forges.ForgeTierDataLoader;
import io.wispforest.alloyforgery.neoforge.utils.NeoforgeGeneralPlatformUtils;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import io.wispforest.alloyforgery.utils.RecipeInjector;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

import java.util.Collection;

@Mod(value = AlloyForgery.MOD_ID)
public class AlloyForgeryNeoforge {
    private static IEventBus MOD_BUS = null;

    public AlloyForgeryNeoforge(IEventBus modBus, Dist dist) {
        MOD_BUS = modBus;

        AlloyForgery.LOGGER.info(AlloyForgery.MOD_ID + " is now loading!");

        NeoForge.EVENT_BUS.<AddServerReloadListenersEvent>addListener((event) -> {
            event.addListener(RecipeTagLoader.ID, RecipeTagLoader.INSTANCE);

            NeoforgeGeneralPlatformUtils.registerEndecDataLoaders(new NeoforgeGeneralPlatformUtils.ReloadListenerRegistration() {
                @Override
                public ResourceType getType() {
                    return ResourceType.SERVER_DATA;
                }

                @Override
                public DynamicRegistryManager getRegistry() {
                    return event.getRegistryAccess();
                }

                @Override
                public NeoforgeGeneralPlatformUtils.ReloadListenerRegistration addListener(Identifier id, ResourceReloader listener) {
                    event.addListener(id, listener);

                    return this;
                }

                @Override
                public NeoforgeGeneralPlatformUtils.ReloadListenerRegistration addDependency(Identifier id, Collection<Identifier> dependencies) {
                    for (var dependency : dependencies) {
                        event.addDependency(dependency, id);
                    }

                    return this;
                }
            });
        });

        ForgeDefinition.runDataLoaders();

        modBus.<FMLCommonSetupEvent>addListener(event -> {
            AlloyForgery.init();
        });

        NeoForge.EVENT_BUS.<ServerStartedEvent>addListener(event -> {
            RecipeInjector.injectRecipes(event.getServer());
        });

        modBus.<RegisterCapabilitiesEvent>addListener(capabilityEvent -> {
            capabilityEvent.registerBlockEntity(Capabilities.Fluid.BLOCK, ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY, (blockEntity, context) -> {
                return blockEntity.<FluidHolderImpl>getFluidHolder();
            });

            // TODO: UNABLE TO HAVE FALLBACK FOR CAPABILITY SIMILAR TO FABRIC SO MIXIN TIME!
//            capabilityEvent.registerBlock(Capabilities.ItemHandler.BLOCK, (world, pos, state, blockEntity, context) -> {
//                if (context == Direction.DOWN && world.getBlockEntity(pos.up()) instanceof ForgeControllerBlockEntity froge){
//                    return new SidedInvWrapper(froge, Direction.DOWN);
//                }
//
//                return null;
//            });

            capabilityEvent.registerBlockEntity(Capabilities.Item.BLOCK, ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY, WorldlyContainerWrapper::new);
        });

        modBus.<RegisterEvent>addListener(event -> {
            event.register(RegistryKeys.BLOCK_ENTITY_TYPE, helper -> AlloyForgery.registerBlockEntities());
            event.register(RegistryKeys.RECIPE_TYPE, helper -> AlloyForgery.registerRecipeTypes());
            event.register(RegistryKeys.RECIPE_SERIALIZER, helper -> AlloyForgery.registerRecipeSerializers());
            event.register(RegistryKeys.SCREEN_HANDLER, helper -> AlloyForgery.registerScreenHandlerType());
            event.register(RegistryKeys.SLOT_DISPLAY, helper -> AlloyForgery.registerSlotDisplays());
            event.register(RegistryKeys.ITEM_GROUP, helper -> AlloyForgery.registerItemGroup());
            event.register(RegistryKeys.BLOCK, helper -> NeoforgeGeneralPlatformUtils.handleLoadedEntries());
            event.register(RegistryKeys.ITEM, helper -> NeoforgeGeneralPlatformUtils.handleLoadedEntries());
        });

        NeoForge.EVENT_BUS.<OnDatapackSyncEvent>addListener(event -> {
            var packet = ForgeTierDataLoader.createSyncPacket();

            event.getRelevantPlayers().forEach(player -> {
                AlloyForgeNetworking.CHANNEL.serverHandle(player).send(packet);
            });
        });

        initEvents();
    }

    public void initEvents() {
        NeoForge.EVENT_BUS.<OnDatapackSyncEvent>addListener(event -> event.getRelevantPlayers().forEach(RecipeTagLoader.INSTANCE::sendPlayerPacketAfterDataLoad));
        NeoForge.EVENT_BUS.<ServerStartedEvent>addListener(event -> RecipeTagLoader.INSTANCE.onServerStarted(event.getServer()));

        NeoForge.EVENT_BUS.<PlayerEvent.PlayerLoggedInEvent>addListener(event -> RecipeTagLoader.INSTANCE.sendTagPacket((ServerPlayerEntity) event.getEntity()));

    }

    public IEventBus getBus() {
        if (MOD_BUS == null) throw new IllegalStateException("Unable to get the given IEventBus for the following mod: " + AlloyForgery.MOD_ID);

        return MOD_BUS;
    }
}
