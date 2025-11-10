package io.wispforest.alloyforgery.mixin.registry;

import io.wispforest.alloyforgery.compat.LegacyIdMappings;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin {
    @ModifyVariable(at = @At("HEAD"), method = {
        "get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;",
        "getEntry(Lnet/minecraft/util/Identifier;)Ljava/util/Optional;"
    }, ordinal = 0, argsOnly = true)
    private Identifier fixMissingFromRegistry(@Nullable Identifier id) {
        return LegacyIdMappings.remap(id);
    }
}
