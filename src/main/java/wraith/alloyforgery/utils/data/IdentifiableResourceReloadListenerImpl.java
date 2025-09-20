package wraith.alloyforgery.utils.data;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record IdentifiableResourceReloadListenerImpl(Identifier location, ResourceReloader listener, Set<Identifier> dependencies) implements IdentifiableResourceReloadListener {

    public IdentifiableResourceReloadListenerImpl(Identifier location, ResourceReloader listener, Identifier... dependencies) {
        this(location, listener, new HashSet<>(List.of(dependencies)));
    }

    @Override
    public Identifier getFabricId() {
        return this.location;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {
        return this.listener.reload(preparationBarrier, resourceManager, executor, executor2);
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return dependencies;
    }
}
