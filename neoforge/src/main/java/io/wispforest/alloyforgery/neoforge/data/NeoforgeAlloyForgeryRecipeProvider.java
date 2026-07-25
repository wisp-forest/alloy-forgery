package io.wispforest.alloyforgery.neoforge.data;

import io.wispforest.alloyforgery.data.providers.AlloyForgeryRecipeProvider;
import io.wispforest.alloyforgery.data.providers.RecipeExporterConditionWrapper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import org.jetbrains.annotations.Nullable;

public class NeoforgeAlloyForgeryRecipeProvider extends AlloyForgeryRecipeProvider {

    public NeoforgeAlloyForgeryRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput recipeExporter) {
        this(null, registryLookup, recipeExporter);
    }

    public NeoforgeAlloyForgeryRecipeProvider(@Nullable String overrideModid, HolderLookup.Provider registryLookup, RecipeOutput recipeExporter) {
        super(new RecipeOutput() {
            @Override
            public Advancement.Builder advancement() {
                return recipeExporter.advancement();
            }

            @Override
            public void includeRootAdvancement() {
                recipeExporter.includeRootAdvancement();
            }

            @Override
            public void accept(ResourceKey<Recipe<?>> arg, Recipe<?> arg2, @Nullable AdvancementHolder arg3, ICondition... iConditions) {
                if (overrideModid != null) {
                    arg = ResourceKey.create(arg.registryKey(), ResourceLocation.fromNamespaceAndPath(overrideModid, arg.location().getPath()));
                }

                recipeExporter.accept(arg, arg2, arg3, iConditions);
            }
        }, registryLookup, NeoforgeResourceConditionHolder.createWrapper(ConditionalRecipeOutput::new));
    }
}
