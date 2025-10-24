package io.wispforest.alloyforgery.client;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;

public class AlloyForgeryClient {

    public static void init() {
        AlloyForgeNetworking.initClient();

        HandledScreens.register(AlloyForgery.ALLOY_FORGE_SCREEN_HANDLER_TYPE, AlloyForgeScreen::new);
    }
}
