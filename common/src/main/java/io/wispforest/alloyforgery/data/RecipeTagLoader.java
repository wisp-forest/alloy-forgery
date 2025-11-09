package io.wispforest.alloyforgery.data;

import io.wispforest.owo.network.ClientAccess;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.ApiStatus;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tag Loader used to load Recipe Based tags with the resolving
 * process being delayed till Data Pack load has ended
 */
public class RecipeTagLoader extends SinglePreparationResourceReloader<Map<Identifier, List<TagGroupLoader.TrackedEntry>>> {

    public static final Identifier ID = AlloyForgery.id("recipe_tag");

    public static final RecipeTagLoader INSTANCE = new RecipeTagLoader();

    private RecipeTagLoader() {}

    private static final Map<Identifier, Set<Identifier>> RESOLVED_CLIENT_ENTRIES = new LinkedHashMap<>();
    private static final Map<Identifier, Set<Identifier>> RESOLVED_SERVER_ENTRIES = new LinkedHashMap<>();

    private boolean areEntriesResolved = true;
    private static final Map<Identifier, List<TagGroupLoader.TrackedEntry>> RAW_TAG_DATA = new LinkedHashMap<>();

    private final DelayedTagGroupLoader<RecipeEntry<Recipe<?>>> tagGroupLoader = new DelayedTagGroupLoader<>("tags/recipe");

    @Override
    protected Map<Identifier, List<TagGroupLoader.TrackedEntry>> prepare(ResourceManager manager, Profiler profiler) {
        return this.tagGroupLoader.loadTags(manager);
    }

    @Override
    protected void apply(Map<Identifier, List<TagGroupLoader.TrackedEntry>> prepared, ResourceManager manager, Profiler profiler) {
        RAW_TAG_DATA.clear();

        RAW_TAG_DATA.putAll(prepared);
        areEntriesResolved = false;
    }

    //--

    /**
     * @param tag   Identifier for the given Tag
     * @param entry Recipe Entry to check
     * @return true if the tag exists and if the given entry exists within the Tag group
     */
    public static boolean isWithinTag(boolean isClient, Identifier tag, RecipeEntry<?> entry) {
        return isWithinTag(isClient, tag, entry.id().getRegistry());
    }

    /**
     * @param tag      Identifier for the given Tag
     * @param recipeID Recipe identifier
     * @return true if the tag exists and if the given entry exists within the Tag group
     */
    public static boolean isWithinTag(boolean isClient, Identifier tag, Identifier recipeID) {
        var entries = (isClient ? RESOLVED_CLIENT_ENTRIES : RESOLVED_SERVER_ENTRIES);

        return entries.containsKey(tag) && entries.get(tag).contains(recipeID);
    }

    //--

    @ApiStatus.Internal
    public void sendPlayerPacketAfterDataLoad(ServerPlayerEntity player) {
        resolveEntries(player.getEntityWorld().getServer());

        sendTagPacket(player);
    }

    @ApiStatus.Internal
    public void onServerStarted(MinecraftServer server) {
        resolveEntries(server);
    }

    private void resolveEntries(MinecraftServer server) {
        if (areEntriesResolved) return;

        var recipeManager = server.getRecipeManager();

        Map<Identifier, List<RecipeEntry<Recipe<?>>>> map = tagGroupLoader.setGetter(identifier -> {
                return Optional.ofNullable((RecipeEntry<Recipe<?>>) recipeManager.get(RegistryKey.of(RegistryKeys.RECIPE, identifier)).orElse(null));
            })
            .buildGroup(RAW_TAG_DATA);

        RESOLVED_SERVER_ENTRIES.clear();

        map.forEach((id, recipes) -> RESOLVED_SERVER_ENTRIES.put(id, recipes.stream().map(RecipeEntry::id).map(RegistryKey::getValue).collect(Collectors.toSet())));

        areEntriesResolved = true;
    }

    public void sendTagPacket(ServerPlayerEntity player) {
        AlloyForgeNetworking.CHANNEL.serverHandle(player).send(RecipeTagLoader.TagPacket.of(RESOLVED_SERVER_ENTRIES));
    }

    // Packet that acts as a sync packet for the Recipe Based Tag Entries
    public record TagPacket(List<TagEntry> entries) {
        public static TagPacket of(Map<Identifier, Set<Identifier>> tagEntries) {
            return new TagPacket(tagEntries.entrySet().stream()
                .map(entry -> new TagEntry(entry.getKey(), List.copyOf(entry.getValue())))
                .toList());
        }

        public static void handlePacket(TagPacket packet, ClientAccess access) {
            RESOLVED_CLIENT_ENTRIES.clear();

            RESOLVED_CLIENT_ENTRIES.putAll(
                packet.entries.stream().collect(Collectors.toMap(TagEntry::id, e -> new HashSet<>(e.entries())))
            );
        }
    }

    public record TagEntry(Identifier id, List<Identifier> entries) { }

    //--
}
