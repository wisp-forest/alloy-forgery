package io.wispforest.alloyforgery.mixin;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerRecipeManager.class)
public interface ServerRecipeManagerAccessor {
    @Accessor("registries")
    RegistryWrapper.WrapperLookup af$getRegistryLookup();

    @Accessor("preparedRecipes")
    PreparedRecipes af$preparedRecipes();
}
