package wraith.alloyforgery.forges;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import io.wispforest.owo.registration.ComplexRegistryAction;
import io.wispforest.owo.registration.RegistryHelper;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import io.wispforest.owo.serialization.format.json.JsonDeserializer;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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

    public static final Endec<Block> BLOCK_ENDEC = BuiltInEndecs.ofRegistry(Registries.BLOCK);

    public static Endec<ForgeDefinition> FORGE_DEFINITION = BuiltInEndecs.IDENTIFIER.xmap(
            identifier -> {
                return ForgeRegistry.getForgeDefinition(identifier)
                        .orElseThrow(() -> new IllegalStateException("Unable to locate ForgerDefinition with Identifier: [ID: " + identifier + "]"));
            }, forgeDefinition -> {
                for (var entry : ForgeRegistry.getForgeEntries()) {
                    if(entry.getValue() == forgeDefinition) return entry.getKey();
                }

                throw new IllegalStateException("A Given forge Definition was not found within the ForgeRegistry!");
            }
    );


    public static void loadAndEnqueue(Identifier id, JsonObject json) {
        final int forgeTier = JsonHelper.getInt(json, "tier");
        final float speedMultiplier = JsonHelper.getFloat(json, "speed_multiplier", 1);
        final int fuelCapacity = JsonHelper.getInt(json, "fuel_capacity", 48000);

        final var mainMaterialId = Identifier.tryParse(JsonHelper.getString(json, "material"));

        final var additionalMaterialIds = new ArrayList<Identifier>();
        JsonHelper.getArray(json, "additional_materials", new JsonArray()).forEach(jsonElement -> additionalMaterialIds.add(Identifier.tryParse(jsonElement.getAsString())));

        final var action = ComplexRegistryAction.Builder.create(() -> {
            final var mainMaterial = Registries.BLOCK.get(mainMaterialId);
            final var additionalMaterialsBuilder = new ImmutableList.Builder<Block>();
            additionalMaterialIds.forEach(identifier -> additionalMaterialsBuilder.add(Registries.BLOCK.get(identifier)));

            final var definition = new ForgeDefinition(forgeTier, speedMultiplier, fuelCapacity, mainMaterial, additionalMaterialsBuilder.build());

            ForgeRegistry.registerDefinition(id, definition);
        }).entry(mainMaterialId).entries(additionalMaterialIds).build();

        RegistryHelper.get(Registries.BLOCK).runWhenPresent(action);
    }

    public boolean isBlockValid(Block block) {
        return block == material || this.additionalMaterials.contains(block);
    }

    public JsonElement generateRecipe(Identifier id) {
        String recipe = RECIPE_PATTERN.replace("{material}", Registries.ITEM.getId(material.asItem()).toString());
        recipe = recipe.replace("{controller}", Registries.ITEM.getId(ForgeRegistry.getControllerBlock(id).get().asItem()).toString());

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

    private static <T, L extends List<T>> Endec<L> listOf(Endec<T> endec, ListBuilder<T, L> listBuilder) {
        return Endec.of((serializer, list) -> {
            try (var sequence = serializer.sequence(endec, list.size())) {
                list.forEach(sequence::element);
            }
        }, deserializer -> {
            var sequenceState = deserializer.sequence(endec);

            return listBuilder.create(sequenceState.estimatedSize(), sequenceState::forEachRemaining);
        });
    }

    public interface ListBuilder<T, L extends List<T>> {
        L create(int estimatedSize, Consumer<Consumer<T>> collector);

        static <T> ListBuilder<T, ImmutableList<T>> ofImmutable(){
            return (estimatedSize, collector) -> {
                var builder = ImmutableList.<T>builderWithExpectedSize(estimatedSize);

                collector.accept(builder::add);

                return builder.build();
            };
        }
    }
}
