package io.wispforest.alloyforgery.fabric.data;

import io.wispforest.alloyforgery.data.providers.RecipeExporterConditionWrapper;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.function.BiFunction;

public record FabricResourceConditionHolder(List<ResourceCondition> conditions) implements ResourceConditionHolder {

    public static RecipeExporterConditionWrapper createWrapper(BiFunction<RecipeOutput, ResourceCondition[], RecipeOutput> providerConditionWrapper) {
        return (recipeExporter, holder) -> {
            if (holder instanceof FabricResourceConditionHolder(java.util.List<ResourceCondition> conditions1)) {
                return providerConditionWrapper.apply(recipeExporter, conditions1.toArray(ResourceCondition[]::new));
            }

            return recipeExporter;
        };
    }

    @Override
    public <T extends ItemLike> ResourceConditionHolder withTags(ResourceKey<Registry<T>> key, TagKey<T>... tags) {
        this.conditions.add(net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions.tagsPopulated(key, tags));

        return this;
    }
}
