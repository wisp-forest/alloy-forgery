package io.wispforest.alloyforgery.data.providers;

import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;

public interface ResourceConditionHolder {

    static ResourceConditionHolder createConditions() {
        return GeneralPlatformUtils.INSTANCE.createConditionsHolder();
    }

    <T extends ItemConvertible> ResourceConditionHolder withTags(RegistryKey<Registry<T>> key, TagKey<T> ...tags);
}
