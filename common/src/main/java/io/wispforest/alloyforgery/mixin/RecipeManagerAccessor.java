package io.wispforest.alloyforgery.mixin;

import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("registries")
    HolderLookup.Provider af$getRegistryLookup();

    @Accessor("recipes")
    RecipeMap af$preparedRecipes();
}
