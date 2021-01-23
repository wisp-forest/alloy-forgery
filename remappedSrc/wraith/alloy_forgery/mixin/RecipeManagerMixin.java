package wraith.alloy_forgery.mixin;

import com.google.gson.JsonElement;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.alloy_forgery.Forge;
import wraith.alloy_forgery.api.Forges;
import wraith.alloy_forgery.utils.Utils;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "apply", at = @At("HEAD"))
    public void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        for (Map.Entry<String, Forge> forge : Forges.getForges()) {
            int i = 0;
            for (String material : forge.getValue().recipeMaterials) {
                map.put(Utils.ID(forge.getKey() + "_" + i++), Utils.createControllerRecipeJson(forge.getKey(), material));
            }
        }
    }

}