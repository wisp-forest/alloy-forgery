package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.StructEndec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record RawAlloyForgeRecipe(Map<Ingredient, Integer> inputs, OutputData outputData,
                                  int minForgeTier, int requiredFuel,
                                  Map<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride> overrideData) {

    public static Endec<Map<Ingredient, Integer>> INPUTS = CountedIngredient.ENDEC.listOf().xmap(list -> {
        Map<Ingredient, MutableInt> unprocessedData = new LinkedHashMap<>();

        for (CountedIngredient countedIngredient : list) {
            var countObj = unprocessedData.computeIfAbsent(
                    countedIngredient.ingredient(),
                    key -> new MutableInt(0));

            countObj.add(countedIngredient.count());
        }

        Map<Ingredient, Integer> data = new LinkedHashMap<>();

        unprocessedData.forEach((ingredient, mutableInt) -> data.put(ingredient, mutableInt.getValue()));

        return data;
    }, map -> {
        return map.entrySet().stream()
                .map(entry -> new CountedIngredient(entry.getKey(), entry.getValue())).toList();
    });

    public static Endec<AlloyForgeRecipe.PendingOverride> PENDING_OVERRIDE = StructEndecBuilder.of(
            BuiltInEndecs.ofRegistry(Registries.ITEM).optionalFieldOf("item", AlloyForgeRecipe.PendingOverride::item, () -> null),
            BuiltInEndecs.ofRegistry(Registries.ITEM).optionalFieldOf("id", orderride -> null, () -> null), //TODO: REMOVE LATER
            Endec.INT.fieldOf("count", AlloyForgeRecipe.PendingOverride::count),
            (item, item2, count) -> {
                if(item == null) item = item2;

                return new AlloyForgeRecipe.PendingOverride(item, count);
            }
    );

    public static StructEndec<RawAlloyForgeRecipe> ENDEC = StructEndecBuilder.of(
            INPUTS.validate(ingredientToCount -> {
                if (ingredientToCount.isEmpty()) {
                    throw new JsonSyntaxException("Inputs cannot be empty");
                } else if (ingredientToCount.keySet().size() > 10) {
                    throw new JsonSyntaxException("Recipe has more than 10 distinct input ingredients");
                } else if (ingredientToCount.values().stream().mapToInt(integer -> integer).sum() > (10 * 64)) {
                    throw new JsonSyntaxException("Recipe exceeded maximum input item count of " + (10 * 64));
                }
            }).fieldOf("inputs", RawAlloyForgeRecipe::inputs),
            OutputData.ENDEC.fieldOf("output", RawAlloyForgeRecipe::outputData),
            Endec.INT.fieldOf("min_forge_tier", RawAlloyForgeRecipe::minForgeTier),
            Endec.INT.fieldOf("fuel_per_tick", RawAlloyForgeRecipe::requiredFuel),
            PENDING_OVERRIDE.mapOf().xmap(rawData -> {
                Map<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride> data = new LinkedHashMap<>();

                rawData.forEach((s, pendingOverride) -> data.put(AlloyForgeRecipe.OverrideRange.fromString(s), pendingOverride));

                return data;
            }, data -> {
                Map<String, AlloyForgeRecipe.PendingOverride> rawData = new LinkedHashMap<>();

                data.forEach((range, pendingOverride) -> rawData.put(range.toString(), pendingOverride));

                return rawData;
            }).optionalFieldOf("overrides", RawAlloyForgeRecipe::overrideData, HashMap::new),
            RawAlloyForgeRecipe::new
    );

    public Pair<ItemStack, ImmutableMap<AlloyForgeRecipe.OverrideRange, ItemStack>> finalOutputData(Map<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride> overridesBuilder){
        if(outputData.outputItem() == null) return new Pair<>(ItemStack.EMPTY, ImmutableMap.of());

        final var builder = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStack>builder();

        final var outputStack = new ItemStack(outputData.outputItem(), outputData.count());

        for (var entry : overridesBuilder.entrySet()) {
            ItemStack stack;

            if (entry.getValue().isCountOnly()) {
                stack = outputStack.copy();
                stack.setCount(entry.getValue().count());
            } else {
                stack = entry.getValue().stack();
            }

            builder.put(entry.getKey(), stack);
        }

        return new Pair<>(outputStack, builder.build());
    }

    public AlloyForgeRecipe generateRecipe(){
        return generateRecipe(false);
    }

    public AlloyForgeRecipe generateRecipe(boolean isDataGenerated){
        var outputData = this.finalOutputData(this.overrideData);

        final var recipe = new AlloyForgeRecipe(Optional.of(this), this.inputs, outputData.getLeft(), minForgeTier, requiredFuel, outputData.getRight());

        if (!isDataGenerated && this.outputData.prioritisedOutput()) {
            AlloyForgeRecipe.PENDING_RECIPES.put(recipe, new AlloyForgeRecipe.PendingRecipeData(new Pair<>(this.outputData.defaultTag(), this.outputData.count()), this.overrideData));
        }

        return recipe;
    }
}
