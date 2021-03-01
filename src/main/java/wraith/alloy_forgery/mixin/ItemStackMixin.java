package wraith.alloy_forgery.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.Forge;
import wraith.alloy_forgery.api.Forges;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    public void getName(CallbackInfoReturnable<Text> cir) {
        Identifier id = Registry.ITEM.getId(getItem());
        if (id.getNamespace().equals(AlloyForgery.MOD_ID) && id.getPath().endsWith("_forge_controller")) {
            Forge forge = Forges.getForge(id.getPath());
            Language language = Language.getInstance();

            //Get the name pattern for our current language
            String name = language.get("alloy_forgery.forge_controller_name_pattern");

            //Replace the keys with the important info, leave everything else intact
            name = name.replace("{type}", language.get(forge.translationKey));
            name = name.replace("{name}", language.get("alloy_forgery.forge_controller"));

            cir.setReturnValue(new LiteralText(name));
        }
    }
}