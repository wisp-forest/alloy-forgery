package io.wispforest.alloyforgery.pond;

import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.mixin.RecipeEntryMixin;

/**
 * Helper interface injected into {@link Recipe} through {@link RecipeEntryMixin}
 * to implement Tag check call within {@link RecipeTagLoader}
 */
public interface RecipeTagHelper {

    default boolean isIn(Identifier tag) {
        throw new UnsupportedOperationException("RecipeTagHelper 'isIn' method not implememnted!");
    }
}
