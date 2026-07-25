package io.wispforest.alloyforgery.neoforge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.forges.ForgeDefinition;
import io.wispforest.alloyforgery.forges.ForgeTierDataLoader;
import io.wispforest.alloyforgery.neoforge.utils.NeoforgeGeneralPlatformUtils;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import io.wispforest.alloyforgery.utils.RecipeInjector;
import io.wispforest.owo.Owo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@Mod(value = "alloy_forgery")
public class AlloyForgeryNeoforge {
    private static IEventBus MOD_BUS = null;

    public AlloyForgeryNeoforge(IEventBus modBus, Dist dist) {
        MOD_BUS = modBus;

        AlloyForgery.LOGGER.info(AlloyForgery.MOD_ID + " is now loading!");

        NeoForge.EVENT_BUS.<AddServerReloadListenersEvent>addListener((event) -> {
            event.addListener(RecipeTagLoader.ID, RecipeTagLoader.INSTANCE);

            NeoforgeGeneralPlatformUtils.registerEndecDataLoaders(new NeoforgeGeneralPlatformUtils.ReloadListenerRegistration() {
                @Override
                public PackType getType() {
                    return PackType.SERVER_DATA;
                }

                @Override
                public RegistryAccess getRegistry() {
                    return event.getRegistryAccess();
                }

                @Override
                public NeoforgeGeneralPlatformUtils.ReloadListenerRegistration addListener(ResourceLocation id, PreparableReloadListener listener) {
                    event.addListener(id, listener);

                    return this;
                }

                @Override
                public NeoforgeGeneralPlatformUtils.ReloadListenerRegistration addDependency(ResourceLocation id, Collection<ResourceLocation> dependencies) {
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
            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> AlloyForgery.registerBlockEntities());
            event.register(Registries.RECIPE_TYPE, helper -> AlloyForgery.registerRecipeTypes());
            event.register(Registries.RECIPE_SERIALIZER, helper -> AlloyForgery.registerRecipeSerializers());
            event.register(Registries.MENU, helper -> AlloyForgery.registerScreenHandlerType());
            event.register(Registries.SLOT_DISPLAY, helper -> AlloyForgery.registerSlotDisplays());
            event.register(Registries.CREATIVE_MODE_TAB, helper -> AlloyForgery.registerItemGroup());
            event.register(Registries.BLOCK, helper -> NeoforgeGeneralPlatformUtils.handleLoadedEntries());
            event.register(Registries.ITEM, helper -> NeoforgeGeneralPlatformUtils.handleLoadedEntries());
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

        NeoForge.EVENT_BUS.<PlayerEvent.PlayerLoggedInEvent>addListener(event -> RecipeTagLoader.INSTANCE.sendTagPacket((ServerPlayer) event.getEntity()));

    }

    public IEventBus getBus() {
        if (MOD_BUS == null) throw new IllegalStateException("Unable to get the given IEventBus for the following mod: " + AlloyForgery.MOD_ID);

        return MOD_BUS;
    }
}
