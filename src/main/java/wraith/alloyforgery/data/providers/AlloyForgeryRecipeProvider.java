package wraith.alloyforgery.data.providers;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.data.builders.AlloyForgeryRecipeBuilder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static wraith.alloyforgery.data.AlloyForgeryTags.Items.*;

public class AlloyForgeryRecipeProvider extends FabricRecipeProvider {

    public RecipeExporter exporter;

    public AlloyForgeryRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);

        setupCompatibilityRecipes();
    }

    public void setupCompatibilityRecipes() {
        this.createMaterialRecipesWithRank("zinc", RecipeRank.ADVANCED);

        this.createMaterialRecipesWithRank("tungsten", RecipeRank.EXTREME, "techreborn", "indrev", "modern_industrialization");

        this.createMaterialRecipesWithRank("titanium", RecipeRank.ADVANCED, "techreborn", "modern_industrialization");

        this.createMaterialRecipesWithRank("tin", RecipeRank.STANDARD, "mythicmetals", "techreborn", "indrev", "modern_industrialization");

        this.createMaterialRecipesWithRank("silver", RecipeRank.STANDARD, "mythicmetals", "techreborn", "indrev", "modern_industrialization");

        this.createMaterialRecipesWithRank("platinum", RecipeRank.ADVANCED,"mythicmetals", "modern_industrialization");

        this.createMaterialRecipesWithRank("palladium", RecipeRank.EXTREME,"mythicmetals");

        this.createMaterialRecipesWithRank("osmium", RecipeRank.ADVANCED,"mythicmetals");

        this.createMaterialRecipesWithRank("orichalcum", RecipeRank.ADVANCED,"mythicmetals");

        this.createMaterialRecipesWithRank("nickel", RecipeRank.STANDARD, "techreborn", "modern_industrialization");

        this.createMaterialRecipesWithRank("mythril", RecipeRank.ADVANCED,"mythicmetals");

        this.createMaterialRecipesWithRank("manganese", RecipeRank.STANDARD, "mythicmetals", "modern_industrialization");

        this.createMaterialRecipesWithRank("lead", RecipeRank.STANDARD, "techreborn", "indrev", "modern_industrialization");

        this.createMaterialRecipesWithRank("iridium", RecipeRank.ADVANCED,"techreborn", "modern_industrialization");

        this.createMaterialRecipesWithRank("antimony", RecipeRank.STANDARD, "modern_industrialization");

        this.createMaterialRecipesWithRank("adamantite", RecipeRank.ADVANCED,"mythicmetals");
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

    @Override
    public void generate(RecipeExporter exporter) {
        this.exporter = exporter;

        // Vanilla recipes
        createRawBlockRecipe("copper", Items.COPPER_BLOCK, ConventionalItemTags.STORAGE_BLOCKS_RAW_COPPER)
            .offerTo(exporter);

        createRawBlockRecipe("iron", Items.IRON_BLOCK, ConventionalItemTags.STORAGE_BLOCKS_RAW_IRON)
            .offerTo(exporter);

        createRawBlockRecipe("gold", Items.GOLD_BLOCK, ConventionalItemTags.STORAGE_BLOCKS_RAW_GOLD)
            .offerTo(exporter);

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
    public static AlloyForgeryRecipeBuilder createStandardRawBlockRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 3, 2, 4, 45);
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting raw material blocks.
     */
    public static AlloyForgeryRecipeBuilder createAdvancedRawBlockRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 2, 3, 3, 90)
            .setMinimumForgeTier(2);
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting raw material blocks.
     */
    public static AlloyForgeryRecipeBuilder createExtremeRawBlockRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 2, 3, 3, 135)
            .setMinimumForgeTier(2);
    }

    //--

    /**
     * Preset recipe builder for tier 1 recipes, specifically for smelting raw ores.
     */
    public static AlloyForgeryRecipeBuilder createStandardRawOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 3, 2, 4, 45);
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting raw ores.
     */
    public static AlloyForgeryRecipeBuilder createAdvancedRawOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 2, 3, 3, 90)
            .setMinimumForgeTier(2);
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting raw ores.
     */
    public static AlloyForgeryRecipeBuilder createExtremeRawOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 2, 3, 3, 135)
            .setMinimumForgeTier(2);
    }

    //--

    /**
     * Preset recipe builder for tier 1 recipes, specifically for smelting ore blocks.
     */
    public static AlloyForgeryRecipeBuilder createStandardOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 3, 2, 4, 45);
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting ore blocks.
     */
    public static AlloyForgeryRecipeBuilder createAdvancedOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 2, 3, 3, 90)
            .setMinimumForgeTier(2);
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting ore blocks..
     */
    public static AlloyForgeryRecipeBuilder createExtremeOreRecipe(String criterionName, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRecipe(criterionName, output, input, 2, 2, 3, 3, 135)
            .setMinimumForgeTier(2);
    }

    //-------------------------------------------

    /**
     * Helper method which builds an Alloy Forgery recipe, specifically designed for smelting raw ores.
     * <br>
     * The recipe uses tags for both inputs and outputs, and supports one recipe override for higher tier recipes
     * <br>
     * Also generates an advancement with the criteria "has_raw_(material name)"
     *
     * @param name           the name for the advancement, the recipe, and the recipe file name
     * @param output         the item tag for the recipe result
     * @param input          the item tag for the recipe input
     * @param inputAmount    how many items are required
     * @param outputAmount   how many items are returned when crafting
     * @param overrideIndex  which tier to use for the recipe override
     * @param overrideAmount how many items are return when crafting with the override
     * @param fuelPerTick    how much fuel is consumed per tick for the recipe
     * @return an AlloyForgeryRecipeBuilder which should be passed to {@link AlloyForgeryRecipeProvider#exportWithTagConditions(AFRBuilderMethod, String, TagKey, TagKey, Identifier...)}
     */
    public static AlloyForgeryRecipeBuilder createOverriddenRecipe(String name, TagKey<Item> output, TagKey<Item> input, int inputAmount, int outputAmount, int overrideIndex, int overrideAmount, int fuelPerTick) {
        return AlloyForgeryRecipeBuilder.create(output, outputAmount)
            .input(input, inputAmount)
            .criterion("has_" + name, conditionsFromTag(input))
            .overrideRange(overrideIndex, true, overrideAmount)
            .setFuelPerTick(fuelPerTick);
    }

    public static AlloyForgeryRecipeBuilder createRawBlockRecipe(String criterionName, Item output, TagKey<Item> input) {
        return AlloyForgeryRecipeBuilder.create(output, 3)
            .input(input, 2)
            .criterion("has_" + criterionName, conditionsFromTag(input))
            .overrideRange(2, true, 4)
            .setFuelPerTick(45);
    }

    //-------------------------------------------

    @Deprecated(forRemoval = true)
    public void exportWithTagConditions(AFRBuilderMethod builder, String name, TagKey<Item> input, TagKey<Item> output, Identifier... priorities) {
        exportRecipe(builder, name, input, output, priorities);
    }

    /**
     * Used to export recipes with an ordered list of tag priorities
     *
     * @param builder    the recipe builder you are wrapping
     * @param name       the name for the advancement, the recipe, and the recipe file name
     * @param input      the item tag for valid recipe inputs
     * @param output     the item tag for what the recipe outputs
     * @param priorities a list which specify decides which items take priority when using a tagged output. E.G. if "mythicmetals:steel_ingot" is provided, and is present in the recipe output, then the recipe always outputs this item
     * @see ResourceConditions#tagsPopulated(net.minecraft.registry.RegistryKey, TagKey[])
     */
    public void exportRecipe(AFRBuilderMethod builder, String name, TagKey<Item> input, TagKey<Item> output, Identifier... priorities) {
        builder.build(name, output, input)
            .addPriorityOutput(priorities)
            .offerTo(this.withConditions(this.exporter, ResourceConditions.tagsPopulated(RegistryKeys.ITEM, output, input)), "compat/forge_" + name);
    }

    public void exportRecipe(AFRBuilderMethod builder, String materialName, String recipeTemplate, String inputTagTemplate, String outputTagTemplate, String priorityTemplate, List<String> modids, List<Identifier> additionalPriorities) {
        var priorities = modids.stream().map(modid -> Identifier.of(modid, materialName + priorityTemplate)).collect(Collectors.toList());

        priorities.addAll(additionalPriorities);

        exportRecipe(builder, materialName + recipeTemplate, common(inputTagTemplate + materialName), common(outputTagTemplate + materialName), priorities.toArray(Identifier[]::new));
    }

    //--

    public void exportStandardRawBlockRecipe(String materialName, String ...modids) {
        exportStandardRawBlockRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    /**
     * Preset recipe builder for tier 1 recipes, specifically for smelting raw material blocks.
     */
    public void exportStandardRawBlockRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createStandardRawBlockRecipe,
                materialName,
                "_blocks", "storage_blocks/raw_", "storage_blocks/", "_block",
                modids,
                additionalPriorities
        );
    }

    public void exportAdvancedRawBlockRecipe(String materialName, String ...modids) {
        exportAdvancedRawBlockRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting raw material blocks.
     */
    public void exportAdvancedRawBlockRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe,
                materialName,
                "_blocks", "storage_blocks/raw_", "storage_blocks/", "_block",
                modids,
                additionalPriorities
        );
    }

    public void exportExtremeRawBlockRecipe(String materialName, String ...modids) {
        exportExtremeRawBlockRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting raw material blocks.
     */
    public void exportExtremeRawBlockRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createExtremeRawBlockRecipe,
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

    /**
     * Preset recipe builder for tier 1 recipes, specifically for smelting raw ores.
     */
    public void exportStandardRawOreRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createStandardRawOreRecipe,
                materialName,
                "_ingots_from_raw_material", "raw_materials/", "ingots/", "_ingot",
                modids,
                additionalPriorities
        );
    }

    public void exportAdvancedRawOreRecipe(String materialName, String ...modids) {
        exportAdvancedRawOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting raw ores.
     */
    public void exportAdvancedRawOreRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe,
                materialName,
                "_ingots_from_raw_material", "raw_materials/", "ingots/", "_ingot",
                modids,
                additionalPriorities
        );
    }

    public void exportExtremeRawOreRecipe(String materialName, String ...modids) {
        exportExtremeRawOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting raw ores.
     */
    public void exportExtremeRawOreRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createExtremeRawOreRecipe,
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
    public void exportStandardOreRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createStandardOreRecipe,
                materialName,
                "_ingots_from_ores", "ores/", "ingots/", "_ingot",
                modids,
                additionalPriorities
        );
    }

    public void exportAdvancedOreRecipe(String materialName, String ...modids) {
        exportAdvancedOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    /**
     * Preset recipe builder for tier 2 recipes, specifically for smelting ore blocks.
     */
    public void exportAdvancedOreRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createAdvancedOreRecipe,
                materialName,
                "_ingots_from_ores", "ores/", "ingots/", "_ingot",
                modids,
                additionalPriorities
        );
    }

    public void exportExtremeOreRecipe(String materialName, String ...modids) {
        exportExtremeOreRecipe(materialName, Arrays.stream(modids).toList(), List.of());
    }

    /**
     * Preset recipe builder for tier 3 recipes, specifically for smelting ore blocks.
     */
    public void exportExtremeOreRecipe(String materialName, List<String> modids, List<Identifier> additionalPriorities) {
        this.exportRecipe(
                AlloyForgeryRecipeProvider::createExtremeOreRecipe,
                materialName,
                "_ingots_from_ores", "ores/", "ingots/", "_ingot",
                modids,
                additionalPriorities
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

        public void createMaterialRecipes(Identifier... additionalPriorities) {
            for (var value : MaterialRecipeType.values()) createMaterialRecipe(value, additionalPriorities);
        }

        public void createMaterialRecipe(MaterialRecipeType recipeType, String... modids) {
            var holder = compatibilityRecipes.computeIfAbsent(recipeType, type -> new HashMap<>())
                    .computeIfAbsent(materialName, name -> new MaterialRecipe(name, this.rank, recipeType));

            for (String modid : modids) holder.addModid(modid);
        }

        public void createMaterialRecipe(MaterialRecipeType recipeType, Identifier... additionalPriorities) {
            var holder = compatibilityRecipes.computeIfAbsent(recipeType, type -> new HashMap<>())
                    .computeIfAbsent(materialName, name -> new MaterialRecipe(name, this.rank, recipeType));

            for (Identifier id : additionalPriorities) holder.addPriority(id);
        }
    }

    private static final class MaterialRecipe {
        private final String materialName;
        private final RecipeRank rank;
        private final MaterialRecipeType recipeTypes;

        private final List<String> modids = new ArrayList<>();
        private final List<Identifier> additionalPriorities = new ArrayList<>();

        public MaterialRecipe(String materialName, RecipeRank rank, MaterialRecipeType recipeTypes) {
            this.materialName = materialName;
            this.rank = rank;
            this.recipeTypes = recipeTypes;
        }

        public void addModid(String modid) {
            this.modids.add(modid);
        }

        public void addPriority(Identifier id) {
            this.additionalPriorities.add(id);
        }
    }

    public enum RecipeRank { STANDARD, ADVANCED, EXTREME }

    public enum MaterialRecipeType { RAW_ORE, RAW_ORE_BLOCK, ORE_BLOCK }

    //-------------------------------------------
}
