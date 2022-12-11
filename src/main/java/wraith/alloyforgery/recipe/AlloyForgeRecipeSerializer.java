package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.LinkedHashMap;
import java.util.Map;

public class AlloyForgeRecipeSerializer implements RecipeSerializer<AlloyForgeRecipe> {

    public static final AlloyForgeRecipeSerializer INSTANCE = new AlloyForgeRecipeSerializer();

    @Override
    public AlloyForgeRecipe read(Identifier id, JsonObject json) {

        Map<IngredientData, MutableInt> ingredientDataToCount = new LinkedHashMap<>();

        for (var entry : JsonHelper.getArray(json, "inputs")) {
            var object = entry.getAsJsonObject();

            IngredientData inputData;

            if (object.has("item")) {
                inputData = new IngredientData(object.get("item").getAsString(), false);
            } else if (object.has("tag")) {
                inputData = new IngredientData(object.get("tag").getAsString(), true);
            } else {
                throw new JsonSyntaxException("An ingredient entry needs either a tag or an item");
            }

            ingredientDataToCount.computeIfAbsent(inputData, stringStringPair -> new MutableInt(0))
                    .add(object.keySet().contains("count") ? object.get("count").getAsInt() : 1);
        }

        if (ingredientDataToCount.isEmpty()) throw new JsonSyntaxException("Inputs cannot be empty");
        if (ingredientDataToCount.keySet().size() > 10) {
            throw new JsonSyntaxException("Recipe has more than 10 distinct input ingredients");
        }

        var ingredientToCount = new LinkedHashMap<Ingredient, Integer>();
        for (var entry : ingredientDataToCount.entrySet()) {
            final var ingredientData = entry.getKey();

            Ingredient ingredient;
            Identifier identifier = new Identifier(ingredientData.data());

            ingredient = ingredientData.isTag()
                    ? Ingredient.fromTag(TagKey.of(RegistryKeys.ITEM, identifier))
                    : Ingredient.ofItems(JsonHelper.asItem(new JsonPrimitive(ingredientData.data()), identifier.toString()));

            ingredientToCount.put(ingredient, entry.getValue().intValue());
        }

        if (ingredientToCount.values().stream().mapToInt(integer -> integer).sum() > (10 * 64)) {
            throw new JsonSyntaxException("Recipe exceeded maximum input item count of " + (10 * 64));
        }

        final var outputObject = JsonHelper.getObject(json, "output");

        boolean prioritisedOutput = false;
        ItemStack outputStack = null;
        Pair<TagKey<Item>, Integer> defaultTag = null;

        if (outputObject.has("priority")) {
            if (!outputObject.has("default")) {
                throw new JsonSyntaxException("Priority-based recipes must declare a 'default' tag");
            }

            prioritisedOutput = true;

            for (var itemElement : JsonHelper.getArray(outputObject, "priority")) {
                var maybeItem = Registries.ITEM.getOrEmpty(Identifier.tryParse(itemElement.getAsString()));

                if (maybeItem.isPresent()) {
                    outputStack = maybeItem.get().getDefaultStack();
                    outputStack.setCount(JsonHelper.getInt(outputObject, "count"));
                    break;
                }
            }

            if (outputStack == null) {
                defaultTag = new Pair<>(TagKey.of(RegistryKeys.ITEM, new Identifier(JsonHelper.getString(outputObject, "default"))), JsonHelper.getInt(outputObject, "count"));
            }
        } else {
            outputStack = getItemStack(outputObject);
        }

        final int minForgeTier = JsonHelper.getInt(json, "min_forge_tier");
        final int requiredFuel = JsonHelper.getInt(json, "fuel_per_tick");

        final var overrides = JsonHelper.getObject(json, "overrides", new JsonObject());
        final var overridesBuilder = ImmutableMap.<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride>builder();

        for (var entry : overrides.entrySet()) {
            final var overrideString = entry.getKey();
            AlloyForgeRecipe.OverrideRange overrideRange = null;

            if (overrideString.matches("\\d+\\+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString.substring(0, overrideString.length() - 1)));
            } else if (overrideString.matches("\\d+ to \\d+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString.substring(0, overrideString.indexOf(" "))), Integer.parseInt(overrideString.substring(overrideString.lastIndexOf(" ") + 1, overrideString.length())));
            } else if (overrideString.matches("\\d+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString), Integer.parseInt(overrideString));
            }

            if (overrideRange == null) throw new JsonSyntaxException("Invalid override range token: " + overrideString);

            final var overrideObject = entry.getValue().getAsJsonObject();

            if (overrideObject.has("id")) {
                overridesBuilder.put(overrideRange, AlloyForgeRecipe.PendingOverride.ofStack(getItemStack(overrideObject)));
            } else {
                overridesBuilder.put(overrideRange, AlloyForgeRecipe.PendingOverride.onlyCount(JsonHelper.getInt(overrideObject, "count")));
            }
        }

        var effectiveOverrides = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStack>of();
        if (!prioritisedOutput) {
            final var builder = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStack>builder();

            for (var entry : overridesBuilder.build().entrySet()) {
                if (entry.getValue().isCountOnly()) {
                    var stack = outputStack.copy();
                    stack.setCount(entry.getValue().count());
                    builder.put(entry.getKey(), stack);
                } else {
                    builder.put(entry.getKey(), entry.getValue().stack());
                }
            }

            effectiveOverrides = builder.build();
        }

        final var recipe = new AlloyForgeRecipe(id, ingredientToCount, outputStack, minForgeTier, requiredFuel, effectiveOverrides);

        if (prioritisedOutput) {
            AlloyForgeRecipe.PENDING_RECIPES.put(recipe, new AlloyForgeRecipe.PendingRecipeData(defaultTag, overridesBuilder.build()));
        }

        return recipe;
    }

    private ItemStack getItemStack(JsonObject json) {
        final var item = JsonHelper.getItem(json, "id");
        final var count = JsonHelper.getInt(json, "count", 1);

        return new ItemStack(item, count);
    }

    @Override
    public AlloyForgeRecipe read(Identifier id, PacketByteBuf buf) {
        final var inputs = buf.readMap(value -> new LinkedHashMap<>(), Ingredient::fromPacket, PacketByteBuf::readVarInt);

        final var output = buf.readItemStack();

        final int minForgeTier = buf.readVarInt();
        final int requiredFuel = buf.readVarInt();

        final var overrides = buf.readMap(buf1 -> new AlloyForgeRecipe.OverrideRange(buf1.readVarInt(), buf1.readVarInt()), PacketByteBuf::readItemStack);

        return new AlloyForgeRecipe(id, inputs, output, minForgeTier, requiredFuel, ImmutableMap.copyOf(overrides));
    }

    @Override
    public void write(PacketByteBuf buf, AlloyForgeRecipe recipe) {
        buf.writeMap(recipe.getIngredientsMap(), (buf1, ingredient) -> ingredient.write(buf1), PacketByteBuf::writeVarInt);

        buf.writeItemStack(recipe.getOutput());

        buf.writeVarInt(recipe.getMinForgeTier());
        buf.writeVarInt(recipe.getFuelPerTick());

        buf.writeMap(recipe.getTierOverrides(), (buf1, overrideRange) -> {
            buf1.writeVarInt(overrideRange.lowerBound());
            buf1.writeVarInt(overrideRange.upperBound());
        }, PacketByteBuf::writeItemStack);
    }

    private record IngredientData(String data, boolean isTag) {}
}
