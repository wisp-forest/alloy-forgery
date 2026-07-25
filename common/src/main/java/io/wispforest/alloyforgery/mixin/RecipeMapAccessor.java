package io.wispforest.alloyforgery.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(RecipeMap.class)
public interface RecipeMapAccessor {
    @Accessor("byType")
    Multimap<RecipeType<?>, RecipeHolder<?>> af$getRecipes();

    @Accessor("byType")
    @Mutable
    void af$setRecipes(Multimap<RecipeType<?>, RecipeHolder<?>> recipesByType);

    @Accessor("byKey")
    Map<Identifier, RecipeHolder<?>> af$getRecipesById();

    @Accessor("byKey")
    @Mutable
    void af$setRecipesById(Map<Identifier, RecipeHolder<?>> recipesById);
}
