package wraith.alloyforgery.data.providers;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.data.builders.AlloyForgeryRecipeBuilder;
import java.util.function.Consumer;

import static wraith.alloyforgery.data.AlloyForgeryTags.Items.*;

public class AlloyForgeryRecipeProvider extends FabricRecipeProvider {

    public Consumer<RecipeJsonProvider> exporter;

    public AlloyForgeryRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        this.exporter = exporter;

        createRawBlockRecipe("copper", Items.COPPER_BLOCK, RAW_COPPER_ORE_BLOCKS)
                .offerTo(exporter);

        createRawBlockRecipe("iron", Items.IRON_BLOCK, RAW_IRON_ORE_BLOCKS)
                .offerTo(exporter);

        createRawBlockRecipe("gold", Items.GOLD_BLOCK, RAW_GOLD_ORE_BLOCKS)
                .offerTo(exporter);

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe,
                "zinc", RAW_ZINC_ORE_BLOCKS, ZINC_BLOCKS);

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createExtremeRawBlockRecipe, "tungsten", RAW_TUNGSTEN_ORE_BLOCKS, TUNGSTEN_BLOCKS,
                new Identifier("techreborn:tungsten_block"),
                new Identifier("indrev:tungsten_block"),
                new Identifier("modern_industrialization:tungsten_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "titanium", RAW_TITANIUM_ORE_BLOCKS, TITANIUM_BLOCKS,
                new Identifier("techreborn:titanium_block"),
                new Identifier("modern_industrialization:titanium_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "tin", RAW_TIN_ORE_BLOCKS, TIN_BLOCKS,
                new Identifier("mythicmetals:tin_block"),
                new Identifier("techreborn:tin_block"),
                new Identifier("indrev:tin_block"),
                new Identifier("modern_industrialization:tin_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "silver", RAW_SILVER_ORE_BLOCKS, SILVER_BLOCKS,
                new Identifier("mythicmetals:silver_block"),
                new Identifier("techreborn:silver_block"),
                new Identifier("indrev:silver_block"),
                new Identifier("modern_industrialization:silver_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "platinum", RAW_PLATINUM_ORE_BLOCKS, PLATINUM_BLOCKS,
                new Identifier("mythicmetals:platinum_block"),
                new Identifier("modern_industrialization:platinum_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createExtremeRawBlockRecipe, "palladium", RAW_PALLADIUM_ORE_BLOCKS, PALLADIUM_BLOCKS,
                new Identifier("mythicmetals:palladium_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "osmium", RAW_OSMIUM_ORE_BLOCKS, OSMIUM_BLOCKS,
                new Identifier("mythicmetals:osmium_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "orichalcum", RAW_ORICHALCUM_ORE_BLOCKS, ORICHALCUM_BLOCKS,
                new Identifier("mythicmetals:orichalcum_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "nickel", RAW_NICKEL_ORE_BLOCKS, NICKEL_BLOCKS,
                new Identifier("techreborn:nickel_block"),
                new Identifier("modern_industrialization:nickel_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "mythril", RAW_MYTHRIL_ORE_BLOCKS, MYTHRIL_BLOCKS,
                new Identifier("mythicmetals:mythril_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "manganese", RAW_MANGANESE_ORE_BLOCKS, MANGANESE_BLOCKS,
                new Identifier("mythicmetals:manganese_block"),
                new Identifier("modern_industrialization:manganese_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "lead", RAW_LEAD_ORE_BLOCKS, LEAD_BLOCKS,
                new Identifier("techreborn:lead_block"),
                new Identifier("indrev:lead_block"),
                new Identifier("modern_industrialization:lead_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "iridium", RAW_IRIDIUM_ORE_BLOCKS, IRIDIUM_BLOCKS,
                new Identifier("techreborn:iridium_block"),
                new Identifier("modern_industrialization:iridium_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createStandardRawBlockRecipe, "antimony", RAW_ANTIMONY_ORE_BLOCKS, ANTIMONY_BLOCKS,
                new Identifier("modern_industrialization:antimony_block")
        );

        this.exportWithTagConditions(AlloyForgeryRecipeProvider::createAdvancedRawBlockRecipe, "adamantite", RAW_ADAMANTITE_ORE_BLOCKS, ADAMANTITE_BLOCKS,
                new Identifier("mythicmetals:adamantite_block")
        );
    }

    //-------------------------------------------

    public static AlloyForgeryRecipeBuilder createOverriddenRawBlockRecipe(String materialType, TagKey<Item> output, TagKey<Item> input, int inputAmount, int outputAmount, int overrideIndex, int overrideAmount, int fuelPerTick) {
        return AlloyForgeryRecipeBuilder.create(output, outputAmount)
                .input(input, inputAmount)
                .criterion("has_raw_" + materialType + "_ore_block", conditionsFromTag(input))
                .overrideRange(overrideIndex, true, overrideAmount)
                .setFuelPerTick(fuelPerTick);
    }

    public static AlloyForgeryRecipeBuilder createStandardRawBlockRecipe(String materialType, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRawBlockRecipe(materialType, output, input, 2, 3, 2, 4, 45);
    }

    public static AlloyForgeryRecipeBuilder createAdvancedRawBlockRecipe(String materialType, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRawBlockRecipe(materialType, output, input, 2, 2, 3, 3, 90)
                .setMinimumForgeTier(2);
    }

    public static AlloyForgeryRecipeBuilder createExtremeRawBlockRecipe(String materialType, TagKey<Item> output, TagKey<Item> input) {
        return createOverriddenRawBlockRecipe(materialType, output, input, 2, 2, 3, 3, 135)
                .setMinimumForgeTier(2);
    }

    //-------------------------------------------

    public static AlloyForgeryRecipeBuilder createRawBlockRecipe(String materialType, Item output, TagKey<Item> input) {
        return AlloyForgeryRecipeBuilder.create(output, 3)
                .input(input, 2)
                .criterion("has_raw_" + materialType + "_ore_block", conditionsFromTag(input))
                .overrideRange(2, true, 4)
                .setFuelPerTick(45);
    }

    //-------------------------------------------

    public void exportWithTagConditions(AFRBuilderMethod builder, String materialType, TagKey<Item> input, TagKey<Item> output, Identifier... priorities) {
        builder.build(materialType, output, input)
                .addPriorityOutput(priorities)
                .offerTo(this.withConditions(this.exporter, DefaultResourceConditions.tagsPopulated(output, input)));
    }

    public interface AFRBuilderMethod {
        AlloyForgeryRecipeBuilder build(String materialType, TagKey<Item> output, TagKey<Item> input);
    }

    //-------------------------------------------
}
