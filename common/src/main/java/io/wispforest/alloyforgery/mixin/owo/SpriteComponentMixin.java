package io.wispforest.alloyforgery.mixin.owo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.ui.component.SpriteComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(value = SpriteComponent.class, remap = false)
public abstract class SpriteComponentMixin {
    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lio/wispforest/owo/ui/core/OwoUIDrawContext;drawSpriteStretched(Ljava/util/function/Function;Lnet/minecraft/client/texture/Sprite;IIII)V"))
    private void drawWithContext(OwoUIDrawContext instance, Function function, Sprite sprite, int x, int y, int width, int height, Operation<Void> original) {
        original.call(instance, function, sprite, x, y, width, height);

        instance.draw();
    }
}
