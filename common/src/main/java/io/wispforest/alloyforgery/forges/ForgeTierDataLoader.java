package io.wispforest.alloyforgery.forges;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.endec.impl.StructField;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.networking.TierDataSync;
import io.wispforest.alloyforgery.utils.data.EndecDataLoader;

import java.util.*;
import java.util.function.Function;

public class ForgeTierDataLoader {

    private static final ForgeTierDataLoader SERVER = new ForgeTierDataLoader();
    private static final ForgeTierDataLoader CLIENT = new ForgeTierDataLoader();

    public static final Identifier TIER_LOADER = AlloyForgery.id("forge_tier");
    public static final Identifier TIER_BINDINGS_LOADER = AlloyForgery.id("forge_tier_bindings");

    public static void init() {
        EndecDataLoader.builder("alloy_forge/tier", RawForgeTier.ENDEC)
            .create(TIER_LOADER, ResourceType.SERVER_DATA, (data, manager, profiler) -> {
                var waitingChildrenTiers = new LinkedHashMap<Identifier, Map<Identifier, RawForgeTier>>();

                data.forEach((identifier, rawForgeTier) -> {
                    var wasAdded = SERVER.registerTier(identifier, rawForgeTier);

                    if (!wasAdded) {
                        waitingChildrenTiers.computeIfAbsent(identifier, id -> new LinkedHashMap<>())
                            .put(identifier, rawForgeTier);
                    } else {
                        var childrenTiers = waitingChildrenTiers.getOrDefault(identifier, Map.of());

                        childrenTiers.forEach(SERVER::registerTier);
                    }
                });

                ForgeDefinition.legacyForgeDefinitionIdToTier.forEach((id, forgeTier) -> SERVER.registerTier(id.withSuffixedPath("_legacy_tier"), forgeTier));

                waitingChildrenTiers.forEach((parentId, childrenTiers) -> {
                    AlloyForgery.LOGGER.error("Unable to register the given children ForgeTier's as the parent '{}' was never registered: [{}]", parentId, childrenTiers.keySet());
                });

                SERVER.buildSortedIndexToTiersMap();
            });

        EndecDataLoader.builder("alloy_forge/tier_binding", Endec.map(Identifier::toString, Identifier::tryParse, MinecraftEndecs.IDENTIFIER))
            .create(TIER_BINDINGS_LOADER, ResourceType.SERVER_DATA, (data, manager, profiler) -> {
                data.values().forEach((bindings) -> bindings.forEach(SERVER.forgeDefinitionToTier::putIfAbsent));

                ForgeDefinition.legacyForgeDefinitionIdToTier.keySet().forEach(id -> SERVER.forgeDefinitionToTier.putIfAbsent(id, id.withSuffixedPath("_legacy_tier")));
            });
    }

    private final Map<Identifier, ForgeTier> idToForgeTier = new HashMap<>();
    private final Map<ForgeTier, Identifier> forgeTierToId = new IdentityHashMap<>();

    private final Map<Identifier, Set<Identifier>> parentTierToChildrenTier = new HashMap<>();

    private final Map<Identifier, Identifier> forgeDefinitionToTier = new HashMap<>();

    private final Int2ObjectMap<List<Identifier>> tierIndexToTiers = new Int2ObjectLinkedOpenHashMap<>();

    private void buildSortedIndexToTiersMap() {
        tierIndexToTiers.clear();

        idToForgeTier.forEach((identifier, forgeTier) -> {
            if (parentTierToChildrenTier.containsKey(identifier)) return;

            tierIndexToTiers.computeIfAbsent(forgeTier.value(), (value) -> new ArrayList<>())
                .add(identifier);
        });

        tierIndexToTiers.forEach((integer, identifiers) -> identifiers.sort(Comparator.naturalOrder()));
    }

    public static ForgeTierDataLoader getForgeRegistry(boolean isClientSide) {
        return (isClientSide) ? CLIENT : SERVER;
    }

    @Nullable
    public static Identifier getForgeTierId(boolean isClientSide, ForgeTier forgeTier) {
        return getForgeRegistry(isClientSide).forgeTierToId.get(forgeTier);
    }

    @Nullable
    public static ForgeTier getForgeTier(boolean isClientSide, Identifier tierId) {
        return getForgeRegistry(isClientSide).idToForgeTier.get(tierId);
    }

    @Nullable
    public ForgeTier getBoundForgeTier(Identifier forgeDefinitionId) {
        return Optional.of(forgeDefinitionId)
            .map(forgeDefinitionToTier::get)
            .map(idToForgeTier::get)
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

    public ForgeTier getPrimaryTierOrDefault(ForgeTier tier) {
        var primaryTier = getPrimaryTier(tier.value());

        return primaryTier != null ? primaryTier : tier;
    }

    public ForgeTier getPrimaryTier(int tierValue) {
        var possibleTiers = tierIndexToTiers.get(tierValue);

        if (possibleTiers == null || possibleTiers.isEmpty()) return null;

        return idToForgeTier.get(possibleTiers.getFirst());
    }

    @ApiStatus.Internal
    public void setTierData(Map<Identifier, ForgeTier> idToForgeTier, Map<Identifier, Identifier> forgeDefinitionToTier) {
        this.forgeDefinitionToTier.clear();
        this.forgeDefinitionToTier.putAll(forgeDefinitionToTier);

        this.idToForgeTier.clear();
        this.idToForgeTier.putAll(idToForgeTier);

        this.forgeTierToId.clear();
        idToForgeTier.forEach((identifier, forgeTier) -> this.forgeTierToId.put(forgeTier, identifier));

        CLIENT.buildSortedIndexToTiersMap();
    }

    public static TierDataSync createSyncPacket() {
        return new TierDataSync(SERVER.idToForgeTier(), SERVER.forgeDefinitionToTier());
    }

    private boolean registerTier(Identifier id, RawForgeTier tier) {
        var parentId = tier.parentIdentifier().orElse(null);

        var parentTier = (parentId != null)
            ? Optional.ofNullable(SERVER.idToForgeTier.get(parentId))
            : Optional.<ForgeTier>empty();

        if (parentId != null) {
            parentTierToChildrenTier.computeIfAbsent(parentId, identifier -> new HashSet<>())
                .add(id);
        } else {
            parentTierToChildrenTier.computeIfAbsent(id, identifier -> new HashSet<>());
        }

        if (parentId != null && parentTier.isEmpty()) return false;

        registerTier(id, tier.buildTier(id, parentTier));

        return true;
    }

    private void registerTier(Identifier id, ForgeTier tier) {
        if (SERVER.idToForgeTier.containsKey(id)) return;

        SERVER.idToForgeTier.put(id, tier);
        SERVER.forgeTierToId.put(tier, id);
    }

    public record RawForgeTier(Optional<Identifier> parentIdentifier, Optional<Integer> value, Optional<Float> speedMultiplier, Optional<Float> fuelConsumptionMultiplier, Optional<Integer> fuelCapacity) {

        public static final Endec<RawForgeTier> ENDEC = StructEndecBuilder.of(
            optionalFieldOf(MinecraftEndecs.IDENTIFIER.optionalOf(),"parent_tier", RawForgeTier::parentIdentifier),
            optionalFieldOf(Endec.INT.optionalOf(),"tier", RawForgeTier::value),
            optionalFieldOf(Endec.FLOAT.optionalOf(),"speed_multiplier", RawForgeTier::speedMultiplier),
            optionalFieldOf(Endec.FLOAT.optionalOf(),"fuel_consumption_multiplier", RawForgeTier::fuelConsumptionMultiplier),
            optionalFieldOf(Endec.INT.optionalOf(),"fuel_capacity", RawForgeTier::fuelCapacity),
            RawForgeTier::new
        );

        private static <T, S> StructField<S, Optional<T>> optionalFieldOf(Endec<Optional<T>> endec, String name, Function<S, Optional<T>> getter) {
            return new StructField<S, Optional<T>>(name, endec, getter, Optional::empty);
        }

        public ForgeTier buildTier(Identifier id, Optional<ForgeTier> parentTier) {
            var speedMultiplierValue = speedMultiplier().or(() -> parentTier.map(ForgeTier::speedMultiplier)).orElse(1f);

            return new ForgeTier(
                id,
                value().or(() -> parentTier.map(ForgeTier::value)).orElse(1),
                speedMultiplierValue,
                fuelConsumptionMultiplier().or(() -> parentTier.map(ForgeTier::fuelConsumptionMultiplier)).orElse(speedMultiplierValue),
                fuelCapacity().or(() -> parentTier.map(ForgeTier::fuelCapacity)).orElse(48000)
            );
        }
    }
}
