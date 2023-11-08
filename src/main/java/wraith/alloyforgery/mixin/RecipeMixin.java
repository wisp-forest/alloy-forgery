package wraith.alloyforgery.mixin;

import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import wraith.alloyforgery.data.RecipeTagLoader;
import wraith.alloyforgery.pond.RecipeTagHelper;

@Mixin(Recipe.class)
public interface RecipeMixin extends RecipeTagHelper {
    @Override
    default boolean isIn(Identifier tag){
        return RecipeTagLoader.isWithinTag(tag, (Recipe<?>) this);
    }
}
