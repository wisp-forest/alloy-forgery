package wraith.alloyforgery.data;

import io.wispforest.owo.network.ClientAccess;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import wraith.alloyforgery.AlloyForgery;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tag Loader used to load Recipe Based tags with the resolving
 * process being delayed till Data Pack load has ended
 */
public class RecipeTagLoader extends SinglePreparationResourceReloader<Map<Identifier, List<TagGroupLoader.TrackedEntry>>> implements IdentifiableResourceReloadListener, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.EndDataPackReload {

    private static final Map<Identifier, Set<Identifier>> RESOLVED_ENTRIES = new HashMap<>();

    private static final Map<Identifier, List<TagGroupLoader.TrackedEntry>> RAW_TAG_DATA = new HashMap<>();

    private final DelayedTagGroupLoader<Recipe<?>> tagGroupLoader = new DelayedTagGroupLoader<>("tags/recipes");

    @Override
    protected Map<Identifier, List<TagGroupLoader.TrackedEntry>> prepare(ResourceManager manager, Profiler profiler) {
        return this.tagGroupLoader.loadTags(manager);
    }

    @Override
    protected void apply(Map<Identifier, List<TagGroupLoader.TrackedEntry>> prepared, ResourceManager manager, Profiler profiler) {
        RAW_TAG_DATA.clear();

        RAW_TAG_DATA.putAll(prepared);
    }

    @Override
    public Identifier getFabricId() {
        return AlloyForgery.id("recipe_tag");
    }

    //--

    /**
     * @param tag Identifier for the given Tag
     * @param entry Recipe Entry to check
     * @return true if the tag exists and if the given entry exists within the Tag group
     */
    public static boolean isWithinTag(Identifier tag, Recipe<?> entry){
        return isWithinTag(tag, entry.getId());
    }

    /**
     * @param tag Identifier for the given Tag
     * @param recipeID Recipe identifier
     * @return true if the tag exists and if the given entry exists within the Tag group
     */
    public static boolean isWithinTag(Identifier tag, Identifier recipeID){
        if(!RESOLVED_ENTRIES.containsKey(tag)) return false;

        return RESOLVED_ENTRIES.get(tag).contains(recipeID);
    }

    //--

    public void initEvents() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(this);
        ServerLifecycleEvents.SERVER_STARTED.register(this);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            AlloyForgery.CHANNEL.serverHandle(handler.player).send(TagPacket.of(RESOLVED_ENTRIES));
        });
    }

    @Override
    public void endDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        if(!success) return;

        resolveEntries(server);

        AlloyForgery.CHANNEL.serverHandle(server).send(TagPacket.of(RESOLVED_ENTRIES));
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        resolveEntries(server);
    }

    public void resolveEntries(MinecraftServer server){
        var recipeManager = server.getRecipeManager();

        Map<Identifier, Collection<Recipe<?>>> map = tagGroupLoader.setGetter(recipeManager::get)
                .buildGroup(RAW_TAG_DATA);

        RESOLVED_ENTRIES.clear();

        map.forEach((id, recipes) -> RESOLVED_ENTRIES.put(id, recipes.stream().map(Recipe::getId).collect(Collectors.toSet())));
    }

    // Packet that acts as a sync packet for the Recipe Based Tag Entries
    public record TagPacket(List<TagEntry> entries){
        public static TagPacket of(Map<Identifier, Set<Identifier>> tagEntries){
            return new TagPacket(tagEntries.entrySet().stream()
                    .map(entry -> new TagEntry(entry.getKey(), List.copyOf(entry.getValue())))
                    .toList());
        }

        public static void handlePacket(TagPacket packet, ClientAccess access){
            RESOLVED_ENTRIES.clear();

            RESOLVED_ENTRIES.putAll(
                    packet.entries.stream().collect(Collectors.toMap(TagEntry::id, e -> new HashSet<>(e.entries())))
            );
        }
    }

    public record TagEntry(Identifier id, List<Identifier> entries){};

    //--
}
