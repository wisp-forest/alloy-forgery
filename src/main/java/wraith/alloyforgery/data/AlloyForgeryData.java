package wraith.alloyforgery.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.registry.RegistryWrapper;
import wraith.alloyforgery.data.providers.AlloyForgeryRecipeProvider;

public class AlloyForgeryData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var fortnitusPackus = fabricDataGenerator.createPack();
        fortnitusPackus.addProvider((output, registriesFuture) -> new FabricRecipeProvider(output, registriesFuture) {
            @Override
            protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
                return new AlloyForgeryRecipeProvider(exporter, registryLookup, this::withConditions);
            }

            @Override
            public String getName() {
                return "Alloy Forgery Compat Recipes";
            }
        });
    }
}
