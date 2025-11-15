package io.wispforest.alloyforgery.mixin;

import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.server.DataPackContents;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.recipe.AlloyForgeRecipe;
import java.util.HashMap;

@Mixin(DataPackContents.class)
public abstract class DataPackContentsMixin {
    @Inject(method = "applyPendingTagLoads", at = @At("TAIL"))
    private void alloy_forgery$onRefresh(CallbackInfo ci) {
        var contents = ((DataPackContents) (Object) this);

        var recipeEntries = GeneralPlatformUtils.INSTANCE.getAllOfType(contents.getRecipeManager(), AlloyForgeRecipe.Type.INSTANCE);

        var map = new HashMap<AlloyForgeRecipe, Identifier>();

        for (var entry : recipeEntries) {
            map.put(entry.value(), entry.id().getValue());
        }

        AlloyForgeRecipe.PENDING_RECIPES.forEach((recipe, pendingRecipeData) -> recipe.finishRecipe(contents.getReloadableRegistries().createRegistryLookup(), pendingRecipeData, key -> map.getOrDefault(key, Identifier.of(AlloyForgery.MOD_ID, "unknown_recipe"))));

        AlloyForgeRecipe.PENDING_RECIPES.clear();
    }
}
