package io.wispforest.alloyforgery.mixin;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HopperBlockEntity.class)
public interface HopperBlockEntityAccessor {

    @Invoker("setCooldown")
    void alloyForge$setTransferCooldown(int transferCooldown);

    @Invoker("isOnCustomCooldown")
    boolean alloyForge$isDisabled();
}
