package wraith.alloyforgery.mixin;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.DataPackContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {

    @Inject(method = "refresh", at = @At("TAIL"))
    private void alloy_forgery$onRefresh(DynamicRegistryManager dynamicRegistryManager, CallbackInfo ci) {
        AlloyForgeRecipe.PENDING_RECIPES.forEach(AlloyForgeRecipe::finishRecipe);
    }

}
