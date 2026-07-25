package io.wispforest.alloyforgery.pond;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceLocation;
import io.wispforest.alloyforgery.data.RecipeTagLoader;
import io.wispforest.alloyforgery.mixin.RecipeHolderMixin;

/**
 * Helper interface injected into {@link Recipe} through {@link RecipeHolderMixin}
 * to implement Tag check call within {@link RecipeTagLoader}
 */
public interface RecipeTagHelper {

    default boolean isIn(ResourceLocation tag) {
        throw new UnsupportedOperationException("RecipeTagHelper 'isIn' method not implememnted!");
    }
}
