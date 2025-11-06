package io.wispforest.alloyforgery.neoforge.data;

import io.wispforest.alloyforgery.data.providers.AlloyForgeryRecipeProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

public class AlloyForgeryData {

    public static void onInitializeDataGenerator(GatherDataEvent.Client event) {
        event.createProvider((output, registriesFuture) -> {
            return new RecipeGenerator.RecipeProvider(output, registriesFuture) {
                @Override
                protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
                    return new NeoforgeAlloyForgeryRecipeProvider(event.getModContainer().getModId(), registryLookup, exporter);
                }

                @Override
                public String getName() {
                    return "Alloy Forgery Compat Recipes";
                }
            };
        });
    }
}
