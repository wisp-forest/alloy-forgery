package io.wispforest.alloyforgery.mixin.registry;

import io.wispforest.alloyforgery.compat.LegacyIdMappings;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin {
    @ModifyVariable(at = @At("HEAD"), method = {
        "getValue(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;",
        "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;"
    }, ordinal = 0, argsOnly = true)
    private ResourceLocation fixMissingFromRegistry(@Nullable ResourceLocation id) {
        return LegacyIdMappings.remap(id);
    }
}
