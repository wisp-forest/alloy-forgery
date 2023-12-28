package wraith.alloyforgery.data.builders;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements.CriterionMerger;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import wraith.alloyforgery.recipe.OutputData;
import wraith.alloyforgery.recipe.RawAlloyForgeRecipe;

import java.util.*;
import java.util.function.Supplier;

public class AlloyForgeryRecipeBuilder implements CraftingRecipeJsonBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap();
    private String group = "";

    @Nullable private final TagKey<Item> outputTag;
    @Nullable private final ItemConvertible outputItem;

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
        priorities.addAll(List.of(outputId));
        return this;
    }

    public AlloyForgeryRecipeBuilder input(TagKey<Item> input, int count) {
        this.inputs.put(Ingredient.fromTag(input), count);
        return this;
    }

    public AlloyForgeryRecipeBuilder input(ItemConvertible input, int count) {
        this.inputs.put(Ingredient.ofItems(input), count);
        return this;
    }

    public AlloyForgeryRecipeBuilder input(ItemStack inputStack) {
        this.inputs.put(Ingredient.ofItems(inputStack.getItem()), inputStack.getCount());
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
                new AlloyForgeRecipe.PendingOverride(output != null ? output.asItem() : null, outputCount));

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
                new AlloyForgeRecipe.PendingOverride(output != null ? output.asItem() : null, outputCount));

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
    public void offerTo(RecipeExporter exporter, Identifier recipeId) { //Consumer<RecipeJsonProvider> exporter
        var advancementId = new Identifier(recipeId.getNamespace(), "recipes/" + "alloy_forgery" + "/" + recipeId.getPath());

        this.validate(recipeId);

        Advancement.Builder builder = exporter.getAdvancementBuilder()
                //.parent(ROOT)
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .criteriaMerger(CriterionMerger.OR);

        this.advancementBuilder.forEach(builder::criterion);

        var recipe = new RawAlloyForgeRecipe(
                inputs,
                new OutputData(this.outputCount, this.outputItem.asItem(), this.priorities, this.outputTag),
                minimumTier,
                fuelPerTick,
                ranges
        );

        exporter.accept(recipeId, recipe.generateRecipe(true), builder.build(advancementId));
    }

    @Override
    public void offerTo(RecipeExporter exporter) {
        this.offerTo(exporter, this.getOutputId());
    }

    @Override
    public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier identifier2 = new Identifier(recipePath);

        if (identifier2.equals(this.getOutputId())) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }

        this.offerTo(exporter, identifier2);
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
