package wraith.alloyforgery.networking;

import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.forges.ForgeTier;
import wraith.alloyforgery.forges.ForgeTierRegistry;

import java.util.Map;

public record TierDataSync(Map<Identifier, ForgeTier> idToForgeTier, Map<Identifier, Identifier> forgeDefinitionToTier) {

    @Environment(EnvType.CLIENT)
    public static void handlePacket(TierDataSync packet, ClientAccess access) {
        var tier = ForgeTierRegistry.getForgeRegistry(true);

        tier.setTierInfo(packet.idToForgeTier());
        tier.forgeDefinitionBindings(packet.forgeDefinitionToTier());
    }
}
