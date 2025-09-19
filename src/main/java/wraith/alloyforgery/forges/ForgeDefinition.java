package wraith.alloyforgery.forges;

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
import net.minecraft.block.Block;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.utils.RecipeInjector;
import wraith.alloyforgery.utils.data.EndecableModDataLoader;
import java.util.*;

public record ForgeDefinition(Block material, ImmutableList<Block> additionalMaterials, boolean blockEntity) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ForgeDefinition(Block material, ImmutableList<Block> additionalMaterials) {
        this(material, additionalMaterials, false);
    }

    public static Endec<ForgeDefinition> FORGE_DEFINITION = MinecraftEndecs.IDENTIFIER.xmap(
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

    @Deprecated
    public static void loadAndEnqueue(Identifier id, JsonObject json) {
        final int forgeTier = JsonHelper.getInt(json, "tier");
        final float speedMultiplier = JsonHelper.getFloat(json, "speed_multiplier", 1);
        final int fuelCapacity = JsonHelper.getInt(json, "fuel_capacity", 48000);

        // TODO: ADD DEPRECATION WARNING ABOUT LOADING TIER INFO
        var tier = new ForgeTier(forgeTier, speedMultiplier, fuelCapacity, Optional.empty());

        final var mainMaterialId = Identifier.tryParse(JsonHelper.getString(json, "material"));

        final var additionalMaterialIds = new ArrayList<Identifier>();
        JsonHelper.getArray(json, "additional_materials", new JsonArray()).forEach(jsonElement -> additionalMaterialIds.add(Identifier.tryParse(jsonElement.getAsString())));

        loadAndEnqueue(id, new RawForgeDefinition(mainMaterialId, additionalMaterialIds, false));
    }

    private static void loadAndEnqueue(Identifier id, RawForgeDefinition rawForgeDefinition) {
        final var action = ComplexRegistryAction.Builder.create(() -> {
            final var mainMaterial = Registries.BLOCK.get(rawForgeDefinition.materialId());
            final var additionalMaterialsBuilder = new ImmutableList.Builder<Block>();
            rawForgeDefinition.additionalMaterialIds().forEach(identifier -> additionalMaterialsBuilder.add(Registries.BLOCK.get(identifier)));

            final var definition = new ForgeDefinition(mainMaterial, additionalMaterialsBuilder.build());

            ForgeRegistry.registerDefinition(id, definition);
        }).entries(rawForgeDefinition.blockIds()).build();

        RegistryHelper.get(Registries.BLOCK).runWhenPresent(action);
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
        String recipe = RECIPE_PATTERN.replace("{material}", Registries.ITEM.getId(material.asItem()).toString());
        recipe = recipe.replace("{controller}", Registries.ITEM.getId(ForgeRegistry.getControllerBlock(id).get().asItem()).toString());

        return ForgeRegistry.GSON.fromJson(recipe, JsonObject.class);
    }

    public static void initLoaders() {
        EndecableModDataLoader.of(
            AlloyForgery.id("old_forge_definition_loader"),
            "alloy_forges",
            GsonEndec.INSTANCE.xmap(JsonElement::getAsJsonObject, jsonObject -> jsonObject),
            ForgeDefinition::loadAndEnqueue
        ).load();

        EndecableModDataLoader.of(
            AlloyForgery.id("forge_definition_loader"),
            "alloy_forge/forge",
            RawForgeDefinition.ENDEC,
            ForgeDefinition::loadAndEnqueue
        ).load();

        RecipeInjector.ADD_RECIPES.register(instance -> {
            for (var forgeEntry : ForgeRegistry.getForgeEntries()) {
                var id = forgeEntry.getKey();

                try {
                    var recipe = RecipeSerializer.SHAPED.codec()
                            .codec()
                            .decode(RegistryOps.of(JsonOps.INSTANCE, instance.lookup()), forgeEntry.getValue().generateRecipe(id))
                            .getOrThrow(string -> new IllegalStateException("Unable to generate recipe for given ForgeDefinition [" + id + "]: " + string))
                            .getFirst();

                    instance.addRecipe(id.withSuffixedPath("_recipe"), recipe);
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
