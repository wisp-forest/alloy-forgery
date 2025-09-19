package wraith.alloyforgery.utils.data;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.RegistriesAttribute;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.mixin.JsonDataLoaderAccessor;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;


// TODO: 1.21.4 ADJUSTMENTS SHOULD BE MADE TO USE LESS DIRECT CODE ANYWAYS
public abstract class EndecDataLoader<T> extends JsonDataLoader<T> {

    protected final String type;

    protected final Identifier id;
    protected final Endec<T> endec;

    protected final Set<Identifier> dependencies;

    protected final SerializationContext context;

    protected final boolean requiresRegistries;

    protected EndecDataLoader(Identifier id, String type, Endec<T> endec, ResourceType packType) {
        this(id, type, endec, packType, false);
    }

    protected EndecDataLoader(Identifier id, String type, Endec<T> endec, ResourceType packType, Set<Identifier> value) {
        this(id, type, endec, packType, SerializationContext.empty(),false, value);
    }

    protected EndecDataLoader(Identifier id, String type, Endec<T> endec, ResourceType packType, boolean requiresRegistries) {
        this(id, type, endec, packType, SerializationContext.empty(), requiresRegistries);
    }

    protected EndecDataLoader(Identifier id, String type, Endec<T> endec, ResourceType packType, SerializationContext context, boolean requiresRegistries) {
        this(id, type, endec, packType, context, requiresRegistries, Set.of());
    }

    protected EndecDataLoader(Identifier id, String type, Endec<T> endec, ResourceType packType, SerializationContext context, boolean requiresRegistries, Set<Identifier> value) {
        super(new DelayedRecursiveCodec<>(), ResourceFinder.json(type));

        this.id = id;
        this.type = type;
        this.endec = endec;
        this.context = context;
        this.requiresRegistries = requiresRegistries;
        this.dependencies = value;

        setupCodec();


        if (ResourceType.SERVER_DATA.equals(packType) && requiresRegistries) {
            ResourceManagerHelper.get(packType).registerReloadListener(id, wrapperLookup -> {
                this.setupOps(wrapperLookup);
                return new IdentifiableResourceReloadListenerImpl(id, this, this.dependencies);
            });
        } else {
            ResourceManagerHelper.get(packType).registerReloadListener(new IdentifiableResourceReloadListenerImpl(id, this, this.dependencies));
        }
    }

    public Identifier getLoaderId() {
        return id;
    }

    public Set<Identifier> getDependencyIds() {
        return dependencies;
    }

    protected void setupCodec() {
        ((DelayedRecursiveCodec<T>) ((JsonDataLoaderAccessor<T>) this).codec())
                .setup(this.endec.toString(), codec -> CodecUtils.toCodec(endec, this.getContext()));
    }

    @Nullable
    private RegistryWrapper.WrapperLookup registries = null;

    @ApiStatus.Internal
    private EndecDataLoader<T> setupOps(RegistryWrapper.WrapperLookup registries) {
        this.registries = registries;

        // Resets the given converted endec to grab new context with current registries
        setupCodec();

        return this;
    }

    private SerializationContext getContext() {
        if (requiresRegistries) {
            Objects.requireNonNull(registries, "Can not build the needed context for the ManagedEndecDataLoader: " + this.getLoaderId());

            return this.context.withAttributes(RegistriesAttribute.fromInfoGetter(new RegistryOps.CachedRegistryInfoGetter(registries)));
        }

        return this.context;
    }

    @Override
    protected Map<Identifier, T> prepare(ResourceManager resourceManager, Profiler profiler) {
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
}
