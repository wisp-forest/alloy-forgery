package io.wispforest.alloyforgery.neoforge.data;

import io.wispforest.alloyforgery.data.providers.AlloyForgeryRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

public class AlloyForgeryData {

    public static void onInitializeDataGenerator(GatherDataEvent.Client event) {
        event.createProvider((output, registriesFuture) -> {
            return new RecipeProvider.Runner(output, registriesFuture) {
                @Override
                protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
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
