package wraith.alloyforgery.pond;

import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.data.RecipeTagLoader;
import wraith.alloyforgery.mixin.RecipeMixin;

/**
 * Helper interface injected into {@link Recipe} through {@link RecipeMixin}
 * to implement Tag check call within {@link RecipeTagLoader}
 */
public interface RecipeTagHelper {

    default boolean isIn(Identifier tag) {
        throw new UnsupportedOperationException("RecipeTagHelper 'isIn' method not implememnted!");
    }
}
