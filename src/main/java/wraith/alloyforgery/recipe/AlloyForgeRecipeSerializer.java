package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

public class AlloyForgeRecipeSerializer implements RecipeSerializer<AlloyForgeRecipe> {

    public static final AlloyForgeRecipeSerializer INSTANCE = new AlloyForgeRecipeSerializer();

    @Override
    public AlloyForgeRecipe read(Identifier id, JsonObject json) {

        Map<Pair<String, String>, MutableInt> ingredientDataToCount = new LinkedHashMap<>();

        for(JsonElement entry : JsonHelper.getArray(json, "inputs")) {
            JsonObject object = entry.getAsJsonObject();

            Pair<String, String> recipeInput;

            if(object.keySet().contains("item")){
                recipeInput = new HashPair<>(object.get("item").getAsString(), "item");
            } else if(object.keySet().contains("tag")){
                recipeInput = new HashPair<>(object.get("tag").getAsString(), "tag");
            } else {
                throw new JsonSyntaxException("Alloy Forge Recipes only allow for item or tag inputs!");
            }

            ingredientDataToCount.putIfAbsent(recipeInput, new MutableInt(1))
                    .add(object.keySet().contains("count") ? object.get("count").getAsInt() : 1);
        }

        if (ingredientDataToCount.isEmpty()) throw new JsonSyntaxException("Inputs cannot be empty");

        Map<Ingredient, Integer> ingredientToCount = new LinkedHashMap<>();

        for(var entry : ingredientDataToCount.entrySet()) {
            Ingredient ingredient;

            Identifier identifier = Identifier.tryParse(entry.getKey().getLeft());

            if(identifier == null){
                throw new JsonSyntaxException(entry.getKey().getLeft() + " is a invalid Identifier");
            }

            if(Objects.equals(entry.getKey().getRight(), "item")){
                ingredient = Ingredient.ofItems(JsonHelper.asItem(new JsonPrimitive(entry.getKey().getLeft()), identifier.toString()));
            } else {
                ingredient = Ingredient.fromTag(TagKey.of(Registry.ITEM_KEY, identifier));
            }

            ingredientToCount.put(ingredient, entry.getValue().intValue());
        }

        if(ingredientToCount.keySet().size() > 10) {
            throw new JsonSyntaxException("The number of Unique ingredients was higher than the max allowed which is 10");
        }

        final int totalAmountOFIngredients = ingredientToCount.values().stream().mapToInt(integer -> integer).sum();

        if(totalAmountOFIngredients > (10 * 64)) {
            throw new JsonSyntaxException("The total count of the entire recipe exceeded the max count of " + (10 * 64));
        }

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

        return new AlloyForgeRecipe(id, ingredientToCount, outputStack, minForgeTier, requiredFuel, overridesBuilder.build());
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

    private static class HashPair<A, B> extends Pair<A, B> {

        public HashPair(A left, B right) {
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
