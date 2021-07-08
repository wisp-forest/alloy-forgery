package wraith.alloyforgery.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import wraith.alloyforgery.forges.ForgeRegistry;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

public class AlloyForgeryPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new AlloyForgingCategory());

        for (var controller : ForgeRegistry.getControllerBlocks()) {
            registry.addWorkstations(AlloyForgingCategory.ID, EntryStacks.of(controller));
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(AlloyForgeRecipe.class, AlloyForgingDisplay::new);
    }
}
