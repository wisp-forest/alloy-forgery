package io.wispforest.alloyforgery.fabric.client;

import io.wispforest.alloyforgery.client.AlloyForgeryClient;
import net.fabricmc.api.ClientModInitializer;

public class AlloyForgeryClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AlloyForgeryClient.init();
    }
}
