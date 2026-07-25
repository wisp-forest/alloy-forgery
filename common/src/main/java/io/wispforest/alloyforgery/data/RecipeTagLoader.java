package io.wispforest.alloyforgery.data;

import io.wispforest.owo.network.ClientAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagLoader;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tag Loader used to load Recipe Based tags with the resolving
 * process being delayed till Data Pack load has ended
 */
public class RecipeTagLoader extends SimplePreparableReloadListener<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> {

    public static final ResourceLocation ID = AlloyForgery.id("recipe_tag");

    public static final RecipeTagLoader INSTANCE = new RecipeTagLoader();

    private RecipeTagLoader() {}

    private static final Map<ResourceLocation, Set<ResourceLocation>> RESOLVED_CLIENT_ENTRIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Set<ResourceLocation>> RESOLVED_SERVER_ENTRIES = new LinkedHashMap<>();

    private boolean areEntriesResolved = true;
    private static final Map<ResourceLocation, List<TagLoader.EntryWithSource>> RAW_TAG_DATA = new LinkedHashMap<>();

    private final DelayedTagGroupLoader<RecipeHolder<Recipe<?>>> tagGroupLoader = new DelayedTagGroupLoader<>("tags/recipe");

    @Override
    protected Map<ResourceLocation, List<TagLoader.EntryWithSource>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return this.tagGroupLoader.load(manager);
    }

    @Override
    protected void apply(Map<ResourceLocation, List<TagLoader.EntryWithSource>> prepared, ResourceManager manager, ProfilerFiller profiler) {
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
    public static boolean isWithinTag(boolean isClient, ResourceLocation tag, RecipeHolder<?> entry) {
        return isWithinTag(isClient, tag, entry.id().registry());
    }

    /**
     * @param tag      Identifier for the given Tag
     * @param recipeID Recipe identifier
     * @return true if the tag exists and if the given entry exists within the Tag group
     */
    public static boolean isWithinTag(boolean isClient, ResourceLocation tag, ResourceLocation recipeID) {
        var entries = (isClient ? RESOLVED_CLIENT_ENTRIES : RESOLVED_SERVER_ENTRIES);

        return entries.containsKey(tag) && entries.get(tag).contains(recipeID);
    }

    //--

    @ApiStatus.Internal
    public void sendPlayerPacketAfterDataLoad(ServerPlayer player) {
        resolveEntries(player.level().getServer());

        sendTagPacket(player);
    }

    @ApiStatus.Internal
    public void onServerStarted(MinecraftServer server) {
        resolveEntries(server);
    }

    private void resolveEntries(MinecraftServer server) {
        if (areEntriesResolved) return;

        var recipeManager = server.getRecipeManager();

        Map<ResourceLocation, List<RecipeHolder<Recipe<?>>>> map = tagGroupLoader.setGetter(identifier -> {
                return Optional.ofNullable((RecipeHolder<Recipe<?>>) recipeManager.byKey(ResourceKey.create(Registries.RECIPE, identifier)).orElse(null));
            })
            .build(RAW_TAG_DATA);

        RESOLVED_SERVER_ENTRIES.clear();

        map.forEach((id, recipes) -> RESOLVED_SERVER_ENTRIES.put(id, recipes.stream().map(RecipeHolder::id).map(ResourceKey::location).collect(Collectors.toSet())));

        areEntriesResolved = true;
    }

    public void sendTagPacket(ServerPlayer player) {
        AlloyForgeNetworking.CHANNEL.serverHandle(player).send(RecipeTagLoader.TagPacket.of(RESOLVED_SERVER_ENTRIES));
    }

    // Packet that acts as a sync packet for the Recipe Based Tag Entries
    public record TagPacket(List<TagEntry> entries) {
        public static TagPacket of(Map<ResourceLocation, Set<ResourceLocation>> tagEntries) {
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

    public record TagEntry(ResourceLocation id, List<ResourceLocation> entries) { }

    //--
}
