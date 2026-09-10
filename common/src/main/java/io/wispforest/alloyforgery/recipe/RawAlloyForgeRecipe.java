package io.wispforest.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.utils.EndecUtils;
import io.wispforest.alloyforgery.utils.LoaderPlatformUtils;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableInt;
import java.util.*;

public record RawAlloyForgeRecipe(
    Map<Ingredient, Integer> inputs,
    OutputData outputData,
    int minForgeTier,
    int requiredFuel,
    Map<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride> overrideData
) {

    public static Endec<Map<Ingredient, Integer>> INPUTS = CountedIngredient.ENDEC.listOf().xmap(list -> {
        var unprocessedData = new Object2ObjectLinkedOpenHashMap<Ingredient, MutableInt>();

        for (CountedIngredient countedIngredient : list) {
            var ingredient = countedIngredient.ingredient();

            if (unprocessedData.containsKey(ingredient) && (AlloyForgery.CONFIG.strictRecipeChecks() || LoaderPlatformUtils.INSTANCE.isDevelopmentEnvironment())) {
                var jsonData = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient)
                    .result()
                    .map(JsonElement::toString)
                    .orElse("Error Unknown");

                throw new IllegalStateException("Duplicate Ingredient Entry! Merge all ingredients of [" + jsonData + "] into a single entry and add a count!");
            }

            unprocessedData.computeIfAbsent(ingredient, key -> new MutableInt(0))
                .add(countedIngredient.count());
        }

        var data = new LinkedHashMap<Ingredient, Integer>();

        unprocessedData.forEach((ingredient, mutableInt) -> data.put(ingredient, mutableInt.getValue()));

        return data;
    }, map -> {
        return map.entrySet().stream()
            .map(entry -> new CountedIngredient(entry.getKey(), entry.getValue())).toList();
    });

    public static Endec<AlloyForgeRecipe.PendingOverride> PENDING_OVERRIDE = StructEndecBuilder.of(
        MinecraftEndecs.ofRegistry(BuiltInRegistries.ITEM).optionalFieldOf("item", AlloyForgeRecipe.PendingOverride::item, () -> null),
        MinecraftEndecs.ofRegistry(BuiltInRegistries.ITEM).optionalFieldOf("id", orderride -> null, () -> null), //TODO: REMOVE LATER
        Endec.INT.fieldOf("count", AlloyForgeRecipe.PendingOverride::count),
        EndecUtils.optionalFieldOf("components", CodecUtils.toEndec(DataComponentPatch.CODEC), AlloyForgeRecipe.PendingOverride::components, () -> DataComponentPatch.EMPTY),
        (item, item2, count, components) -> {
            if (item == null) item = item2;

            return new AlloyForgeRecipe.PendingOverride(item, count, components);
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

    public Tuple<ItemStackTemplate, ImmutableMap<AlloyForgeRecipe.OverrideRange, ItemStackTemplate>> finalOutputData(Map<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride> overridesBuilder) {
        if (outputData.outputItem() == null) throw new NullPointerException("output item can not be null");

        final var builder = ImmutableMap.<AlloyForgeRecipe.OverrideRange, ItemStackTemplate>builder();

        final var outputStack = new ItemStackTemplate(outputData.outputItem(), outputData.count());

        for (var entry : overridesBuilder.entrySet()) {
            ItemStackTemplate template;
            var override = entry.getValue();

            if (entry.getValue().isCountOnly()) {
                template = outputStack.withCount(entry.getValue().count());
            } else {
                template = new ItemStackTemplate(override.item(), override.count());
            }

            if (!entry.getValue().components().isEmpty()) {
                template = new ItemStackTemplate(override.item(), override.components());
            }

            builder.put(entry.getKey(), template);
        }

        return new Tuple<>(outputStack, builder.build());
    }

    public AlloyForgeRecipe generateRecipe() {
        return generateRecipe(false);
    }

    public AlloyForgeRecipe generateRecipe(boolean isDataGenerated) {
        var outputData = this.finalOutputData(this.overrideData);

        final var recipe = new AlloyForgeRecipe(Optional.of(this), this.inputs, outputData.getA(), minForgeTier, requiredFuel, outputData.getB());

        if (!isDataGenerated && this.outputData.prioritisedOutput()) {
            AlloyForgeRecipe.PENDING_RECIPES.put(recipe, new AlloyForgeRecipe.PendingRecipeData(new Tuple<>(this.outputData.defaultTag(), this.outputData.count()), this.overrideData));
        }

        return recipe;
    }
}
