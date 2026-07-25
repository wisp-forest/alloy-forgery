package io.wispforest.alloyforgery.mixin.registry;

import io.wispforest.alloyforgery.compat.LegacyIdMappings;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin {
    @ModifyVariable(at = @At("HEAD"), method = {
        "getValue(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;",
        "get(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;"
    }, ordinal = 0, argsOnly = true)
    private Identifier fixMissingFromRegistry(@Nullable Identifier id) {
        return LegacyIdMappings.remap(id);
    }
}
