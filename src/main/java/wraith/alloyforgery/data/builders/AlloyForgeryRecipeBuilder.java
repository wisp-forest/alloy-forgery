package wraith.alloyforgery.data.builders;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.TagEntry;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wraith.alloyforgery.mixin.IngredientAccessor;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import wraith.alloyforgery.recipe.AlloyForgeRecipeSerializer;

import java.util.*;
import java.util.function.Consumer;

public class AlloyForgeryRecipeBuilder implements CraftingRecipeJsonBuilder, RecipeJsonProvider {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Advancement.Builder advancementBuilder = Advancement.Builder.create();
    private String group = "";

    private final Ingredient output;
    private final int outputCount;

    private final Map<Ingredient, Integer> inputs = new LinkedHashMap<>();

    private final Map<AlloyForgeRecipe.OverrideRange, AlloyForgeRecipe.PendingOverride> ranges = new LinkedHashMap<>();

    private final Set<Identifier> priorities = new LinkedHashSet<>();

    private int minimumTier = 1;
    private int fuelPerTick = 5;

    public AlloyForgeryRecipeBuilder(Ingredient output, int outputCount) {
        this.output = output;
        this.outputCount = outputCount;
    }

    //--------------------------------------------------------------------

    public static AlloyForgeryRecipeBuilder create(ItemConvertible output) {
        return create(output, 1);
    }

    public static AlloyForgeryRecipeBuilder create(ItemConvertible output, int outputCount) {
        return new AlloyForgeryRecipeBuilder(Ingredient.ofItems(output), outputCount);
    }

    public static AlloyForgeryRecipeBuilder create(TagKey<Item> outputTag) {
        return create(outputTag, 1);
    }

    public static AlloyForgeryRecipeBuilder create(TagKey<Item> outputTag, int outputCount) {
        return new AlloyForgeryRecipeBuilder(Ingredient.fromTag(outputTag), outputCount);
    }

    //--------------------------------------------------------------------

    public AlloyForgeryRecipeBuilder addPriorityOutput(ItemConvertible ...outputs){
        return this.addPriorityOutput(Arrays.stream(outputs).map(output -> Registry.ITEM.getId(output.asItem())).toArray(Identifier[]::new));
    }

    public AlloyForgeryRecipeBuilder addPriorityOutput(Identifier ...outputId){
        priorities.addAll(List.of(outputId));
        return this;
    }

    public AlloyForgeryRecipeBuilder input(TagKey<Item> input, int count){
        this.inputs.put(Ingredient.fromTag(input), count);
        return this;
    }

    public AlloyForgeryRecipeBuilder input(ItemConvertible input, int count){
        this.inputs.put(Ingredient.ofItems(input), count);
        return this;
    }

    public AlloyForgeryRecipeBuilder input(ItemStack inputStack){
        this.inputs.put(Ingredient.ofItems(inputStack.getItem()), inputStack.getCount());
        return this;
    }

    public AlloyForgeryRecipeBuilder overrideRange(AlloyForgeRecipe.OverrideRange range, AlloyForgeRecipe.PendingOverride override){
        this.ranges.put(range, override);
        return this;
    }

    public AlloyForgeryRecipeBuilder overrideRange(int start, int end, int outputCount){
        return this.overrideRange(start, end, null, outputCount);
    }

    public AlloyForgeryRecipeBuilder overrideRange(int start, int end, @Nullable ItemConvertible output, int outputCount){
        this.ranges.put(new AlloyForgeRecipe.OverrideRange(start, end),
                new AlloyForgeRecipe.PendingOverride(output != null ? output.asItem().getDefaultStack() : null, outputCount));

        return this;
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, int outputCount) {
        return this.overrideRange(index, false, null, outputCount);
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, boolean includeUpperValues, int outputCount){
        return this.overrideRange(index, includeUpperValues, null, outputCount);
    }

    public AlloyForgeryRecipeBuilder overrideRange(int index, boolean includeUpperValues, @Nullable ItemConvertible output, int outputCount){
        this.ranges.put(new AlloyForgeRecipe.OverrideRange(index, includeUpperValues ? -1 : index),
                new AlloyForgeRecipe.PendingOverride(output != null ? output.asItem().getDefaultStack() : null, outputCount));

        return this;
    }

    public AlloyForgeryRecipeBuilder setMinimumForgeTier(int tier){
        this.minimumTier = tier;
        return this;
    }

    public AlloyForgeryRecipeBuilder setFuelPerTick(int fuelAmount){
        this.fuelPerTick = fuelAmount;
        return this;
    }

    @Override
    public AlloyForgeryRecipeBuilder criterion(String string, CriterionConditions criterionConditions) {
        this.advancementBuilder.criterion(string, criterionConditions);
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
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        this.validate();
        this.advancementBuilder
                .parent(ROOT)
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
                .rewards(net.minecraft.advancement.AdvancementRewards.Builder.recipe(recipeId))
                .criteriaMerger(CriterionMerger.OR);

        this.recipeId = recipeId;
        this.advancementId = new Identifier(recipeId.getNamespace(), "recipes/" + "alloy_forgery" + "/" + recipeId.getPath());

        exporter.accept(this);
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter) {
        this.offerTo(exporter, makeRecipeId());
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, String recipePath) {
        Identifier identifier2 = new Identifier(recipePath);

        if (identifier2.equals(makeRecipeId())) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }

        this.offerTo(exporter, identifier2);
    }

    public void validate(){
        if (this.advancementBuilder.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        } else if(this.inputs.isEmpty()){
            throw new IllegalStateException("Missing inputs meaning such cannot be made " + recipeId);
        }
    }

    private Identifier makeRecipeId(){
        Ingredient.Entry firstEntry = ((IngredientAccessor) (Object) this.output).af$getEntries()[0];

        Identifier outputId;

        if(firstEntry instanceof Ingredient.TagEntry tagEntry){
            outputId = Identifier.tryParse(JsonHelper.getString(tagEntry.toJson(), "tag"));

        } else if(firstEntry instanceof Ingredient.StackEntry stackEntry) {
            outputId = Identifier.tryParse(JsonHelper.getString(stackEntry.toJson(), "item"));
        } else {
            throw new IllegalStateException("Seems the output is composed of Ingredient Entry not implemented by Alloy Forgery, try manually naming your recipe instead");
        }

        return outputId;
    }

    //----------------------------------------------------

    @Nullable public Identifier recipeId;
    @Nullable public Identifier advancementId;

    @Override
    public void serialize(JsonObject recipeJson) {
        JsonArray array = new JsonArray();

        this.inputs.forEach((ingredient, integer) -> {
            Ingredient.Entry firstEntry = ((IngredientAccessor) (Object) ingredient).af$getEntries()[0];

            Identifier inputId;
            JsonObject inputJson = new JsonObject();

            if (firstEntry instanceof Ingredient.TagEntry tagEntry) {
                inputId = Identifier.tryParse(JsonHelper.getString(tagEntry.toJson(), "tag"));

                inputJson.addProperty("tag", inputId.toString());
            } else if (firstEntry instanceof Ingredient.StackEntry stackEntry) {
                inputId = Identifier.tryParse(JsonHelper.getString(stackEntry.toJson(), "item"));

                inputJson.addProperty("item", inputId.toString());
            } else {
                throw new IllegalStateException("Seems the output is composed of Ingredient Entry not implemented by Alloy Forgery, try manually naming your recipe instead");
            }

            if (integer > 1) inputJson.addProperty("count", integer);

            array.add(inputJson);
        });

        recipeJson.add("inputs", array);

        //--------------------------------------

        JsonObject outputJson = new JsonObject();

        JsonArray priorityArray = new JsonArray();

        if (!this.priorities.isEmpty()) {
            this.priorities.forEach(identifier -> priorityArray.add(identifier.toString()));

            outputJson.add("priority", priorityArray);
        }

        Ingredient.Entry firstEntry = ((IngredientAccessor) (Object) this.output).af$getEntries()[0];

        Identifier outputId;

        if(firstEntry instanceof Ingredient.TagEntry tagEntry){
            outputId = Identifier.tryParse(JsonHelper.getString(tagEntry.toJson(), "tag"));

            outputJson.addProperty("default", outputId.toString());

            outputJson.add("priority", priorityArray);
        } else if(firstEntry instanceof Ingredient.StackEntry stackEntry) {
            outputId = Identifier.tryParse(JsonHelper.getString(stackEntry.toJson(), "item"));

            outputJson.addProperty("id", outputId.toString());

            if(!priorityArray.isEmpty()){
                LOGGER.warn("[AlloyForgeryRecipeBuilder] Priority-based recipes only work with Tag based Ingredient outputs, such will be ignored. [RecipeId: {}]", this.getRecipeId());
            }
        } else {
            throw new IllegalStateException("Seems the output is composed of Ingredient Entry not implemented by Alloy Forgery, try manually naming your recipe instead");
        }

        outputJson.addProperty("count", this.outputCount);

        recipeJson.add("output", outputJson);

        //--------------------------------------

        if(!this.ranges.isEmpty()){
            JsonObject overrideJson = new JsonObject();

            this.ranges.forEach((overrideRange, override) -> {
                JsonObject overrideObject = new JsonObject();

                if(!override.isCountOnly()) {
                    overrideObject.addProperty("id", Registry.ITEM.getId(override.stack().getItem()).toString());
                }

                overrideObject.addProperty("count", override.count());

                overrideJson.add(overrideRange.toString(), overrideObject);
            });

            recipeJson.add("overrides", overrideJson);
        }

        recipeJson.addProperty("min_forge_tier", this.minimumTier);
        recipeJson.addProperty("fuel_per_tick", this.fuelPerTick);
    }

    @Override
    public Identifier getRecipeId() { return this.recipeId; }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AlloyForgeRecipeSerializer.INSTANCE;
    }

    @Nullable
    @Override
    public JsonObject toAdvancementJson() {
        return this.advancementBuilder.toJson();
    }

    @Nullable
    @Override
    public Identifier getAdvancementId() {
        return this.advancementId;
    }
}
