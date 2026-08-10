package io.wispforest.alloyforgery.utils.data;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.RegistriesAttribute;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.PreparableReloadListener.SharedState;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import io.wispforest.alloyforgery.mixin.SimpleJsonResourceReloadListenerAccessor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


// TODO: 1.21.4 ADJUSTMENTS SHOULD BE MADE TO USE LESS DIRECT CODE ANYWAYS
public abstract class EndecDataLoader<T> extends SimpleJsonResourceReloadListener<T> {

    protected final String type;

    protected final Identifier id;
    protected final Endec<T> endec;

    protected final Set<Identifier> dependencies;

    protected final SerializationContext context;

    protected final boolean requiresRegistries;

    private EndecDataLoader(Identifier id, String type, Endec<T> endec, SerializationContext context, boolean requiresRegistries, Set<Identifier> value) {
        super(new DelayedRecursiveCodec<>(), FileToIdConverter.json(type));

        this.id = id;
        this.type = type;
        this.endec = endec;
        this.context = context;
        this.requiresRegistries = requiresRegistries;
        this.dependencies = Collections.unmodifiableSet(value);

        setupCodec();
    }

    public static <T> EndecDataLoader.Builder<T> builder(String type, Endec<T> endec) {
        return new Builder<>(type, endec);
    }

    public static class Builder<T> {
        protected final String type;
        protected final Endec<T> endec;

        //--

        protected final Set<Identifier> dependencies = new HashSet<>();

        protected SerializationContext context = SerializationContext.empty();

        protected boolean requiresRegistries = false;

        Builder(String type, Endec<T> endec) {
            this.type = type;
            this.endec = endec;
        }

        public Builder<T> addDependencies(Identifier...dependencies) {
            return addDependencies(List.of(dependencies));
        }

        public Builder<T> addDependencies(Collection<Identifier> dependencies) {
            this.dependencies.addAll(dependencies);

            return this;
        }

        public Builder<T> requiresRegistries(boolean value) {
            this.requiresRegistries = value;

            return this;
        }

        public Builder<T> setContext(SerializationContext context) {
            this.context = context;

            return this;
        }

        public EndecDataLoader<T> create(Identifier id, PackType packType, LoadedDataHandler<T> handler) {
            var loader = new EndecDataLoader<T>(id, this.type, this.endec, this.context, this.requiresRegistries, this.dependencies) {
                @Override
                protected void apply(Map<Identifier, T> prepared, ResourceManager manager, ProfilerFiller profiler) {
                    handler.handleData(prepared, manager, profiler);
                }
            };

            GeneralPlatformUtils.INSTANCE.registerLoader(id, packType, loader, this.requiresRegistries);

            return loader;
        }
    }

    public Identifier getLoaderId() {
        return id;
    }

    public Set<Identifier> getDependencyIds() {
        return dependencies;
    }

    public boolean requiresRegistries() {
        return this.requiresRegistries;
    }

    protected void setupCodec() {
        ((DelayedRecursiveCodec<T>) ((SimpleJsonResourceReloadListenerAccessor<T>) this).codec())
                .setup(this.endec.toString(), codec -> CodecUtils.toCodec(endec, this.getContext()));
    }

    @Nullable
    private HolderLookup.Provider registries = null;

    @Nullable
    private Function<SharedState, HolderLookup.Provider> registryGetter = null;

    @Override
    public void prepareSharedState(SharedState store) {
        if (requiresRegistries) {
            Objects.requireNonNull(registryGetter, "Can not get the needed context for the ManagedEndecDataLoader: " + this.getLoaderId());

            this.registries = registryGetter.apply(store);

            registryGetter = null;

            // Resets the given converted endec to grab new context with current registries
            setupCodec();
        }
    }

    @ApiStatus.Internal
    public EndecDataLoader<T> setRegistryGetter(Function<SharedState, HolderLookup.Provider> registryGetter) {
        this.registryGetter = registryGetter;

        return this;
    }

    // FIXME - This is marked final in NeoForge
    private SerializationContext getContext() {
        if (requiresRegistries) {
            Objects.requireNonNull(registries, "Can not build the needed context for the ManagedEndecDataLoader: " + this.getLoaderId());

            return this.context.withAttributes(RegistriesAttribute.fromCachedInfoGetter(new RegistryOps.HolderLookupAdapter(registries)));
        }

        return this.context;
    }

    @Override
    protected Map<Identifier, T> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        if (requiresRegistries && registries == null) {
            throw new IllegalStateException("Unable to prepare files as the given Registry access has not been setup on the server! [Id: " + this.getLoaderId() + "]");
        }

        var entries = super.prepare(resourceManager, profiler);

        this.registries = null;

        return entries;
    }

    private static class DelayedRecursiveCodec<T> implements Codec<T> {
        private String name;
        private Supplier<Codec<T>> wrapped;

        public void setup(String name, Function<Codec<T>, Codec<T>> wrapped) {
            this.name = name;
            this.wrapped = Suppliers.memoize(() -> wrapped.apply(this));
        }

        @Override
        public <S> DataResult<Pair<T, S>> decode(final DynamicOps<S> ops, final S input) {
            return wrapped.get().decode(ops, input);
        }

        @Override
        public <S> DataResult<S> encode(final T input, final DynamicOps<S> ops, final S prefix) {
            return wrapped.get().encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "RecursiveCodec[" + name + ']';
        }
    }

    public interface LoadedDataHandler<T> {
        void handleData(Map<Identifier, T> data, ResourceManager manager, ProfilerFiller profiler);
    }
}
