package io.wispforest.alloyforgery.mixin;

import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.wispforest.alloyforgery.utils.DataPackEvents;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "onDataPacksReloaded", at = @At("HEAD"))
    private void alloyForgery_beforeReloadSync(CallbackInfo ci) {
        DataPackEvents.BEFORE_SYNC.invoker().beforeSync(((PlayerManager) (Object) this).getServer());
    }
}
