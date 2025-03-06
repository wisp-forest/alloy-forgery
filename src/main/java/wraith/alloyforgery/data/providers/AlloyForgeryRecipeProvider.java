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
import java.util.concurrent.CompletableFuture;

import static wraith.alloyforgery.data.AlloyForgeryTags.Items.*;

public class AlloyForgeryRecipeProvider extends FabricRecipeProvider {

    public RecipeExporter exporter;

    public AlloyForgeryRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
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
        //////////////////////////////////
        // Compat Storage Block recipes //
        //////////////////////////////////
        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe,
            "zinc_blocks", STORAGE_BLOCKS_RAW_ZINC, STORAGE_BLOCKS_ZINC);

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createExtremeRawBlockRecipe, "tungsten_blocks", STORAGE_BLOCKS_RAW_TUNGSTEN, STORAGE_BLOCKS_TUNGSTEN,
            Identifier.of("techreborn:tungsten_block"),
            Identifier.of("indrev:tungsten_block"),
            Identifier.of("modern_industrialization:tungsten_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "titanium_blocks", STORAGE_BLOCKS_RAW_TITANIUM, STORAGE_BLOCKS_TITANIUM,
            Identifier.of("techreborn:titanium_block"),
            Identifier.of("modern_industrialization:titanium_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "tin_blocks", STORAGE_BLOCKS_RAW_TIN, STORAGE_BLOCKS_TIN,
            Identifier.of("mythicmetals:tin_block"),
            Identifier.of("techreborn:tin_block"),
            Identifier.of("indrev:tin_block"),
            Identifier.of("modern_industrialization:tin_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "silver_blocks", STORAGE_BLOCKS_RAW_SILVER, STORAGE_BLOCKS_SILVER,
            Identifier.of("mythicmetals:silver_block"),
            Identifier.of("techreborn:silver_block"),
            Identifier.of("indrev:silver_block"),
            Identifier.of("modern_industrialization:silver_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "platinum_blocks", STORAGE_BLOCKS_RAW_PLATINUM, STORAGE_BLOCKS_PLATINUM,
            Identifier.of("mythicmetals:platinum_block"),
            Identifier.of("modern_industrialization:platinum_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createExtremeRawBlockRecipe, "palladium_blocks", STORAGE_BLOCKS_RAW_PALLADIUM, STORAGE_BLOCKS_PALLADIUM,
            Identifier.of("mythicmetals:palladium_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "osmium_blocks", STORAGE_BLOCKS_RAW_OSMIUM, STORAGE_BLOCKS_OSMIUM,
            Identifier.of("mythicmetals:osmium_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "orichalcum_blocks", STORAGE_BLOCKS_RAW_ORICHALCUM, STORAGE_BLOCKS_ORICHALCUM,
            Identifier.of("mythicmetals:orichalcum_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "nickel_blocks", STORAGE_BLOCKS_RAW_NICKEL, STORAGE_BLOCKS_NICKEL,
            Identifier.of("techreborn:nickel_block"),
            Identifier.of("modern_industrialization:nickel_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "mythril_blocks", STORAGE_BLOCKS_RAW_MYTHRIL, STORAGE_BLOCKS_MYTHRIL,
            Identifier.of("mythicmetals:mythril_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "manganese_blocks", STORAGE_BLOCKS_RAW_MANGANESE, STORAGE_BLOCKS_MANGANESE,
            Identifier.of("mythicmetals:manganese_block"),
            Identifier.of("modern_industrialization:manganese_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "lead_blocks", STORAGE_BLOCKS_RAW_LEAD, STORAGE_BLOCKS_LEAD,
            Identifier.of("techreborn:lead_block"),
            Identifier.of("indrev:lead_block"),
            Identifier.of("modern_industrialization:lead_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "iridium_blocks", STORAGE_BLOCKS_RAW_IRIDIUM, STORAGE_BLOCKS_IRIDIUM,
            Identifier.of("techreborn:iridium_block"),
            Identifier.of("modern_industrialization:iridium_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "antimony_blocks", STORAGE_BLOCKS_RAW_ANTIMONY, STORAGE_BLOCKS_ANTIMONY,
            Identifier.of("modern_industrialization:antimony_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "adamantite_blocks", STORAGE_BLOCKS_RAW_ADAMANTITE, STORAGE_BLOCKS_ADAMANTITE,
            Identifier.of("mythicmetals:adamantite_block")
        );

        /////////////////////////////
        // raw ore -> ingot compat //
        /////////////////////////////
        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe,
            "zinc_ingots_from_raw_material", RAW_MATERIALS_ZINC, INGOTS_ZINC);

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createExtremeRawOreRecipe, "tungsten_ingots_from_raw_material", RAW_MATERIALS_TUNGSTEN, INGOTS_TUNGSTEN,
            Identifier.of("techreborn:tungsten_ingot"),
            Identifier.of("indrev:tungsten_ingot"),
            Identifier.of("modern_industrialization:tungsten_ingot"));


        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe, "titanium_ingots_from_raw_material", RAW_MATERIALS_TITANIUM, INGOTS_TITANIUM,
            Identifier.of("techreborn:titanium_ingot"),
            Identifier.of("modern_industrialization:titanium_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawOreRecipe, "tin_ingots_from_raw_material", RAW_MATERIALS_TIN, INGOTS_TIN,
            Identifier.of("mythicmetals:tin_ingot"),
            Identifier.of("techreborn:tin_ingot"),
            Identifier.of("indrev:tin_ingot"),
            Identifier.of("modern_industrialization:tin_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawOreRecipe, "silver_ingots_from_raw_material", RAW_MATERIALS_SILVER, INGOTS_SILVER,
            Identifier.of("mythicmetals:silver_ingot"),
            Identifier.of("techreborn:silver_ingot"),
            Identifier.of("indrev:silver_ingot"),
            Identifier.of("modern_industrialization:silver_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe, "platinum_ingots_from_raw_material", RAW_MATERIALS_PLATINUM, INGOTS_PLATINUM,
            Identifier.of("mythicmetals:platinum_ingot"),
            Identifier.of("modern_industrialization:platinum_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createExtremeRawOreRecipe, "palladium_ingots_from_raw_material", RAW_MATERIALS_PALLADIUM, INGOTS_PALLADIUM,
            Identifier.of("mythicmetals:palladium_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe, "osmium_ingots_from_raw_material", RAW_MATERIALS_OSMIUM, INGOTS_OSMIUM,
            Identifier.of("mythicmetals:osmium_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe, "orichalcum_ingots_from_raw_material", RAW_MATERIALS_ORICHALCUM, INGOTS_ORICHALCUM,
            Identifier.of("mythicmetals:orichalcum_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawOreRecipe, "nickel_ingots_from_raw_material", RAW_MATERIALS_NICKEL, INGOTS_NICKEL,
            Identifier.of("techreborn:nickel_ingot"),
            Identifier.of("modern_industrialization:nickel_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe, "mythril_ingots_from_raw_material", RAW_MATERIALS_MYTHRIL, INGOTS_MYTHRIL,
            Identifier.of("mythicmetals:mythril_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawOreRecipe, "manganese_ingots_from_raw_material", RAW_MATERIALS_MANGANESE, INGOTS_MANGANESE,
            Identifier.of("mythicmetals:manganese_ingot"),
            Identifier.of("modern_industrialization:manganese_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawOreRecipe, "lead_ingots_from_raw_material", RAW_MATERIALS_LEAD, INGOTS_LEAD,
            Identifier.of("techreborn:lead_ingot"),
            Identifier.of("indrev:lead_ingot"),
            Identifier.of("modern_industrialization:lead_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe, "iridium_ingots_from_raw_material", RAW_MATERIALS_IRIDIUM, INGOTS_IRIDIUM,
            Identifier.of("techreborn:iridium_ingot"),
            Identifier.of("modern_industrialization:iridium_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawOreRecipe, "antimony_ingots_from_raw_material", RAW_MATERIALS_ANTIMONY, INGOTS_ANTIMONY,
            Identifier.of("modern_industrialization:antimony_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawOreRecipe, "adamantite_ingots_from_raw_material", RAW_MATERIALS_ADAMANTITE, INGOTS_ADAMANTITE,
            Identifier.of("mythicmetals:adamantite_ingot")
        );

        ///////////////////////////////
        // ore block -> ingot compat //
        ///////////////////////////////
        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedOreRecipe,
            "zinc_ingots_from_ores", ORES_ZINC, INGOTS_ZINC);

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createExtremeOreRecipe, "tungsten_ingots_from_ores", ORES_TUNGSTEN, INGOTS_TUNGSTEN,
            Identifier.of("techreborn:tungsten_ingot"),
            Identifier.of("indrev:tungsten_ingot"),
            Identifier.of("modern_industrialization:tungsten_ingot"));


        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedOreRecipe, "titanium_ingots_from_ores", ORES_TITANIUM, INGOTS_TITANIUM,
            Identifier.of("techreborn:titanium_ingot"),
            Identifier.of("modern_industrialization:titanium_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardOreRecipe, "tin_ingots_from_ores", ORES_TIN, INGOTS_TIN,
            Identifier.of("mythicmetals:tin_ingot"),
            Identifier.of("techreborn:tin_ingot"),
            Identifier.of("indrev:tin_ingot"),
            Identifier.of("modern_industrialization:tin_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardOreRecipe, "silver_ingots_from_ores", ORES_SILVER, INGOTS_SILVER,
            Identifier.of("mythicmetals:silver_ingot"),
            Identifier.of("techreborn:silver_ingot"),
            Identifier.of("indrev:silver_ingot"),
            Identifier.of("modern_industrialization:silver_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedOreRecipe, "platinum_ingots_from_ores", ORES_PLATINUM, INGOTS_PLATINUM,
            Identifier.of("mythicmetals:platinum_ingot"),
            Identifier.of("modern_industrialization:platinum_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createExtremeOreRecipe, "palladium_ingots_from_ores", ORES_PALLADIUM, INGOTS_PALLADIUM,
            Identifier.of("mythicmetals:palladium_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedOreRecipe, "osmium_ingots_from_ores", ORES_OSMIUM, INGOTS_OSMIUM,
            Identifier.of("mythicmetals:osmium_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedOreRecipe, "orichalcum_ingots_from_ores", ORES_ORICHALCUM, INGOTS_ORICHALCUM,
            Identifier.of("mythicmetals:orichalcum_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardOreRecipe, "nickel_ingots_from_ores", ORES_NICKEL, INGOTS_NICKEL,
            Identifier.of("techreborn:nickel_ingot"),
            Identifier.of("modern_industrialization:nickel_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedOreRecipe, "mythril_ingots_from_ores", ORES_MYTHRIL, INGOTS_MYTHRIL,
            Identifier.of("mythicmetals:mythril_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardOreRecipe, "manganese_ingots_from_ores", ORES_MANGANESE, INGOTS_MANGANESE,
            Identifier.of("mythicmetals:manganese_ingot"),
            Identifier.of("modern_industrialization:manganese_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardOreRecipe, "lead_ingots_from_ores", ORES_LEAD, INGOTS_LEAD,
            Identifier.of("techreborn:lead_ingot"),
            Identifier.of("indrev:lead_ingot"),
            Identifier.of("modern_industrialization:lead_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedOreRecipe, "iridium_ingots_from_ores", ORES_IRIDIUM, INGOTS_IRIDIUM,
            Identifier.of("techreborn:iridium_ingot"),
            Identifier.of("modern_industrialization:iridium_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardOreRecipe, "antimony_ingots_from_ores", ORES_ANTIMONY, INGOTS_ANTIMONY,
            Identifier.of("modern_industrialization:antimony_ingot")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedOreRecipe, "adamantite_ingots_from_ores", ORES_ADAMANTITE, INGOTS_ADAMANTITE,
            Identifier.of("mythicmetals:adamantite_ingot")
        );
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
    public void exportWithTagConditions(AFRBuilderMethod builder, String name, TagKey<Item> input, TagKey<Item> output, Identifier... priorities) {
        builder.build(name, output, input)
            .addPriorityOutput(priorities)
            .offerTo(this.withConditions(this.exporter, ResourceConditions.tagsPopulated(RegistryKeys.ITEM, output, input)), "compat/forge_" + name);
    }

    public interface AFRBuilderMethod {
        AlloyForgeryRecipeBuilder build(String criterionName, TagKey<Item> output, TagKey<Item> input);
    }

    //-------------------------------------------
}
