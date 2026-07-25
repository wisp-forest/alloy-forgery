package io.wispforest.alloyforgery.neoforge.client;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.client.AlloyForgeryClient;
import io.wispforest.alloyforgery.neoforge.data.AlloyForgeryData;
import io.wispforest.alloyforgery.neoforge.utils.NeoforgeGeneralPlatformUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collection;

@Mod(value = "alloy_forgery", dist = Dist.CLIENT)
public class AlloyForgeryClientNeoforgeInit {
    public AlloyForgeryClientNeoforgeInit(IEventBus modBus) {
        modBus.<FMLCommonSetupEvent>addListener(event -> {
            AlloyForgeryClient.init();
        });

        modBus.addListener(AlloyForgeryData::onInitializeDataGenerator);

        modBus.<AddClientReloadListenersEvent>addListener(event -> {
            NeoforgeGeneralPlatformUtils.registerEndecDataLoaders(new NeoforgeGeneralPlatformUtils.ReloadListenerRegistration() {
                @Override
                public PackType getType() {
                    return PackType.CLIENT_RESOURCES;
                }

                @Override
                public NeoforgeGeneralPlatformUtils.ReloadListenerRegistration addListener(Identifier id, PreparableReloadListener listener) {
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
    }
}
