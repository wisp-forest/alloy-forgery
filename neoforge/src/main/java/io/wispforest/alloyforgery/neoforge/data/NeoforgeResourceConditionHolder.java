package io.wispforest.alloyforgery.neoforge.data;

import io.wispforest.alloyforgery.data.providers.RecipeExporterConditionWrapper;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public record NeoforgeResourceConditionHolder(List<ICondition> conditions) implements ResourceConditionHolder {

    public static RecipeExporterConditionWrapper createWrapper(BiFunction<RecipeExporter, ICondition[], RecipeExporter> providerConditionWrapper) {
        return (recipeExporter, holder) -> {
            if (holder instanceof NeoforgeResourceConditionHolder(List<ICondition> conditions1)) {
                return providerConditionWrapper.apply(recipeExporter, conditions1.toArray(ICondition[]::new));
            }

            return recipeExporter;
        };
    }

    @Override
    public <T extends ItemConvertible> ResourceConditionHolder withTags(RegistryKey<Registry<T>> key, TagKey<T>... tags) {
        this.conditions.add(NeoForgeConditions.and(Arrays.stream(tags).map(NeoForgeConditions::tagEmpty).map(NeoForgeConditions::not).toArray(ICondition[]::new)));

        return this;
    }
}
