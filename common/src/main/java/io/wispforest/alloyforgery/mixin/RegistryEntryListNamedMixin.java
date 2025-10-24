package io.wispforest.alloyforgery.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;

@Mixin(value = RegistryEntryList.Named.class, priority = 2000)
public abstract class RegistryEntryListNamedMixin<T> extends RegistryEntryList.ListBacked<T> {
    @Shadow
    protected abstract List<RegistryEntry<T>> getEntries();

    @Shadow
    private @Nullable List<RegistryEntry<T>> entries;

    @Shadow
    public abstract Either<TagKey<T>, List<RegistryEntry<T>>> getStorage();

    @Shadow
    public abstract Optional<TagKey<T>> getTagKey();

    @Shadow
    public abstract TagKey<T> getTag();

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        return obj instanceof RegistryEntryList.Named<?> named && this.getTag().equals(named.getTag());
    }

    @Override
    public int hashCode() {
        return this.getTag().hashCode();
    }
}
