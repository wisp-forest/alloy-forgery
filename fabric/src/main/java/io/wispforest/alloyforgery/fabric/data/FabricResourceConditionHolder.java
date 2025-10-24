package io.wispforest.alloyforgery.fabric.data;

import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;

import java.util.List;

public record FabricResourceConditionHolder(List<ResourceCondition> conditions) implements ResourceConditionHolder {
    @Override
    public <T extends ItemConvertible> ResourceConditionHolder withTags(RegistryKey<Registry<T>> key, TagKey<T>... tags) {
        this.conditions.add(net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions.tagsPopulated(key, tags));

        return this;
    }
}
