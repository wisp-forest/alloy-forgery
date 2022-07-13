package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class AlloyForgeRecipeSerializer implements RecipeSerializer<AlloyForgeRecipe> {

    public static final AlloyForgeRecipeSerializer INSTANCE = new AlloyForgeRecipeSerializer();

    @Override
    public AlloyForgeRecipe read(Identifier id, JsonObject json) {

        Map<Pair<String, String>, Integer> jsonObjectIntegerMap = new LinkedHashMap<>();

        for(JsonElement entry : JsonHelper.getArray(json, "inputs")) {
            JsonObject object = entry.getAsJsonObject();

            Pair<String, String> recipeInput;

            int recipeInputCount = 1;

            if(object.keySet().contains("item")){
                recipeInput = new BetterPair<>(object.get("item").getAsString(), "item");
            } else if(object.keySet().contains("tag")){
                recipeInput = new BetterPair<>(object.get("tag").getAsString(), "tag");
            } else {
                throw new JsonSyntaxException("Alloy Forge Recipes only allow for item or tag inputs!");
            }

            if(object.keySet().contains("count")){
                recipeInputCount = object.get("count").getAsInt();
            }

            if(jsonObjectIntegerMap.containsKey(recipeInput)) {
                jsonObjectIntegerMap.replace(recipeInput, jsonObjectIntegerMap.get(recipeInput) + recipeInputCount);
            } else {
                jsonObjectIntegerMap.put(recipeInput, recipeInputCount);
            }
        }

        if (jsonObjectIntegerMap.isEmpty()) throw new JsonSyntaxException("Inputs cannot be empty");

        Map<Ingredient, Integer> ingredientIntegerMap = new LinkedHashMap<>();

        for(Map.Entry<Pair<String, String>, Integer> entry : jsonObjectIntegerMap.entrySet()) {
            Ingredient ingredient;

            Identifier identifier = Identifier.tryParse(entry.getKey().getLeft());

            if(identifier == null){
                throw new JsonSyntaxException(entry.getKey().getLeft() + " is a invalid Identifier");
            }

            if(Objects.equals(entry.getKey().getRight(), "item")){
                Optional<Item> item = Registry.ITEM.getOrEmpty(identifier);

                if(item.isPresent()) {
                    ingredient = Ingredient.ofItems(item.get());
                } else {
                    throw new JsonSyntaxException("Item identifier [" + entry.getKey().getLeft() + "] could not be found within the item Registry");
                }
            } else {
                ingredient = Ingredient.fromTag(TagKey.of(Registry.ITEM_KEY, identifier));
            }

            ingredientIntegerMap.put(ingredient, entry.getValue());
        }

        if(ingredientIntegerMap.keySet().size() > 10)
            throw new JsonSyntaxException("The number of Unique ingredients was higher than the max allowed which is 10");

        int totalAmountOFIngredients = ingredientIntegerMap.values().stream().mapToInt(integer -> integer).sum();

        if(totalAmountOFIngredients > (10 * 64))
            throw new JsonSyntaxException("The total count of the entire recipe exceeded the max count of " + (10 * 64));

        final var outputStack = getItemStack(JsonHelper.getObject(json, "output"));

        final int minForgeTier = JsonHelper.getInt(json, "min_forge_tier");
        final int requiredFuel = JsonHelper.getInt(json, "fuel_per_tick");

        final var overridesJson = JsonHelper.getObject(json, "overrides", new JsonObject());
        final var overridesBuilder = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStack>builder();

        for (var entry : overridesJson.entrySet()) {

            final var overrideString = entry.getKey();
            AlloyForgeRecipe.OverrideRange overrideRange = null;

            if (overrideString.matches("\\d+\\+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString.substring(0, overrideString.length() - 1)));
            } else if (overrideString.matches("\\d+ to \\d+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString.substring(0, overrideString.indexOf(" "))), Integer.parseInt(overrideString.substring(overrideString.lastIndexOf(" ") + 1, overrideString.length())));
            } else if (overrideString.matches("\\d+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString), Integer.parseInt(overrideString));
            }

            if (overrideRange == null) {
                throw new JsonSyntaxException("Invalid override range token: " + overrideString);
            }

            overridesBuilder.put(overrideRange, getItemStack(entry.getValue().getAsJsonObject()));
        }

        return new AlloyForgeRecipe(id, ingredientIntegerMap, outputStack, minForgeTier, requiredFuel, overridesBuilder.build());
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

    private static class BetterPair<A, B> extends Pair<A, B> {

        public BetterPair(A left, B right) {
            super(left, right);
        }

        @Override
        public int hashCode() {
            int hash = getLeft().hashCode();

            hash = 31 * hash + getRight().hashCode();

            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Pair<?, ?> pair){
                return pair.getLeft().equals(getLeft()) && pair.getRight().equals(getRight());
            }

            return super.equals(obj);
        }

        @Override
        public String toString() {
            return this.getLeft().toString() + " / " + this.getRight().toString();
        }
    }
}
