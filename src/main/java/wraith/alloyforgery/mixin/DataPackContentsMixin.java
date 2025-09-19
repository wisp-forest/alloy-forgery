package wraith.alloyforgery.mixin;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.server.DataPackContents;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import java.util.HashMap;

@Mixin(DataPackContents.class)
public abstract class DataPackContentsMixin {

    @Shadow
    @Final
    private ServerRecipeManager recipeManager;
    @Shadow
    @Final
    private ReloadableRegistries.Lookup reloadableRegistries;

    @Inject(method = "applyPendingTagLoads", at = @At("TAIL"))
    private void alloy_forgery$onRefresh(CallbackInfo ci) {
        var recipeEntries = recipeManager.getAllOfType(AlloyForgeRecipe.Type.INSTANCE);

        var map = new HashMap<AlloyForgeRecipe, Identifier>();

        for (var entry : recipeEntries) {
            map.put(entry.value(), entry.id().getValue());
        }

        AlloyForgeRecipe.PENDING_RECIPES.forEach((recipe, pendingRecipeData) -> recipe.finishRecipe(this.reloadableRegistries.createRegistryLookup(), pendingRecipeData, key -> map.getOrDefault(key, Identifier.of(AlloyForgery.MOD_ID, "unknown_recipe"))));

        AlloyForgeRecipe.PENDING_RECIPES.clear();
    }
}
