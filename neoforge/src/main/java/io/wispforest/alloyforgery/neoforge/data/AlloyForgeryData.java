package io.wispforest.alloyforgery.neoforge.data;

import io.wispforest.alloyforgery.data.providers.AlloyForgeryRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.registry.RegistryWrapper;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class AlloyForgeryData {

    public static void onInitializeDataGenerator(GatherDataEvent.Client event) {
        event.createProvider((output, registriesFuture) -> {
            return new RecipeGenerator.RecipeProvider(output, registriesFuture) {
                @Override
                protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
                    return new AlloyForgeryRecipeProvider(exporter, registryLookup, NeoforgeResourceConditionHolder.createWrapper(ConditionalRecipeOutput::new));
                }

                @Override
                public String getName() {
                    return "Alloy Forgery Compat Recipes";
                }
            };
        });
    }
}
