package io.wispforest.alloyforgery.neoforge.mixin;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessor {
    @Accessor("texture")
    Identifier af$getTexture();
}
