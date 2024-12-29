package wraith.alloyforgery.utils.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;

public class EndecableDataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    private final Identifier id;

    private final Set<Identifier> dependencies = new HashSet<>();

    private final EndecedHandler<?> handler;

    private EndecableDataLoader(Identifier id, String dataType, EndecedHandler<?> handler) {
        super(GSON, dataType);

        this.id = id;
        this.handler = handler;
    }

    public static <T> EndecableDataLoader of(Identifier id, String dataType, Endec<T> endec, BiConsumer<Identifier, T> consumer) {
        return new EndecableDataLoader(id, dataType, new EndecedHandler<>(endec, consumer));
    }

    public static <T> EndecableDataLoader of(Identifier id, String dataType, String fieldName, Endec<T> endec, BiConsumer<Identifier, T> consumer) {
        return new EndecableDataLoader(id, dataType,
                EndecedHandler.of(fieldName, endec, consumer, entryId -> {
                    LOGGER.warn("A given entry within the [{}] Data Loader was found to be missing any data! [EntryId: {}]", id, entryId);
                }));
    }

    public EndecableDataLoader addDependencies(Identifier ...dependencies) {
        this.dependencies.addAll(Set.of(dependencies));

        return this;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        prepared.forEach((identifier, jsonElement) -> {
            try {
                handler.handle(identifier, jsonElement);
            } catch (JsonSyntaxException e){
                LOGGER.error("An error has occurred during [{}] stage:", id, e);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return this.dependencies;
    }
}
