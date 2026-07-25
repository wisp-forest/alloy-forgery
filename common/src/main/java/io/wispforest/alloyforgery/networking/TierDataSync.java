package io.wispforest.alloyforgery.networking;

import io.wispforest.owo.network.ClientAccess;
import net.minecraft.resources.ResourceLocation;
import io.wispforest.alloyforgery.forges.ForgeTier;
import io.wispforest.alloyforgery.forges.ForgeTierDataLoader;
import java.util.Map;

public record TierDataSync(Map<ResourceLocation, ForgeTier> idToForgeTier, Map<ResourceLocation, ResourceLocation> forgeDefinitionToTier) {

    //@Environment(EnvType.CLIENT)
    public static void handlePacket(TierDataSync packet, ClientAccess access) {
        ForgeTierDataLoader.getForgeRegistry(true)
            .setTierData(packet.idToForgeTier(), packet.forgeDefinitionToTier());
    }
}
