package wraith.alloyforgery.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.alloyforgery.utils.DataPackEvents;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "onDataPacksReloaded", at = @At("HEAD"))
    private void alloyForgery_beforeReloadSync(CallbackInfo ci) {
        DataPackEvents.BEFORE_SYNC.invoker().beforeSync(this.server);
    }
}
