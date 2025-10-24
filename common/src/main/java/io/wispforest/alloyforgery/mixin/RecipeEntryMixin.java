package io.wispforest.alloyforgery.mixin;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.pond.RecipeTagHelper;

@Mixin(RecipeEntry.class)
public abstract class RecipeEntryMixin implements RecipeTagHelper {
    @Override
    public boolean isIn(Identifier tag) {
        return RecipeTagLoader.isWithinTag(tag, ((RecipeEntry<Recipe<?>>) (Object) this));
    }
}
