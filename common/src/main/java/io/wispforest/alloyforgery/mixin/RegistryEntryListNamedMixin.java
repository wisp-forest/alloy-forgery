package io.wispforest.alloyforgery.mixin;

import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = HolderSet.Named.class, priority = 2000)
public abstract class RegistryEntryListNamedMixin<T> extends HolderSet.ListBacked<T> {
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        return obj instanceof HolderSet.Named<?> named && ((HolderSet.Named<T>) (Object) this).key().equals(named.key());
    }

    @Override
    public int hashCode() {
        return ((HolderSet.Named<T>) (Object) this).key().hashCode();
    }
}
