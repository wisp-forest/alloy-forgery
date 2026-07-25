package io.wispforest.alloyforgery.compat;

import net.minecraft.resources.ResourceLocation;
import io.wispforest.alloyforgery.AlloyForgery;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LegacyIdMappings {

    private static final Map<ResourceLocation, ResourceLocation> MAPPINGS = new HashMap<>();

    static {
        MAPPINGS.put(id("blackstone_forge_controller"), id("polished_blackstone_forge_controller"));
        MAPPINGS.put(id("brick_forge_controller"), id("bricks_forge_controller"));
        MAPPINGS.put(id("deepslate_forge_controller"), id("deepslate_bricks_forge_controller"));
        MAPPINGS.put(id("end_stone_forge_controller"), id("end_stone_bricks_forge_controller"));
        MAPPINGS.put(id("stone_brick_forge_controller"), id("stone_bricks_forge_controller"));
    }

    public static final String MOD_ID = "alloy_forgery";

    public static ResourceLocation remap(@Nullable ResourceLocation original) {
        if (original == null) return null;

        if (original.getNamespace().equals(MOD_ID)) {
            original = ResourceLocation.fromNamespaceAndPath(AlloyForgery.MOD_ID, original.getPath());
        }

        return MAPPINGS.getOrDefault(original, original);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AlloyForgery.MOD_ID, path);
    }
}
