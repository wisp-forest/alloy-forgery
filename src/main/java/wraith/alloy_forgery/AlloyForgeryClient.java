package wraith.alloy_forgery;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import wraith.alloy_forgery.registry.CustomScreenRegistry;
import wraith.alloy_forgery.screens.AlloyForgerScreenHandler;
import wraith.alloy_forgery.utils.Utils;

public class AlloyForgeryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Utils.saveFilesFromJar("configs/textures", "textures", false);
        CustomScreenRegistry.registerScreens();
        registerPacketHandlers();
    }

    private void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("update_gui"), (client, handler, buf, responseSender) -> {
            NbtCompound tag = buf.readNbt();
            ScreenHandler screenHandler = client.player.currentScreenHandler;
            client.execute(() -> {
                if (screenHandler instanceof AlloyForgerScreenHandler) {
                    ((AlloyForgerScreenHandler) screenHandler).setHeat(tag.getInt("heat"));
                    ((AlloyForgerScreenHandler) screenHandler).setSmeltingTime(tag.getInt("smelting_time"));
                }
            });
        });
    }

}
