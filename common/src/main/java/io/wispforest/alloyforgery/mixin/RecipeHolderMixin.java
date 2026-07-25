package io.wispforest.alloyforgery.mixin;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.pond.RecipeTagHelper;

@Mixin(RecipeHolder.class)
public abstract class RecipeHolderMixin implements RecipeTagHelper {
    @Override
    public boolean isIn(Identifier tag) {
        return RecipeTagLoader.isWithinTag(false, tag, ((RecipeHolder<Recipe<?>>) (Object) this));
    }
}
