package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;

public class AlloyForgeRecipeSerializer implements RecipeSerializer<AlloyForgeRecipe> {

    public static final AlloyForgeRecipeSerializer INSTANCE = new AlloyForgeRecipeSerializer();

    @Override
    public AlloyForgeRecipe read(Identifier id, JsonObject json) {

        final var inputs = new ArrayList<Ingredient>();

        JsonHelper.getArray(json, "inputs").forEach(jsonElement -> inputs.add(Ingredient.fromJson(jsonElement)));
        if (inputs.isEmpty()) throw new JsonSyntaxException("Inputs cannot be empty");

        final var outputStack = getItemStack(JsonHelper.getObject(json, "output"));

        final int minForgeTier = JsonHelper.getInt(json, "min_forge_tier");
        final int requiredFuel = JsonHelper.getInt(json, "fuel_per_tick");

        final var overridesJson = JsonHelper.getObject(json, "overrides", new JsonObject());
        final var overridesBuilder = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStack>builder();

        for (var entry : overridesJson.entrySet()) {

            final var overrideString = entry.getKey();
            AlloyForgeRecipe.OverrideRange overrideRange = null;

            if(overrideString.matches("\\d+\\+")){
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString.substring(0, overrideString.length() - 1)));
            } else if(overrideString.matches("\\d+ to \\d+")){
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString.substring(0, overrideString.indexOf(" "))), Integer.parseInt(overrideString.substring(overrideString.lastIndexOf(" ") + 1, overrideString.length())));
            } else if(overrideString.matches("\\d+")){
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(overrideString), Integer.parseInt(overrideString));
            }

            if(overrideRange == null){
                throw new JsonSyntaxException("Invalid override range token: " + overrideString);
            }

            overridesBuilder.put(overrideRange, getItemStack(entry.getValue().getAsJsonObject()));
        }

        return new AlloyForgeRecipe(id, inputs, outputStack, minForgeTier, requiredFuel, overridesBuilder.build());
    }

    private ItemStack getItemStack(JsonObject json) {
        final var item = JsonHelper.getItem(json, "id");
        final var count = JsonHelper.getInt(json, "count", 1);

        return new ItemStack(item, count);
    }

    @Override
    public AlloyForgeRecipe read(Identifier id, PacketByteBuf buf) {

        final var inputs = buf.readCollection(value -> new ArrayList<>(), Ingredient::fromPacket);
        final var output = buf.readItemStack();

        final int minForgeTier = buf.readVarInt();
        final int requiredFuel = buf.readVarInt();

        final var overrides = buf.readMap(buf1 -> new AlloyForgeRecipe.OverrideRange(buf1.readVarInt(), buf1.readVarInt()), PacketByteBuf::readItemStack);

        return new AlloyForgeRecipe(id, inputs, output, minForgeTier, requiredFuel, ImmutableMap.copyOf(overrides));
    }

    @Override
    public void write(PacketByteBuf buf, AlloyForgeRecipe recipe) {
        buf.writeCollection(recipe.getIngredients(), (buf1, ingredient) -> ingredient.write(buf1));
        buf.writeItemStack(recipe.getOutput());

        buf.writeVarInt(recipe.getMinForgeTier());
        buf.writeVarInt(recipe.getFuelPerTick());

        buf.writeMap(recipe.getTierOverrides(), (buf1, overrideRange) -> {
            buf1.writeVarInt(overrideRange.lowerBound());
            buf1.writeVarInt(overrideRange.upperBound());
        }, PacketByteBuf::writeItemStack);
    }
}
