package io.wispforest.alloyforgery.mixin;

import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.wispforest.alloyforgery.utils.DataPackEvents;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void alloyForgery_beforeReloadSync(CallbackInfo ci) {
        DataPackEvents.BEFORE_SYNC.invoker().beforeSync(((PlayerList) (Object) this).getServer());
    }
}
