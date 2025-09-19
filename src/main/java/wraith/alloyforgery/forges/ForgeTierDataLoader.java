package wraith.alloyforgery.forges;

import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.networking.AlloyForgeNetworking;
import wraith.alloyforgery.networking.TierDataSync;
import wraith.alloyforgery.utils.data.EndecDataLoader;

import java.util.*;

public class ForgeTierDataLoader {

    private static final ForgeTierDataLoader SERVER = new ForgeTierDataLoader();
    private static final ForgeTierDataLoader CLIENT = new ForgeTierDataLoader();

    private static final EndecDataLoader<ForgeTier> TIER_DATA_LOADER = new EndecDataLoader<>(
            AlloyForgery.id("forge_tier"),
            "alloy_forge/tier",
            ForgeTier.ENDEC,
            ResourceType.SERVER_DATA) {
        @Override
        protected void apply(Map<Identifier, ForgeTier> prepared, ResourceManager manager, Profiler profiler) {
            prepared.forEach((identifier, forgeTier) -> {
                SERVER.idToForgeTier.put(identifier, forgeTier);
                SERVER.forgeTierToId.put(forgeTier, identifier);
            });
        }
    };

    private static final EndecDataLoader<Map<Identifier, Identifier>> TIER_BINDING_LOADER = new EndecDataLoader<>(
            AlloyForgery.id("forge_tier"),
            "alloy_forge/tier",
            Endec.map(Identifier::toString, Identifier::tryParse, MinecraftEndecs.IDENTIFIER),
            ResourceType.SERVER_DATA) {
        @Override
        protected void apply(Map<Identifier, Map<Identifier, Identifier>> prepared, ResourceManager manager, Profiler profiler) {
            prepared.forEach((identifier, bindings) -> bindings.forEach(SERVER.forgeDefinitionToTier::putIfAbsent));
        }
    };

    public static void initDataLoaders() {
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            AlloyForgeNetworking.CHANNEL.serverHandle(player).send(new TierDataSync(SERVER.idToForgeTier(), SERVER.forgeDefinitionToTier()));
        });
    }

    private final Map<Identifier, ForgeTier> idToForgeTier = new HashMap<>();
    private final Map<ForgeTier, Identifier> forgeTierToId = new HashMap<>();

    private final Map<Identifier, Identifier> forgeDefinitionToTier = new HashMap<>();

    public static ForgeTierDataLoader getForgeRegistry(boolean isClientSide) {
        return (isClientSide) ? CLIENT : SERVER;
    }

    @Nullable
    public static Identifier getForgeTierId(boolean isClientSide, ForgeTier forgeTier) {
        return getForgeRegistry(isClientSide).forgeTierToId().get(forgeTier);
    }

    @Nullable
    public static ForgeTier getForgeTier(boolean isClientSide, Identifier tierId) {
        return getForgeRegistry(isClientSide).idToForgeTier().get(tierId);
    }

    @Nullable
    public ForgeTier getForgeTier(ForgeDefinition forgeDefinition) {
        return forgeDefinition.id()
            .map(forgeDefinitionToTier()::get)
            .map(idToForgeTier()::get)
            .orElse(null);
    }

    public Map<Identifier, ForgeTier> idToForgeTier() {
        return Collections.unmodifiableMap(idToForgeTier);
    }

    public Map<ForgeTier, Identifier> forgeTierToId() {
        return Collections.unmodifiableMap(forgeTierToId);
    }

    public Map<Identifier, Identifier> forgeDefinitionToTier() {
        return Collections.unmodifiableMap(forgeDefinitionToTier);
    }

    @ApiStatus.Internal
    public void forgeDefinitionBindings(Map<Identifier, Identifier> forgeDefinitionToTier) {
        this.forgeDefinitionToTier.clear();
        this.forgeDefinitionToTier.putAll(forgeDefinitionToTier);
    }

    @ApiStatus.Internal
    public void setTierInfo(Map<Identifier, ForgeTier> idToForgeTier) {
        this.idToForgeTier.clear();
        this.idToForgeTier.putAll(idToForgeTier);

        this.forgeTierToId.clear();

        idToForgeTier.forEach((identifier, forgeTier) -> this.forgeTierToId.put(forgeTier, identifier));
    }
}
