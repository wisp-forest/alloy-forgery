package wraith.alloyforgery.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.data.RecipeTagLoader;

@Environment(EnvType.CLIENT)
public class AlloyForgeryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(AlloyForgery.ALLOY_FORGE_SCREEN_HANDLER_TYPE, AlloyForgeScreen::new);

        AlloyForgery.CHANNEL.registerClientbound(RecipeTagLoader.TagPacket.class, RecipeTagLoader.TagPacket::handlePacket);
    }

}
