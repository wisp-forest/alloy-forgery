package io.wispforest.alloyforgery.data.providers;

import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface ResourceConditionHolder {

    static ResourceConditionHolder createConditions() {
        return GeneralPlatformUtils.INSTANCE.createConditionsHolder();
    }

    <T extends ItemLike> ResourceConditionHolder withTags(ResourceKey<Registry<T>> key, TagKey<T> ...tags);
}
