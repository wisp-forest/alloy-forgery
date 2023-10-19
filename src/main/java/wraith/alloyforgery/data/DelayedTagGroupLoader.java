package wraith.alloyforgery.data;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Version of {@link TagGroupLoader} but with tweaks for delayed use and without entire tags being thrown out
 * @param <T>
 */
public class DelayedTagGroupLoader<T> extends TagGroupLoader<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Function<Identifier, Optional<? extends T>> registryGetter = null;
    private final String dataType;

    public DelayedTagGroupLoader(String dataType){
        super(identifier -> Optional.empty(), dataType);

        this.dataType = dataType;
    }

    public DelayedTagGroupLoader<T> setGetter(Function<Identifier, Optional<? extends T>> registryGetter){
        this.registryGetter = registryGetter;

        return this;
    }

    // Copy of vanilla but returns both the error list and the tag resolved to its best effort
    private Pair<Collection<TrackedEntry>, Collection<T>> resolveAll(TagEntry.ValueGetter<T> valueGetter, List<TrackedEntry> entries) {
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        List<TrackedEntry> list = new ArrayList();

        for(TrackedEntry trackedEntry : entries) {
            if (!trackedEntry.entry().resolve(valueGetter, builder::add)) {
                list.add(trackedEntry);
            }
        }

        return new Pair<>(list, builder.build());
    }

    //Copy to vanilla but checks if this versions registeryGetter is set and uses to Pair version
    @Override
    public Map<Identifier, Collection<T>> buildGroup(Map<Identifier, List<TrackedEntry>> tags) {
        if(registryGetter == null) throw new RuntimeException("DelayedTagGroupLoader did not have the required registeryGetter set to resolve! [Type: " + this.dataType + "]");

        final Map<Identifier, Collection<T>> map = Maps.newHashMap();

        TagEntry.ValueGetter<T> valueGetter = new TagEntry.ValueGetter<>() {
            @Nullable @Override public T direct(Identifier id) { return registryGetter.apply(id).orElse(null); }
            @Nullable @Override public Collection<T> tag(Identifier id) { return map.get(id); }
        };

        DependencyTracker<Identifier, TagDependencies> dependencyTracker = new DependencyTracker<>();

        tags.forEach((id, entries) -> dependencyTracker.add(id, new TagDependencies(entries)));

        dependencyTracker.traverse((id, dependencies) -> {
            var pair = this.resolveAll(valueGetter, dependencies.entries());

            var missingReferences = pair.getLeft();

            if(!missingReferences.isEmpty()){
                LOGGER.error(
                        "Couldn't load the given entries within tag {}: {}",
                        id,
                        missingReferences.stream().map(Objects::toString).collect(Collectors.joining(", "))
                );
            }

            map.put(id, pair.getRight());
        });

        return map;
    }
}
