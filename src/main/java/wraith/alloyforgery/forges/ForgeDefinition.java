package wraith.alloyforgery.forges;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.owo.registration.ComplexRegistryAction;
import io.wispforest.owo.registration.RegistryHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.AlloyForgery;

import java.util.ArrayList;

public record ForgeDefinition(int forgeTier,
                              float speedMultiplier,
                              int fuelCapacity,
                              int maxSmeltTime,
                              Block material,
                              ImmutableList<Block> additionalMaterials) {

    public static final int BASE_MAX_SMELT_TIME = 200;
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
                            "#": {
                                "item": "{material}"
                            },
                            "B": {
                                "item": "minecraft:blast_furnace"
                            }
                        },
                        "result": {
                            "item": "{controller}",
                            "count": 1
                        }
                    }
                    """;

    private ForgeDefinition(int forgeTier, float speedMultiplier, int fuelCapacity, Block material, ImmutableList<Block> additionalMaterials) {
        this(forgeTier, speedMultiplier, fuelCapacity, (int) (BASE_MAX_SMELT_TIME / speedMultiplier), material, additionalMaterials);
    }

    public static void loadAndEnqueue(Identifier id, JsonObject json) {
        final int forgeTier = JsonHelper.getInt(json, "tier");
        final float speedMultiplier = JsonHelper.getFloat(json, "speed_multiplier", 1);
        final int fuelCapacity = JsonHelper.getInt(json, "fuel_capacity", 48000);

        final var mainMaterialId = Identifier.tryParse(JsonHelper.getString(json, "material"));

        final var additionalMaterialIds = new ArrayList<Identifier>();
        JsonHelper.getArray(json, "additional_materials", new JsonArray()).forEach(jsonElement -> additionalMaterialIds.add(Identifier.tryParse(jsonElement.getAsString())));

        final var action = ComplexRegistryAction.Builder.create(() -> {
            final var mainMaterial = Registry.BLOCK.get(mainMaterialId);
            final var additionalMaterialsBuilder = new ImmutableList.Builder<Block>();
            additionalMaterialIds.forEach(identifier -> additionalMaterialsBuilder.add(Registry.BLOCK.get(identifier)));

            final var definition = new ForgeDefinition(forgeTier, speedMultiplier, fuelCapacity, mainMaterial, additionalMaterialsBuilder.build());

            ForgeRegistry.registerDefinition(id, definition);
        }).entry(mainMaterialId).entries(additionalMaterialIds).build();

        RegistryHelper.get(Registry.BLOCK).runWhenPresent(action);
    }

    public boolean isBlockValid(Block block) {
        return block == material || this.additionalMaterials.contains(block);
    }

    public JsonElement generateRecipe(Identifier id) {
        String recipe = RECIPE_PATTERN.replace("{material}", Registry.ITEM.getId(material.asItem()).toString());
        recipe = recipe.replace("{controller}", Registry.ITEM.getId(ForgeRegistry.getControllerBlock(id).get().asItem()).toString());

        return ForgeRegistry.GSON.fromJson(recipe, JsonObject.class);
    }

    @Override
    public String toString() {
        return "ForgeDefinition{" +
                "forgeTier=" + forgeTier +
                ", speedMultiplier=" + speedMultiplier +
                ", fuelCapacity=" + fuelCapacity +
                ", maxSmeltTime=" + maxSmeltTime +
                ", material=" + material +
                ", additionalMaterials=" + additionalMaterials +
                '}';
    }
}
