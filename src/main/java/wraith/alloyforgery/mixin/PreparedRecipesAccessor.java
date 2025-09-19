package wraith.alloyforgery.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.recipe.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(PreparedRecipes.class)
public interface PreparedRecipesAccessor {
    @Accessor("byType")
    Multimap<RecipeType<?>, RecipeEntry<?>> af$getRecipes();

    @Accessor("byType")
    @Mutable
    void af$setRecipes(Multimap<RecipeType<?>, RecipeEntry<?>> recipesByType);

    @Accessor("byKey")
    Map<Identifier, RecipeEntry<?>> af$getRecipesById();

    @Accessor("byKey")
    @Mutable
    void af$setRecipesById(Map<Identifier, RecipeEntry<?>> recipesById);
}
