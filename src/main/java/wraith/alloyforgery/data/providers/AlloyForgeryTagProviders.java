package wraith.alloyforgery.data.providers;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.data.AlloyForgeryTags;

import java.util.concurrent.CompletableFuture;

public class AlloyForgeryTagProviders {

    public static class Item extends FabricTagProvider.ItemTagProvider {

        public Item(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            getOrCreateTagBuilder(AlloyForgeryTags.Items.RAW_IRON_ORE_BLOCKS)
                    .add(Items.RAW_IRON_BLOCK);

            getOrCreateTagBuilder(AlloyForgeryTags.Items.RAW_GOLD_ORE_BLOCKS)
                    .add(Items.RAW_GOLD_BLOCK);

            getOrCreateTagBuilder(AlloyForgeryTags.Items.RAW_COPPER_ORE_BLOCKS)
                    .add(Items.RAW_COPPER_BLOCK);
        }
    }
}
