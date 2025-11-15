package io.wispforest.alloyforgery.mixin;

import net.minecraft.registry.entry.RegistryEntryList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = RegistryEntryList.Named.class, priority = 2000)
public abstract class RegistryEntryListNamedMixin<T> extends RegistryEntryList.ListBacked<T> {
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        return obj instanceof RegistryEntryList.Named<?> named && ((RegistryEntryList.Named<T>) (Object) this).getTag().equals(named.getTag());
    }

    @Override
    public int hashCode() {
        return ((RegistryEntryList.Named<T>) (Object) this).getTag().hashCode();
    }
}
