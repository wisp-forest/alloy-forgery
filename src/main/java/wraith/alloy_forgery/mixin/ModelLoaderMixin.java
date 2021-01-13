package wraith.alloy_forgery.mixin;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.utils.Utils;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {

    @Inject(method = "loadModelFromJson", at = @At("HEAD"), cancellable = true)
    public void loadModelFromJson(Identifier id, CallbackInfoReturnable<JsonUnbakedModel> cir) {
        boolean isAlloyForgery = id.getNamespace().equals(AlloyForgery.MOD_ID);

            String json = Utils.createModelJson(id.getPath(), modelParent);

            JsonUnbakedModel model = JsonUnbakedModel.deserialize(json);
            model.id = id.toString();
            cir.setReturnValue(model);
            cir.cancel();
        }
}