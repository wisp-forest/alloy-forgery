package io.wispforest.alloyforgery.data.providers;

import net.minecraft.data.recipes.RecipeOutput;

public interface RecipeExporterConditionWrapper {
    RecipeOutput withConditions(RecipeOutput exporter, ResourceConditionHolder conditions);
}
