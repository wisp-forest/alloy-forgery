package wraith.alloyforgery.data.providers;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.data.AlloyForgeryTags;

public class AlloyForgeryTagProviders {

    public static class Item extends FabricTagProvider.ItemTagProvider {

        public Item(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateTags() {
            getOrCreateTagBuilder(AlloyForgeryTags.Items.RAW_IRON_ORE_BLOCKS)
                    .add(Items.RAW_IRON_BLOCK);

            getOrCreateTagBuilder(AlloyForgeryTags.Items.RAW_GOLD_ORE_BLOCKS)
                    .add(Items.RAW_GOLD_BLOCK);

            getOrCreateTagBuilder(AlloyForgeryTags.Items.RAW_COPPER_ORE_BLOCKS)
                    .add(Items.RAW_COPPER_BLOCK);
        }
    }
}
