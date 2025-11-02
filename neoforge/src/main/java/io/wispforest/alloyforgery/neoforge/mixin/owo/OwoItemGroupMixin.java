package io.wispforest.alloyforgery.neoforge.mixin.owo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.wispforest.alloyforgery.neoforge.mixin.ItemGroupAccessor;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OwoItemGroup.class)
public class OwoItemGroupMixin {
    @WrapMethod(method = "getTexture")
    private Identifier test(Operation<Identifier> original) {
        var id = original.call();

        if (id == null) {
            id = ((ItemGroupAccessor) this).af$getTexture();
        }

        return id;
    }
}
