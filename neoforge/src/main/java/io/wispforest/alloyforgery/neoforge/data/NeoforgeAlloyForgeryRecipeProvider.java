package io.wispforest.alloyforgery.neoforge.data;

import io.wispforest.alloyforgery.data.providers.AlloyForgeryRecipeProvider;
import io.wispforest.alloyforgery.data.providers.RecipeExporterConditionWrapper;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import org.jetbrains.annotations.Nullable;

public class NeoforgeAlloyForgeryRecipeProvider extends AlloyForgeryRecipeProvider {

    public NeoforgeAlloyForgeryRecipeProvider(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter recipeExporter) {
        this(null, registryLookup, recipeExporter);
    }

    public NeoforgeAlloyForgeryRecipeProvider(@Nullable String overrideModid, RegistryWrapper.WrapperLookup registryLookup, RecipeExporter recipeExporter) {
        super(new RecipeExporter() {
            @Override
            public Advancement.Builder getAdvancementBuilder() {
                return recipeExporter.getAdvancementBuilder();
            }

            @Override
            public void addRootAdvancement() {
                recipeExporter.addRootAdvancement();
            }

            @Override
            public void accept(RegistryKey<Recipe<?>> arg, Recipe<?> arg2, @Nullable AdvancementEntry arg3, ICondition... iConditions) {
                if (overrideModid != null) {
                    arg = RegistryKey.of(arg.getRegistryRef(), Identifier.of(overrideModid, arg.getValue().getPath()));
                }

                recipeExporter.accept(arg, arg2, arg3, iConditions);
            }
        }, registryLookup, NeoforgeResourceConditionHolder.createWrapper(ConditionalRecipeOutput::new));
    }
}
