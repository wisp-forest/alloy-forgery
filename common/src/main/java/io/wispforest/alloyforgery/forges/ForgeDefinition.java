package io.wispforest.alloyforgery.forges;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.gson.GsonEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.registration.ComplexRegistryAction;
import io.wispforest.owo.registration.RegistryHelper;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.utils.RecipeInjector;
import io.wispforest.alloyforgery.utils.data.EndecableModDataLoader;
import java.util.*;

public record ForgeDefinition(Block material, ImmutableList<Block> additionalMaterials, boolean blockEntity) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ForgeDefinition(Block material, ImmutableList<Block> additionalMaterials) {
        this(material, additionalMaterials, false);
    }

    public static final Endec<ForgeDefinition> FORGE_DEFINITION = MinecraftEndecs.IDENTIFIER.xmap(
        identifier -> {
            return ForgeRegistry.getForgeDefinition(identifier)
                .orElseThrow(() -> new IllegalStateException("Unable to locate ForgerDefinition with Identifier: [ID: " + identifier + "]"));
        }, forgeDefinition -> {
            return forgeDefinition.id()
                .orElseThrow(() -> new IllegalStateException("A Given forge Definition was not found within the ForgeRegistry!"));
        }
    );

    public Optional<Identifier> id() {
        return ForgeRegistry.getId(this);
    }

    @ApiStatus.Internal
    public static final Map<Identifier, ForgeTier> legacyForgeDefinitionIdToTier = new HashMap<>();

    @Deprecated
    @ApiStatus.Internal
    public static void loadAndEnqueue(Identifier id, JsonObject json) {
        LOGGER.warn("A given Forge Definition '{}' has been loaded though a deprecated manor, please bug the author to switch over to the new system.", id);
        final int forgeTier = GsonHelper.getAsInt(json, "tier");
        final float speedMultiplier = GsonHelper.getAsFloat(json, "speed_multiplier", 1);
        final int fuelCapacity = GsonHelper.getAsInt(json, "fuel_capacity", 48000);

        // TODO: ADD DEPRECATION WARNING ABOUT LOADING TIER INFO
        var tier = new ForgeTier(id, forgeTier, speedMultiplier, fuelCapacity, Optional.empty());

        legacyForgeDefinitionIdToTier.put(id, tier);

        final var mainMaterialId = Identifier.tryParse(GsonHelper.getAsString(json, "material"));

        final var additionalMaterialIds = new ArrayList<Identifier>();
        GsonHelper.getAsJsonArray(json, "additional_materials", new JsonArray()).forEach(jsonElement -> additionalMaterialIds.add(Identifier.tryParse(jsonElement.getAsString())));

        loadAndEnqueue(id, new RawForgeDefinition(mainMaterialId, additionalMaterialIds, false));
    }

    @ApiStatus.Internal
    private static void loadAndEnqueue(Identifier id, RawForgeDefinition rawForgeDefinition) {
        final var action = ComplexRegistryAction.Builder.create(() -> {
            final var mainMaterial = BuiltInRegistries.BLOCK.getValue(rawForgeDefinition.materialId());
            final var additionalMaterialsBuilder = new ImmutableList.Builder<Block>();
            rawForgeDefinition.additionalMaterialIds().forEach(identifier -> additionalMaterialsBuilder.add(BuiltInRegistries.BLOCK.getValue(identifier)));

            final var definition = new ForgeDefinition(mainMaterial, additionalMaterialsBuilder.build());

            ForgeRegistry.registerDefinition(id, definition);
        }).entries(rawForgeDefinition.blockIds()).build();

        RegistryHelper.get(BuiltInRegistries.BLOCK).runWhenPresent(action);
    }

    public boolean isBlockValid(Block block) {
        return block == material || this.additionalMaterials.contains(block);
    }

    // TODO - kill
    //why kubejs why
    private static final String RECIPE_PATTERN =
        """
            {
                "type": "minecraft:crafting_shaped",
                "pattern": [
                    "###",
                    "#B#",
                    "###"
                ],
                "key": {
                    "#": "{material}",
                    "B": "minecraft:blast_furnace"
                },
                "result": {
                    "id": "{controller}",
                    "count": 1
                }
            }
            """;

    public JsonElement generateRecipe(Identifier id) {
        String recipe = RECIPE_PATTERN.replace("{material}", BuiltInRegistries.ITEM.getKey(material.asItem()).toString());
        recipe = recipe.replace("{controller}", BuiltInRegistries.ITEM.getKey(ForgeRegistry.getControllerBlock(id).get().asItem()).toString());

        return ForgeRegistry.GSON.fromJson(recipe, JsonObject.class);
    }

    public static void runDataLoaders() {
        EndecableModDataLoader.of(
            AlloyForgery.id("old_forge_definition_loader"),
            "alloy_forges",
            GsonEndec.INSTANCE.xmap(JsonElement::getAsJsonObject, jsonObject -> jsonObject),
            ForgeDefinition::loadAndEnqueue
        ).load();

        EndecableModDataLoader.of(
            AlloyForgery.id("forge_definition_loader"),
            "alloy_forge/controller",
            RawForgeDefinition.ENDEC,
            ForgeDefinition::loadAndEnqueue
        ).load();
    }

    public static void injectRecipeAdditions() {
        RecipeInjector.ADD_RECIPES.register(instance -> {
            for (var forgeEntry : ForgeRegistry.getForgeEntries()) {
                var id = forgeEntry.getKey();

                try {
                    var recipe = ShapedRecipe.CODEC
                            .decode(RegistryOps.create(JsonOps.INSTANCE, instance.lookup()), forgeEntry.getValue().generateRecipe(id))
                            .getOrThrow(string -> new IllegalStateException("Unable to generate recipe for given ForgeDefinition [" + id + "]: " + string))
                            .getFirst();

                    instance.addRecipe(id.withSuffix("_recipe"), recipe);
                } catch (Throwable e) {
                    LOGGER.error("{} recipe had a issue!", id, e);
                }
            }
        });
    }

    private record RawForgeDefinition(Identifier materialId, List<Identifier> additionalMaterialIds, boolean isBlockEntity) {
        public static final StructEndec<RawForgeDefinition> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.fieldOf("material", RawForgeDefinition::materialId),
            MinecraftEndecs.IDENTIFIER.listOf().optionalFieldOf("additional_materials", RawForgeDefinition::additionalMaterialIds, List.of()),
            Endec.BOOLEAN.optionalFieldOf("is_block_entity", RawForgeDefinition::isBlockEntity, false),
            RawForgeDefinition::new
        );

        public List<Identifier> blockIds() {
            var list = new ArrayList<>(additionalMaterialIds);
            list.addFirst(materialId);
            return list;
        }
    }

    @Override
    public String toString() {
        return "ForgeDefinition{" +
            "material=" + material +
            ", additionalMaterials=" + additionalMaterials +
            '}';
    }
}
