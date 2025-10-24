package io.wispforest.alloyforgery.data.providers;

import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

public class AlloyForgeryRecipeProvider extends AlloyForgeryBaseRecipeProvider {

    public AlloyForgeryRecipeProvider(RecipeExporter recipeExporter, RegistryWrapper.WrapperLookup registryLookup, RecipeExporterConditionWrapper withConditionsWrapper) {
        super(recipeExporter, registryLookup, withConditionsWrapper);
    }

    @Override
    public void generateAlloyForgeryRecipes() {
        setupCompatibilityRecipes();

        // Vanilla recipes
        createRawBlockRecipe("copper", Items.COPPER_BLOCK, STORAGE_BLOCKS_RAW_COPPER)
            .offerTo(exporter);

        createRawBlockRecipe("iron", Items.IRON_BLOCK, STORAGE_BLOCKS_RAW_IRON)
            .offerTo(exporter);

        createRawBlockRecipe("gold", Items.GOLD_BLOCK, STORAGE_BLOCKS_RAW_GOLD)
            .offerTo(exporter);

        this.createAlloyingRecipes("bronze",
                4,
                inputs -> { inputs.put("copper", 3); inputs.put("tin", 1); },
                overrides -> { overrides.put(2, 5); overrides.put(3, 6); },
                1,
                5,
                "mythicmetals", "techreborn", "indrev", "modern_industrialization"
        );

        this.createAlloyingRecipes("brass",
                3,
                inputs -> { inputs.put("copper", 2); inputs.put("zinc", 1); },
                overrides -> { overrides.put(2, 4); overrides.put(3, 5); },
                1,
                5,
                "create", "techreborn"
        );

        this.createAlloyingRecipes("electrum",
                2,
                inputs -> { inputs.put("gold", 1); inputs.put("silver", 1); },
                overrides -> overrides.put(3, 3),
                2,
                10,
                "techreborn", "indrev", "modern_industrialization"
        );

        this.createAlloyingRecipes("invar",
                3,
                inputs -> { inputs.put("iron", 2); inputs.put("nickel", 1); },
                overrides -> { overrides.put(2, 4); overrides.put(3, 5); },
                1,
                5,
                "techreborn", "modern_industrialization"
        );

        this.createAlloyingRecipes("steel",
                2,
                inputs -> { inputs.put("iron", 1); inputs.put("coal", 1); },
                overrides -> { overrides.put(2, 3); overrides.put(3, 4); },
                1,
                5,
                "techreborn", "modern_industrialization"
        );
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

        registerCommonInputExceptions("ingots/coal", "coal");
        registerCommonInputExceptions("raw_materials/coal", "coal");
        registerCommonInputExceptions("storage_blocks/raw_coal", "storage_blocks/coal");
    }
}
