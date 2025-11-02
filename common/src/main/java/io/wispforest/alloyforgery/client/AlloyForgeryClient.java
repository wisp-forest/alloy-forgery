package io.wispforest.alloyforgery.client;

import io.wispforest.alloyforgery.AlloyForgeScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;

public class AlloyForgeryClient {

    public static void init() {
        AlloyForgeNetworking.initClient();

        HandledScreens.register(AlloyForgeScreenHandler.ALLOY_FORGE_SCREEN_HANDLER_TYPE, AlloyForgeScreen::new);
    }
}
