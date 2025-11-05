package io.wispforest.alloyforgery.compat.rei;

import io.wispforest.alloyforgery.client.AlloyForgeScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import io.wispforest.alloyforgery.forges.ForgeRegistry;

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
            if (screen.getScreenHandler().getRequiredTierData() > -1) return new Rectangle();

            return new Rectangle(screen.rootX() + 142, screen.rootY() + 20, 21, 24);
        }, AlloyForgeScreen.class, AlloyForgeryCommonPlugin.ID);
    }

//    @Override
//    public void registerDisplays(DisplayRegistry registry) {
//        registry.registerRecipeFiller(AlloyForgeRecipe.class, AlloyForgeRecipe.Type.INSTANCE, AlloyForgingDisplay::of);
//    }
}
