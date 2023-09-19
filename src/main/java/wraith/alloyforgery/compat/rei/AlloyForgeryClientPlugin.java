package wraith.alloyforgery.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import wraith.alloyforgery.client.AlloyForgeScreen;
import wraith.alloyforgery.forges.ForgeRegistry;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import wraith.alloyforgery.recipe.AlloyForgeRecipeSerializer;
import wraith.alloyforgery.recipe.handlers.BlastFurnaceRecipeHandler;

import java.util.List;

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
            return new Rectangle(screen.rootX() + 142, screen.rootY() + 20, 21, 24);
        }, AlloyForgeScreen.class, AlloyForgeryCommonPlugin.ID);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(AlloyForgeRecipe.class, AlloyForgingDisplay::of);

        List<Recipe<?>> alloyForgeryRecipes = registry.getRecipeManager().values().stream()
                .filter(recipe -> recipe.getType() == AlloyForgeRecipe.Type.INSTANCE)
                .toList();

        registry.registerFiller(BlastingRecipe.class, recipe -> {
            if(BlastFurnaceRecipeHandler.isUniqueRecipe(alloyForgeryRecipes, recipe)){
                return AlloyForgingDisplay.of(recipe);
            }

            return null;
        });
    }
}
