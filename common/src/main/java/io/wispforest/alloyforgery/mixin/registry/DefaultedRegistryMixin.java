package io.wispforest.alloyforgery.mixin.registry;

import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import io.wispforest.alloyforgery.compat.LegacyIdMappings;

@Mixin(DefaultedMappedRegistry.class)
public abstract class DefaultedRegistryMixin {
    @ModifyVariable(at = @At("HEAD"), method = "getValue(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;", ordinal = 0, argsOnly = true)
    private ResourceLocation fixMissingFromRegistry(@Nullable ResourceLocation id) {
        return LegacyIdMappings.remap(id);
    }
}
