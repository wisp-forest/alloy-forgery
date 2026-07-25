package io.wispforest.alloyforgery.data.providers;

import io.wispforest.alloyforgery.data.builders.AlloyForgeryRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.wispforest.alloyforgery.data.AlloyForgeryTags.Items.common;

public class AlloyForgeryBaseRecipeProvider extends RecipeProvider {

    protected final RecipeExporterConditionWrapper withConditionsWrapper;

    public AlloyForgeryBaseRecipeProvider(RecipeOutput recipeExporter, HolderLookup.Provider registryLookup, RecipeExporterConditionWrapper withConditionsWrapper) {
        super(registryLookup, recipeExporter);

        this.withConditionsWrapper = withConditionsWrapper;
    }


    private final Map<String, MaterialRank> materialRanking = new HashMap<>();

    private final Map<MaterialRecipeType, Map<String, MaterialRecipe>> compatibilityRecipes = new HashMap<>();

    public MaterialRank getOrCreateRank(String materialName, RecipeRank purposedRank) {
        return this.materialRanking.computeIfAbsent(materialName, name -> new MaterialRank(name, purposedRank));
    }

    public void createMaterialRecipesWithRank(String materialName, RecipeRank purposedRank, String... modids) {
        this.materialRanking.computeIfAbsent(materialName, name -> new MaterialRank(name, purposedRank))
            .createMaterialRecipes(modids);
    }

    private Map<String, String> commonInputExceptions = new HashMap<>();

    public void registerCommonInputExceptions(String templatedPath, String properPath) {
        if (commonInputExceptions.containsKey(templatedPath)) {
            throw new IllegalStateException("Unable to add exception to templated paths as it already was registered: " + templatedPath);
        }

        commonInputExceptions.put(templatedPath, properPath);
    }

    public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = register("storage_blocks/raw_copper");
    public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = register("storage_blocks/raw_gold");
    public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = register("storage_blocks/raw_iron");

    private static TagKey<Item> register(String tagId) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", tagId));
    }

    public void generateAlloyForgeryRecipes() {

    }

    public final void buildRecipes() {
        this.generateAlloyForgeryRecipes();

        this.compatibilityRecipes.forEach((recipeType, recipeMap) -> {
            for (var materialRecipe : recipeMap.values()) {
                switch (materialRecipe.recipeTypes) {
                    case RAW_ORE -> {
                        switch (materialRecipe.rank) {
                            case STANDARD -> this.exportStandardRawOreRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                            case ADVANCED -> this.exportAdvancedRawOreRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                            case EXTREME  ->  this.exportExtremeRawOreRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                        }
                    }
                    case RAW_ORE_BLOCK -> {
                        switch (materialRecipe.rank) {
                            case STANDARD -> this.exportStandardRawBlockRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                            case ADVANCED -> this.exportAdvancedRawBlockRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                            case EXTREME  ->  this.exportExtremeRawBlockRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                        }
                    }
                    case ORE_BLOCK -> {
                        switch (materialRecipe.rank) {
                            case STANDARD -> this.exportStandardOreRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                            case ADVANCED -> this.exportAdvancedOreRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                            case EXTREME  ->  this.exportExtremeOreRecipe(materialRecipe.materialName, materialRecipe.modids, materialRecipe.additionalPriorities);
                        }
                    }
                }
            }
        });
    }

    //-------------------------------------------

    /**
     * Preset recipe builder for tier 1 recipes, specifically for smelting raw material blocks.
     */
    public AlloyForgeryRecipeBuilder createStandardRawBlockRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 3, 2, 4, 45);
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting raw material blocks.
     */
    public AlloyForgeryRecipeBuilder createAdvancedRawBlockRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 2, 3, 3, 90)
            .setMinimumForgeTier(2);
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting raw material blocks.
     */
    public AlloyForgeryRecipeBuilder createExtremeRawBlockRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 2, 3, 3, 135)
            .setMinimumForgeTier(2);
    }

    //--

    /**
     * Preset recipe builder for tier 1 recipes, specifically for smelting raw ores.
     */
    public AlloyForgeryRecipeBuilder createStandardRawOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 3, 2, 4, 45);
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting raw ores.
     */
    public AlloyForgeryRecipeBuilder createAdvancedRawOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 2, 3, 3, 90)
            .setMinimumForgeTier(2);
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting raw ores.
     */
    public AlloyForgeryRecipeBuilder createExtremeRawOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 2, 3, 3, 135)
            .setMinimumForgeTier(2);
    }

    //--

    /**
     * Preset recipe builder for tier 1 recipes, specifically for smelting ore blocks.
     */
    public AlloyForgeryRecipeBuilder createStandardOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 3, 2, 4, 45);
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting ore blocks.
     */
    public AlloyForgeryRecipeBuilder createAdvancedOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 2, 3, 3, 90)
            .setMinimumForgeTier(2);
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting ore blocks..
     */
    public AlloyForgeryRecipeBuilder createExtremeOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createAFRecipeWithOverride(criterionName, output, input, 2, 2, 3, 3, 135)
            .setMinimumForgeTier(2);
    }

    //-------------------------------------------

    public AlloyForgeryRecipeBuilder createRawBlockRecipe(String criterionName, Item output, TagKey<Item> input) {
        return AlloyForgeryRecipeBuilder.create(output, 3)
            .input(registries.lookupOrThrow(Registries.ITEM), input, 2)
            .unlockedBy("has_" + criterionName, has(input))
            .overrideRange(2, true, 4)
            .setFuelPerTick(45);
    }

    /**
     * Helper method which builds an Alloy Forgery recipe, specifically designed for smelting raw ores.
     * <br>
     * The recipe uses tags for both inputs and outputs, and supports one recipe override for higher tier recipes
     * <br>
     * Also generates an advancement with the criteria "has_raw_(material name)"
     *
     * @param inputCriterionName  the name for the advancement, the recipe, and the recipe file name
     * @param output         the item tag for the recipe result
     * @param input          the item tag for the recipe input
     * @param inputAmount    how many items are required
     * @param outputAmount   how many items are returned when crafting
     * @param overrideIndex  which tier to use for the recipe override
     * @param overrideAmount how many items are return when crafting with the override
     * @param fuelPerTick    how much fuel is consumed per tick for the recipe
     * @return an AlloyForgeryRecipeBuilder which should be passed to {@link AlloyForgeryRecipeProvider#exportCompatRecipe(AFRBuilderMethod, String, TagKey, TagKey, ResourceLocation...)}
     */
    public AlloyForgeryRecipeBuilder createAFRecipeWithOverride(String inputCriterionName, TagKey<Item> output, TagKey<Item> input, int inputAmount, int outputAmount, int overrideIndex, int overrideAmount, int fuelPerTick) {

        return createAFRecipeWithOverride(inputCriterionName, output, new LinkedHashMap<>(Map.of(input, inputAmount)), outputAmount, overrideIndex, overrideAmount, fuelPerTick);
    }

    public AlloyForgeryRecipeBuilder createAFRecipeWithOverride(String inputCriterionName, TagKey<Item> output, SequencedMap<TagKey<Item>, Integer> inputs, int outputAmount, int overrideIndex, int overrideAmount, int fuelPerTick) {
        return createAFRecipeWithOverrides(inputCriterionName, output, inputs, outputAmount, new LinkedHashMap<>(Map.of(overrideIndex, overrideAmount)), fuelPerTick);
    }

    public AlloyForgeryRecipeBuilder createAFRecipe(String inputCriterionName, TagKey<Item> output, Consumer<SequencedMap<TagKey<Item>, Integer>> inputs, int outputAmount, int fuelPerTick) {
        return createAFRecipeWithOverrides(inputCriterionName, output, inputs, outputAmount, map -> {}, fuelPerTick);
    }

    public AlloyForgeryRecipeBuilder createAFRecipeWithOverrides(String inputCriterionName, TagKey<Item> output, Consumer<SequencedMap<TagKey<Item>, Integer>> inputs, int outputAmount, Consumer<SequencedMap<Integer, Integer>> overrides, int fuelPerTick) {
        return createAFRecipeWithOverrides(inputCriterionName, output, Util.make(new LinkedHashMap<>(), inputs), outputAmount, Util.make(new LinkedHashMap<>(), overrides), fuelPerTick);
    }

    public AlloyForgeryRecipeBuilder createAFRecipe(String inputCriterionName, TagKey<Item> output, SequencedMap<TagKey<Item>, Integer> inputs, int outputAmount, int fuelPerTick) {
        return createAFRecipeWithOverrides(inputCriterionName, output, inputs, outputAmount, new LinkedHashMap<>(), fuelPerTick);
    }

    public AlloyForgeryRecipeBuilder createAFRecipeWithOverrides(String inputCriterionName, TagKey<Item> output, SequencedMap<TagKey<Item>, Integer> inputs, int outputAmount, SequencedMap<Integer, Integer> overrides, int fuelPerTick) {
        var builder = AlloyForgeryRecipeBuilder.create(output, outputAmount)
            .tagInputs(registries.lookupOrThrow(Registries.ITEM), inputs)
            .criterion("has_" + inputCriterionName, inputs, this::has)
            .setFuelPerTick(fuelPerTick);

        overrides.forEach((overrideIndex, overrideAmount) -> builder.overrideRange(overrideIndex, true, overrideAmount));

        return builder;
    }

    //-------------------------------------------

    public void exportCompatRecipe(AFRBuilderMethod builder, String materialName, String recipeTemplate, String inputTagTemplate, String outputTagTemplate, String priorityTemplate, List<String> modids, List<ResourceLocation> additionalPriorities) {
        var priorities = modids.stream().map(modid -> ResourceLocation.fromNamespaceAndPath(modid, materialName + priorityTemplate)).collect(Collectors.toList());

        priorities.addAll(additionalPriorities);

        exportCompatRecipe(builder,
            materialName + recipeTemplate,
            common(inputTagTemplate + materialName),
            common(outputTagTemplate + materialName),
            priorities.toArray(ResourceLocation[]::new)
        );
    }

    /**
     * Used to export recipes with an ordered list of tag priorities
     *
     * @param builder    the recipe builder you are wrapping
     * @param name       the name for the advancement, the recipe, and the recipe file name
     * @param input      the item tag for valid recipe inputs
     * @param output     the item tag for what the recipe outputs
     * @param priorities a list which specify decides which items take priority when using a tagged output. E.G. if "mythicmetals:steel_ingot" is provided, and is present in the recipe output, then the recipe always outputs this item
     * @see ResourceConditionHolder#tagsPopulated(net.minecraft.resources.ResourceKey, TagKey[])
     */
    public void exportCompatRecipe(AFRBuilderMethod builder, String name, TagKey<Item> input, TagKey<Item> output, ResourceLocation... priorities) {
        exportCompatRecipe(name, builder.build(name, output, input).addPriorityOutput(priorities));
    }

    public void exportCompatRecipe(String name, AlloyForgeryRecipeBuilder builder) {
        exportRecipe("compat/forge_" + name, builder);
    }

    public void exportAlloyCompatRecipe(String name, AlloyForgeryRecipeBuilder builder) {
        exportRecipe("compat/alloys/forge_" + name, builder);
    }

    public void exportRecipe(String name, AlloyForgeryRecipeBuilder builder) {
        builder.offerToWithConditions(this.output, name, withConditionsWrapper);
    }

    //--

    public void exportStandardRawBlockRecipe(String materialName, String ...modids) {
        exportStandardRawBlockRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    public void exportStandardRawBlockRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createStandardRawBlockRecipe,
            materialName,
            "_blocks", "storage_blocks/raw_", "storage_blocks/", "_block",
            modids,
            additionalPriorities
        );
    }

    public void exportAdvancedRawBlockRecipe(String materialName, String ...modids) {
        exportAdvancedRawBlockRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    public void exportAdvancedRawBlockRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createAdvancedRawBlockRecipe,
            materialName,
            "_blocks", "storage_blocks/raw_", "storage_blocks/", "_block",
            modids,
            additionalPriorities
        );
    }

    public void exportExtremeRawBlockRecipe(String materialName, String ...modids) {
        exportExtremeRawBlockRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    public void exportExtremeRawBlockRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createExtremeRawBlockRecipe,
            materialName,
            "_blocks", "storage_blocks/raw_", "storage_blocks/", "_block",
            modids,
            additionalPriorities
        );
    }

    //--

    public void exportStandardRawOreRecipe(String materialName, String ...modids) {
        exportStandardRawOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    public void exportStandardRawOreRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createStandardRawOreRecipe,
            materialName,
            "_ingots_from_raw_material", "raw_materials/", "ingots/", "_ingot",
            modids,
            additionalPriorities
        );
    }

    public void exportAdvancedRawOreRecipe(String materialName, String ...modids) {
        exportAdvancedRawOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    public void exportAdvancedRawOreRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createAdvancedRawOreRecipe,
            materialName,
            "_ingots_from_raw_material", "raw_materials/", "ingots/", "_ingot",
            modids,
            additionalPriorities
        );
    }

    public void exportExtremeRawOreRecipe(String materialName, String ...modids) {
        exportExtremeRawOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    public void exportExtremeRawOreRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createExtremeRawOreRecipe,
            materialName,
            "_ingots_from_raw_material", "raw_materials/", "ingots/", "_ingot",
            modids,
            additionalPriorities
        );
    }

    //--

    public void exportStandardOreRecipe(String materialName, String ...modids) {
        exportStandardOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    /**
     * Preset recipe builder for tier 1 recipes, specifically for smelting ore blocks.
     */
    public void exportStandardOreRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createStandardOreRecipe,
            materialName,
            "_ingots_from_ores", "ores/", "ingots/", "_ingot",
            modids,
            additionalPriorities
        );
    }

    public void exportAdvancedOreRecipe(String materialName, String ...modids) {
        exportAdvancedOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    public void exportAdvancedOreRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createAdvancedOreRecipe,
            materialName,
            "_ingots_from_ores", "ores/", "ingots/", "_ingot",
            modids,
            additionalPriorities
        );
    }

    public void exportExtremeOreRecipe(String materialName, String ...modids) {
        exportExtremeOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    public void exportExtremeOreRecipe(String materialName, List<String> modids, List<ResourceLocation> additionalPriorities) {
        this.exportCompatRecipe(
            this::createExtremeOreRecipe,
            materialName,
            "_ingots_from_ores", "ores/", "ingots/", "_ingot",
            modids,
            additionalPriorities
        );
    }

    //--

    public void createAlloyingRecipes(String materialName, int outputAmount, Consumer<SequencedMap<String, Integer>> materialInputs, Consumer<SequencedMap<Integer, Integer>> overridesMap, int forgeTier, int fuelPerTick, String... modids) {
        createAlloyingRecipes(materialName, outputAmount, materialInputs, overridesMap, forgeTier, fuelPerTick, Arrays.stream(modids).toList(), List.of());
    }

    public void createAlloyingRecipes(String materialName, int outputAmount, Consumer<SequencedMap<String, Integer>> materialInputs, Consumer<SequencedMap<Integer, Integer>> overridesMap, int forgeTier, int fuelPerTick, List<String> modids, List<ResourceLocation> additionalPriorities) {
        Function<String, SequencedMap<TagKey<Item>, Integer>> inputBuilder = (materialType) -> {
            return Util.make(new LinkedHashMap<>(), materialInputs).entrySet().stream()
                .map(entry -> {
                    var path = materialType + entry.getKey();

                    path = commonInputExceptions.getOrDefault(path, path);

                    return Map.entry(common(path), entry.getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (i1, i2) -> i1, LinkedHashMap::new));
        };

        var list = Util.make(new LinkedHashMap<>(), overridesMap).entrySet().stream().toList();

        Function<Integer, SequencedMap<Integer, Integer>> overrideBuilder = (maxIndex) -> {
            return Util.make(new LinkedHashMap<>(), map -> {
                for (var i = 0; i < maxIndex; i++) {
                    if (i >= list.size()) break;

                    var entry = list.get(i);
                    map.put(entry.getKey(), entry.getValue());
                }
            });
        };

        var priorities = new ArrayList<>(additionalPriorities);

        priorities.addAll(modids.stream().map(modid -> ResourceLocation.fromNamespaceAndPath(modid, materialName + "_ingot")).toList());

        this.exportAlloyCompatRecipe(
            materialName + "_from_ingots",
            createAFRecipe(materialName, common("ingots/" + materialName), inputBuilder.apply("ingots/"), outputAmount, fuelPerTick)
                .addPriorityOutput(priorities)
                .setMinimumForgeTier(forgeTier)
        );

        this.exportAlloyCompatRecipe(
            materialName + "_from_raw_ores",
            createAFRecipeWithOverrides(materialName, common("ingots/" + materialName), inputBuilder.apply("raw_materials/"), outputAmount, overrideBuilder.apply(1), fuelPerTick)
                .addPriorityOutput(priorities)
                .setMinimumForgeTier(forgeTier)
        );

        this.exportAlloyCompatRecipe(
            materialName + "_from_raw_ore_blocks",
            createAFRecipeWithOverrides(materialName, common("storage_blocks/" + materialName), inputBuilder.apply("storage_blocks/raw_"), outputAmount, overrideBuilder.apply(1), fuelPerTick * 9)
                .addPriorityOutput(priorities)
                .setMinimumForgeTier(forgeTier)
        );

        this.exportAlloyCompatRecipe(
            materialName + "_from_ores",
            createAFRecipeWithOverrides(materialName, common("ingots/" + materialName), inputBuilder.apply("ores/"), outputAmount, overrideBuilder.apply(2), fuelPerTick)
                .addPriorityOutput(priorities)
                .setMinimumForgeTier(forgeTier)
        );
    }

    //--

    public interface AFRBuilderMethod {
        AlloyForgeryRecipeBuilder build(String criterionName, TagKey<Item> output, TagKey<Item> input);
    }

    public final class MaterialRank {
        private final String materialName;
        private final RecipeRank rank;

        public MaterialRank(String materialName, RecipeRank rank) {
            this.materialName = materialName;
            this.rank = rank;
        }

        public void createMaterialRecipes(String... modids) {
            for (var value : MaterialRecipeType.values()) createMaterialRecipe(value, modids);
        }

        public void createMaterialRecipes(ResourceLocation... additionalPriorities) {
            for (var value : MaterialRecipeType.values()) createMaterialRecipe(value, additionalPriorities);
        }

        public void createMaterialRecipe(MaterialRecipeType recipeType, String... modids) {
            var holder = compatibilityRecipes.computeIfAbsent(recipeType, type -> new HashMap<>())
                .computeIfAbsent(materialName, name -> new MaterialRecipe(name, this.rank, recipeType));

            for (String modid : modids) holder.addModid(modid);
        }

        public void createMaterialRecipe(MaterialRecipeType recipeType, ResourceLocation... additionalPriorities) {
            var holder = compatibilityRecipes.computeIfAbsent(recipeType, type -> new HashMap<>())
                .computeIfAbsent(materialName, name -> new MaterialRecipe(name, this.rank, recipeType));

            for (ResourceLocation id : additionalPriorities) holder.addPriority(id);
        }
    }

    public static final class MaterialRecipe {
        private final String materialName;
        private final RecipeRank rank;
        private final MaterialRecipeType recipeTypes;

        private final List<String> modids = new ArrayList<>();
        private final List<ResourceLocation> additionalPriorities = new ArrayList<>();

        public MaterialRecipe(String materialName, RecipeRank rank, MaterialRecipeType recipeTypes) {
            this.materialName = materialName;
            this.rank = rank;
            this.recipeTypes = recipeTypes;
        }

        public void addModid(String modid) {
            this.modids.add(modid);
        }

        public void addPriority(ResourceLocation id) {
            this.additionalPriorities.add(id);
        }
    }

    public enum RecipeRank { STANDARD, ADVANCED, EXTREME }

    public enum MaterialRecipeType { RAW_ORE, RAW_ORE_BLOCK, ORE_BLOCK }

}
