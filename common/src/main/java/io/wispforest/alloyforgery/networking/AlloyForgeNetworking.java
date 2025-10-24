package io.wispforest.alloyforgery.networking;

import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.forges.ForgeTier;

public class AlloyForgeNetworking {
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(AlloyForgery.id("main"));

    public static void init() {
        CHANNEL.addEndecs(builder -> builder.register(ForgeTier.ENDEC, ForgeTier.class));

        CHANNEL.registerClientboundDeferred(RecipeTagLoader.TagPacket.class);
        CHANNEL.registerClientboundDeferred(TierDataSync.class);

        CHANNEL.registerServerbound(DisableSlotToggle.class, DisableSlotToggle.ENDEC, DisableSlotToggle::handle);
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        CHANNEL.registerClientbound(RecipeTagLoader.TagPacket.class, RecipeTagLoader.TagPacket::handlePacket);
        CHANNEL.registerClientbound(TierDataSync.class, TierDataSync::handlePacket);
    }
}
