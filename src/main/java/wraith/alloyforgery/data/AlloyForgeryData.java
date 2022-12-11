package wraith.alloyforgery.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import wraith.alloyforgery.data.providers.AlloyForgeryRecipeProvider;
import wraith.alloyforgery.data.providers.AlloyForgeryTagProviders;

public class AlloyForgeryData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var fortnitusPackus = fabricDataGenerator.createPack();
        fortnitusPackus.addProvider(AlloyForgeryRecipeProvider::new);
        fortnitusPackus.addProvider(AlloyForgeryTagProviders.Item::new);
    }
}
