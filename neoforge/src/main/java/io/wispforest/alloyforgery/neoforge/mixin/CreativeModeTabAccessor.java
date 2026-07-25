package io.wispforest.alloyforgery.neoforge.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {
    @Accessor("backgroundTexture")
    Identifier af$getBackgroundTexture();
}
