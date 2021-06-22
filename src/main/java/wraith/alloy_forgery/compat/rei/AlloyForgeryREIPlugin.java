package wraith.alloy_forgery.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.Item;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.registry.ItemRegistry;

public class AlloyForgeryREIPlugin implements REIClientPlugin {

    public static final CategoryIdentifier<AlloyForgeDisplay> ALLOY_FORGE_CATEGORY_ID = CategoryIdentifier.of(AlloyForgery.MOD_ID, "alloy_forging");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new AlloyForgeCategory());

        for (Item item : ItemRegistry.ITEMS.values()) {
            registry.addWorkstations(ALLOY_FORGE_CATEGORY_ID, EntryStacks.of(item));
        }

    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerDisplayGenerator(ALLOY_FORGE_CATEGORY_ID, new AlloyForgeRecipeGenerator());

    }
}
