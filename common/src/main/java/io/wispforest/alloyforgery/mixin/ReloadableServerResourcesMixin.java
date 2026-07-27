package io.wispforest.alloyforgery.mixin;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.recipe.AlloyForgeRecipe;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashMap;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    @Inject(method = "updateComponentsAndStaticRegistryTags", at = @At("TAIL"))
    private void alloy_forgery$onRefresh(CallbackInfo ci) {
        var contents = ((ReloadableServerResources) (Object) this);

        var recipeEntries = GeneralPlatformUtils.INSTANCE.getAllOfType(contents.getRecipeManager(), AlloyForgeRecipe.Type.INSTANCE);

        var map = new HashMap<AlloyForgeRecipe, Identifier>();

        for (var entry : recipeEntries) {
            map.put(entry.value(), entry.id().identifier());
        }

        AlloyForgeRecipe.PENDING_RECIPES.forEach((recipe, pendingRecipeData) -> recipe.finishRecipe(contents.fullRegistries().lookup(), pendingRecipeData, key -> map.getOrDefault(key, Identifier.fromNamespaceAndPath(AlloyForgery.MOD_ID, "unknown_recipe"))));

        AlloyForgeRecipe.PENDING_RECIPES.clear();
    }
}
