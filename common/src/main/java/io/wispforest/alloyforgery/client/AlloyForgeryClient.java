package io.wispforest.alloyforgery.client;

import io.wispforest.alloyforgery.AlloyForgeScreenHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;

public class AlloyForgeryClient {

    public static void init() {
        AlloyForgeNetworking.initClient();

        MenuScreens.register(AlloyForgeScreenHandler.ALLOY_FORGE_SCREEN_HANDLER_TYPE, AlloyForgeScreen::new);
    }
}
