package wraith.alloyforgery.data.builders;

import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.advancement.*;
import net.minecraft.advancement.AdvancementRequirements.CriterionMerger;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.component.ComponentChanges;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wraith.alloyforgery.recipe.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class AlloyForgeryRecipeBuilder implements CraftingRecipeJsonBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap<>();
    private String group = "";

    private final Set<TagKey<Item>> inputTags = new LinkedHashSet<>();

    @Nullable
    private final TagKey<Item> outputTag;
    @Nullable
    private final ItemConvertible outputItem;

    //private final Ingredient output;
    private final int outputCount;

    private final Map<Ingredient, Integer> inputs = new LinkedHashMap<>();

    private final Map<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride> ranges = new LinkedHashMap<>();

    private final List<Identifier> priorities = new ArrayList<>();

    private int minimumTier = 1;
    private int fuelPerTick = 5;

    private AlloyForgeryRecipeBuilder(@Nullable TagKey<Item> outputTag, @Nullable ItemConvertible outputItem, int outputCount) {
        this.outputTag = outputTag;
        this.outputItem = outputItem;

        this.outputCount = outputCount;
    }

    //--------------------------------------------------------------------

    public static AlloyForgeryRecipeBuilder create(ItemConvertible output) {
        return create(output, 1);
    }

    public static AlloyForgeryRecipeBuilder create(ItemConvertible output, int outputCount) {
        return new AlloyForgeryRecipeBuilder(null, output, outputCount);
    }

    public static AlloyForgeryRecipeBuilder create(TagKey<Item> outputTag) {
        return create(outputTag, 1);
    }

    public static AlloyForgeryRecipeBuilder create(TagKey<Item> outputTag, int outputCount) {
        return new AlloyForgeryRecipeBuilder(outputTag, null, outputCount);
    }

    //--------------------------------------------------------------------

    public AlloyForgeryRecipeBuilder addPriorityOutput(ItemConvertible... outputs) {
        return this.addPriorityOutput(Arrays.stream(outputs).map(output -> Registries.ITEM.getId(output.asItem())).toArray(Identifier[]::new));
    }

    public AlloyForgeryRecipeBuilder addPriorityOutput(Identifier... outputId) {
        return addPriorityOutput(List.of(outputId));
    }

    public AlloyForgeryRecipeBuilder addPriorityOutput(List<Identifier> outputIds) {
        priorities.addAll(outputIds);
        return this;
    }

    public AlloyForgeryRecipeBuilder tagInputs(RegistryEntryLookup<Item> registryLookup, Map<TagKey<Item>, Integer> inputs) {
        inputs.forEach((itemTagKey, integer) -> this.input(registryLookup, itemTagKey, integer));

        return this;
    }

    public AlloyForgeryRecipeBuilder input(RegistryEntryLookup<Item> registryLookup, TagKey<Item> input, int count) {
        this.inputTags.add(input);
        this.inputs.put(Ingredient.fromTag(registryLookup.getOrThrow(input)), count);
        return this;
    }

    public AlloyForgeryRecipeBuilder itemInputs(Map<ItemConvertible, Integer> inputs) {
        inputs.forEach(this::input);

        return this;
    }

    public AlloyForgeryRecipeBuilder input(ItemConvertible input, int count) {
        this.inputs.put(Ingredient.ofItems(input), count);
        return this;
    }

    public AlloyForgeryRecipeBuilder input(ItemStack inputStack) {
        var ingredient = new ComponentsIngredient(Ingredient.ofItem(inputStack.getItem()), inputStack.getComponentChanges())
                .toVanilla();

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

    public AlloyForgeryRecipeBuilder overrideRange(int start, int end, @Nullable ItemConvertible output, int outputCount) {
        this.ranges.put(new AlloyForgeRecipe.OverrideRange(start, end),
            new AlloyForgeRecipe.PendingOverride(output != null ? output.asItem() : null, outputCount, ComponentChanges.EMPTY));

        return this;
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, int outputCount) {
        return this.overrideRange(index, false, null, outputCount);
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, boolean includeUpperValues, int outputCount) {
        return this.overrideRange(index, includeUpperValues, null, outputCount);
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, boolean includeUpperValues, @Nullable ItemConvertible output, int outputCount) {
        this.ranges.put(new AlloyForgeRecipe.OverrideRange(index, includeUpperValues ? -1 : index),
            new AlloyForgeRecipe.PendingOverride(output != null ? output.asItem() : null, outputCount, ComponentChanges.EMPTY));

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
    public AlloyForgeryRecipeBuilder criterion(String string, AdvancementCriterion criterion) {
        this.advancementBuilder.put(string, criterion);
        return this;
    }

    public <T> AlloyForgeryRecipeBuilder criterion(String string, Map<T, Integer> inputs, Function<T, AdvancementCriterion> criterionMaker) {
        inputs.keySet().forEach(itemTagKey -> this.criterion(string, criterionMaker.apply(itemTagKey)));

        return this;
    }

    @Override
    public AlloyForgeryRecipeBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public Item getOutputItem() {
        //TODO: Maybe not air, idk
        return Items.AIR;
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeId) {
        var advancementId = Identifier.of(recipeId.getValue().getNamespace(), "recipes/" + "alloy_forgery" + "/" + recipeId.getValue().getPath());

        this.validate(recipeId.getValue());

        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .criteriaMerger(CriterionMerger.OR);

        this.advancementBuilder.forEach(builder::criterion);

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

    public void offerTo(RecipeExporter exporter, Identifier recipeId) { //Consumer<RecipeJsonProvider> exporter
        offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, recipeId));
    }

    @Override
    public void offerTo(RecipeExporter exporter) {
        this.offerTo(exporter, this.getOutputId());
    }

    @Override
    public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier identifier2 = Identifier.of(recipePath);

        if (identifier2.equals(this.getOutputId())) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }

        this.offerTo(exporter, identifier2);
    }

    public void offerToWithConditions(RecipeExporter exporter, String recipePath, BiFunction<RecipeExporter, ResourceCondition[], RecipeExporter> withConditionsWrapper) {
        var tags = (TagKey<Item>[]) Streams.concat(inputTags.stream(), Stream.of(this.outputTag)).toArray(TagKey[]::new);

        exporter = withConditionsWrapper.apply(exporter, new ResourceCondition[]{ResourceConditions.tagsPopulated(RegistryKeys.ITEM, tags)});

        offerTo(exporter, recipePath);
    }

    public void validate(Identifier recipeId) {
        if (this.advancementBuilder.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        } else if (this.inputs.isEmpty()) {
            throw new IllegalStateException("Missing inputs meaning such cannot be made " + recipeId);
        }
    }

    private Identifier getOutputId() {
        return (outputTag != null) ? outputTag.id() : Registries.ITEM.getId(outputItem.asItem());
    }

    //----------------------------------------------------
}
