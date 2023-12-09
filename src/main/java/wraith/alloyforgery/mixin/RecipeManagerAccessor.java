package wraith.alloyforgery.mixin;

import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("recipes")
    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> af$getRecipes();

    @Accessor("recipes")
    void af$setRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes);

    @Accessor("recipesById")
    Map<Identifier, Recipe<?>> af$getRecipesById();

    @Accessor("recipesById")
    void af$setRecipesById(Map<Identifier, Recipe<?>> recipesById);
}
