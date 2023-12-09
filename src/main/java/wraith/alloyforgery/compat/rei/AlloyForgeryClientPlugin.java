package wraith.alloyforgery.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import wraith.alloyforgery.client.AlloyForgeScreen;
import wraith.alloyforgery.forges.ForgeRegistry;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

public class AlloyForgeryClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new AlloyForgingCategory());

        for (var controller : ForgeRegistry.getControllerBlocks()) {
            registry.addWorkstations(AlloyForgeryCommonPlugin.ID, EntryStacks.of(controller));
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerClickArea(screen -> {
            if(screen.getScreenHandler().getRequiredTierData() > -1) return new Rectangle();

            return new Rectangle(screen.rootX() + 142, screen.rootY() + 20, 21, 24);
        }, AlloyForgeScreen.class, AlloyForgeryCommonPlugin.ID);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(AlloyForgeRecipe.class, AlloyForgingDisplay::of);
    }
}
