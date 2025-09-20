package wraith.alloyforgery.forges;

import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.compress.archivers.sevenz.CLI;
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

    public static final Identifier TIER_LOADER = AlloyForgery.id("forge_tier");
    public static final Identifier TIER_BINDINGS_LOADER = AlloyForgery.id("forge_tier_bindings");

    public static void init() {
        EndecDataLoader.builder("alloy_forge/tier", ForgeTier.ENDEC)
            .create(TIER_LOADER, ResourceType.SERVER_DATA, (data, manager, profiler) -> {
                data.forEach(SERVER::registerTier);

                ForgeDefinition.legacyForgeDefinitionIdToTier.forEach((id, forgeTier) -> SERVER.registerTier(id.withSuffixedPath("_legacy_tier"), forgeTier));
            });

        EndecDataLoader.builder("alloy_forge/tier_binding", Endec.map(Identifier::toString, Identifier::tryParse, MinecraftEndecs.IDENTIFIER))
            .create(TIER_BINDINGS_LOADER, ResourceType.SERVER_DATA, (data, manager, profiler) -> {
                data.values().forEach((bindings) -> bindings.forEach(SERVER.forgeDefinitionToTier::putIfAbsent));

                ForgeDefinition.legacyForgeDefinitionIdToTier.keySet().forEach(id -> SERVER.forgeDefinitionToTier.putIfAbsent(id, id.withSuffixedPath("_legacy_tier")));
            });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            AlloyForgeNetworking.CHANNEL.serverHandle(player).send(new TierDataSync(SERVER.idToForgeTier(), SERVER.forgeDefinitionToTier()));
        });
    }

    private final Map<Identifier, ForgeTier> idToForgeTier = new IdentityHashMap<>();
    private final Map<ForgeTier, Identifier> forgeTierToId = new IdentityHashMap<>();

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
    public ForgeTier getBoundForgeTier(Identifier forgeDefinitionId) {
        return Optional.of(forgeDefinitionId)
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
    public void setTierData(Map<Identifier, ForgeTier> idToForgeTier, Map<Identifier, Identifier> forgeDefinitionToTier) {
        this.forgeDefinitionToTier.clear();
        this.forgeDefinitionToTier.putAll(forgeDefinitionToTier);

        this.idToForgeTier.clear();
        this.idToForgeTier.putAll(idToForgeTier);

        this.forgeTierToId.clear();
        idToForgeTier.forEach((identifier, forgeTier) -> this.forgeTierToId.put(forgeTier, identifier));
    }

    private void registerTier(Identifier id, ForgeTier tier) {
        if (SERVER.idToForgeTier.containsKey(id)) return;

        SERVER.idToForgeTier.put(id, tier);
        SERVER.forgeTierToId.put(tier, id);
    }
}
