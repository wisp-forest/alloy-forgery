package io.wispforest.alloyforgery.neoforge.data;

import io.wispforest.alloyforgery.data.providers.RecipeExporterConditionWrapper;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public record NeoforgeResourceConditionHolder(List<ICondition> conditions) implements ResourceConditionHolder {

    public static RecipeExporterConditionWrapper createWrapper(BiFunction<RecipeOutput, ICondition[], RecipeOutput> providerConditionWrapper) {
        return (recipeExporter, holder) -> {
            if (holder instanceof NeoforgeResourceConditionHolder(List<ICondition> conditions1)) {
                return providerConditionWrapper.apply(recipeExporter, conditions1.toArray(ICondition[]::new));
            }

            return recipeExporter;
        };
    }

    @Override
    public <T extends ItemLike> ResourceConditionHolder withTags(ResourceKey<Registry<T>> key, TagKey<T>... tags) {
        this.conditions.add(NeoForgeConditions.and(Arrays.stream(tags).map(NeoForgeConditions::tagEmpty).map(NeoForgeConditions::not).toArray(ICondition[]::new)));

        return this;
    }
}
