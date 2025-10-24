package io.wispforest.alloyforgery.data.providers;

import net.minecraft.data.recipe.RecipeExporter;

public interface RecipeExporterConditionWrapper {
    RecipeExporter withConditions(RecipeExporter exporter, ResourceConditionHolder conditions);
}
