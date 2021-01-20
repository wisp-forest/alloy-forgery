package wraith.alloy_forgery.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.utils.Utils;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    public void getName(CallbackInfoReturnable<Text> cir) {
        Identifier id = Registry.ITEM.getId(getItem());
        if (id.getNamespace().equals(AlloyForgery.MOD_ID) && id.getPath().endsWith("_forge_controller")) {
            String[] segments = id.getPath().split("_");
            String[] materialSegments = new String[segments.length - 2];
            if (materialSegments.length >= 0) {
                System.arraycopy(segments, 0, materialSegments, 0, materialSegments.length);
            }
            Text forgeText = new TranslatableText("alloy_forgery.forge_controller");
            Text name = new LiteralText(Utils.capitalize(materialSegments) + " " + forgeText.getString());
            cir.setReturnValue(name);
        }
    }
}