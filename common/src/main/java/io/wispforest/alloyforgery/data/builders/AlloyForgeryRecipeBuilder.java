package io.wispforest.alloyforgery.data.builders;

import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.data.providers.RecipeExporterConditionWrapper;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import io.wispforest.alloyforgery.recipe.AlloyForgeRecipe;
import io.wispforest.alloyforgery.recipe.OutputData;
import io.wispforest.alloyforgery.recipe.RawAlloyForgeRecipe;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class AlloyForgeryRecipeBuilder implements RecipeBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, Criterion<?>> advancementBuilder = new LinkedHashMap<>();
    private String group = "";

    private final Set<TagKey<Item>> inputTags = new LinkedHashSet<>();

    @Nullable
    private final TagKey<Item> outputTag;
    @Nullable
    private final ItemLike outputItem;

    //private final Ingredient output;
    private final int outputCount;

    private final Map<Ingredient, Integer> inputs = new LinkedHashMap<>();

    private final Map<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride> ranges = new LinkedHashMap<>();

    private final List<Identifier> priorities = new ArrayList<>();

    private int minimumTier = 1;
    private int fuelPerTick = 5;

    private AlloyForgeryRecipeBuilder(@Nullable TagKey<Item> outputTag, @Nullable ItemLike outputItem, int outputCount) {
        this.outputTag = outputTag;
        this.outputItem = outputItem;

        this.outputCount = outputCount;
    }

    //--------------------------------------------------------------------

    public static AlloyForgeryRecipeBuilder create(ItemLike output) {
        return create(output, 1);
    }

    public static AlloyForgeryRecipeBuilder create(ItemLike output, int outputCount) {
        return new AlloyForgeryRecipeBuilder(null, output, outputCount);
    }

    public static AlloyForgeryRecipeBuilder create(TagKey<Item> outputTag) {
        return create(outputTag, 1);
    }

    public static AlloyForgeryRecipeBuilder create(TagKey<Item> outputTag, int outputCount) {
        return new AlloyForgeryRecipeBuilder(outputTag, null, outputCount);
    }

    //--------------------------------------------------------------------

    public AlloyForgeryRecipeBuilder addPriorityOutput(ItemLike... outputs) {
        return this.addPriorityOutput(Arrays.stream(outputs).map(output -> BuiltInRegistries.ITEM.getKey(output.asItem())).toArray(Identifier[]::new));
    }

    public AlloyForgeryRecipeBuilder addPriorityOutput(Identifier... outputId) {
        return addPriorityOutput(List.of(outputId));
    }

    public AlloyForgeryRecipeBuilder addPriorityOutput(List<Identifier> outputIds) {
        priorities.addAll(outputIds);
        return this;
    }

    public AlloyForgeryRecipeBuilder tagInputs(HolderGetter<Item> registryLookup, Map<TagKey<Item>, Integer> inputs) {
        inputs.forEach((itemTagKey, integer) -> this.input(registryLookup, itemTagKey, integer));

        return this;
    }

    public AlloyForgeryRecipeBuilder input(HolderGetter<Item> registryLookup, TagKey<Item> input, int count) {
        this.inputTags.add(input);
        this.inputs.put(Ingredient.of(registryLookup.getOrThrow(input)), count);
        return this;
    }

    public AlloyForgeryRecipeBuilder itemInputs(Map<ItemLike, Integer> inputs) {
        inputs.forEach(this::input);

        return this;
    }

    public AlloyForgeryRecipeBuilder input(ItemLike input, int count) {
        this.inputs.put(Ingredient.of(input), count);
        return this;
    }

    public AlloyForgeryRecipeBuilder input(ItemStack inputStack) {
        var ingredient = GeneralPlatformUtils.INSTANCE.createStackIngredient(inputStack);

        this.inputs.put(ingredient, inputStack.getCount());
        return this;
    }

    public AlloyForgeryRecipeBuilder overrideRange(AlloyForgeRecipe.OverrideRange range, AlloyForgeRecipe.PendingOverride override) {
        this.ranges.put(range, override);
        return this;
    }

    public AlloyForgeryRecipeBuilder overrideRange(int start, int end, int outputCount) {
        return this.overrideRange(start, end, null, outputCount);
    }

    public AlloyForgeryRecipeBuilder overrideRange(int start, int end, @Nullable ItemLike output, int outputCount) {
        this.ranges.put(new AlloyForgeRecipe.OverrideRange(start, end),
            new AlloyForgeRecipe.PendingOverride(output != null ? output.asItem() : null, outputCount, DataComponentPatch.EMPTY));

        return this;
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, int outputCount) {
        return this.overrideRange(index, false, null, outputCount);
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, boolean includeUpperValues, int outputCount) {
        return this.overrideRange(index, includeUpperValues, null, outputCount);
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, boolean includeUpperValues, @Nullable ItemLike output, int outputCount) {
        this.ranges.put(new AlloyForgeRecipe.OverrideRange(index, includeUpperValues ? -1 : index),
            new AlloyForgeRecipe.PendingOverride(output != null ? output.asItem() : null, outputCount, DataComponentPatch.EMPTY));

        return this;
    }

    public AlloyForgeryRecipeBuilder setMinimumForgeTier(int tier) {
        this.minimumTier = tier;
        return this;
    }

    public AlloyForgeryRecipeBuilder setFuelPerTick(int fuelAmount) {
        this.fuelPerTick = fuelAmount;
        return this;
    }

    @Override
    public AlloyForgeryRecipeBuilder unlockedBy(String string, Criterion criterion) {
        this.advancementBuilder.put(string, criterion);
        return this;
    }

    public <T> AlloyForgeryRecipeBuilder criterion(String string, Map<T, Integer> inputs, Function<T, Criterion> criterionMaker) {
        inputs.keySet().forEach(itemTagKey -> this.unlockedBy(string, criterionMaker.apply(itemTagKey)));

        return this;
    }

    @Override
    public AlloyForgeryRecipeBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return null;
    }

    @Override
    public void save(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeId) {
        var advancementId = Identifier.fromNamespaceAndPath(recipeId.identifier().getNamespace(), "recipes/" + AlloyForgery.MOD_ID + "/" + recipeId.identifier().getPath());

        this.validate(recipeId.identifier());

        Advancement.Builder builder = exporter.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(recipeId))
                .requirements(Strategy.OR);

        this.advancementBuilder.forEach(builder::addCriterion);

        var recipe = new RawAlloyForgeRecipe(
                inputs,
                new OutputData(
                        this.outputCount,
                        this.outputItem != null ? this.outputItem.asItem() : null,
                        this.priorities.isEmpty() ? null : this.priorities,
                        this.outputTag
                ),
                minimumTier,
                fuelPerTick,
                ranges
        );

        exporter.accept(recipeId, recipe.generateRecipe(true), builder.build(advancementId));
    }

    public void offerTo(RecipeOutput exporter, Identifier recipeId) { //Consumer<RecipeJsonProvider> exporter
        save(exporter, ResourceKey.create(Registries.RECIPE, recipeId));
    }

    @Override
    public void save(RecipeOutput exporter) {
        this.offerTo(exporter, this.getOutputId());
    }

    @Override
    public void save(RecipeOutput exporter, String recipePath) {
        Identifier identifier2 = Identifier.parse(recipePath);

        if (identifier2.equals(this.getOutputId())) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }

        this.offerTo(exporter, identifier2);
    }

    // TODO: HANDLE SOMEHOW.....
    public void offerToWithConditions(RecipeOutput exporter, String recipePath, RecipeExporterConditionWrapper withConditionsWrapper) {
        var tags = (TagKey<Item>[]) Streams.concat(inputTags.stream(), Stream.of(this.outputTag)).toArray(TagKey[]::new);

        exporter = withConditionsWrapper.withConditions(exporter, ResourceConditionHolder.createConditions().withTags(Registries.ITEM, tags));

        save(exporter, recipePath);
    }

    public void validate(Identifier recipeId) {
        if (this.advancementBuilder.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        } else if (this.inputs.isEmpty()) {
            throw new IllegalStateException("Missing inputs meaning such cannot be made " + recipeId);
        }
    }

    private Identifier getOutputId() {
        return (outputTag != null) ? outputTag.location() : BuiltInRegistries.ITEM.getKey(outputItem.asItem());
    }

    //----------------------------------------------------
}
