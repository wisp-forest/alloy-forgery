package io.wispforest.alloyforgery.mixin;

import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import io.wispforest.alloyforgery.compat.LegacyIdMappings;

@Mixin(SimpleDefaultedRegistry.class)
public abstract class DefaultedRegistryMixin {
    @ModifyVariable(at = @At("HEAD"), method = "get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;", ordinal = 0, argsOnly = true)
    private Identifier fixMissingFromRegistry(@Nullable Identifier id) {
        return LegacyIdMappings.remap(id);
    }
}
