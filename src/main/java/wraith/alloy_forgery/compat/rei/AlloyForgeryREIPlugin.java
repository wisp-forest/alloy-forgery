package wraith.alloy_forgery.compat.rei;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.registry.ItemRegistry;

public class AlloyForgeryREIPlugin implements REIPluginV0 {

    public static final Identifier ID = new Identifier(AlloyForgery.MOD_ID, "rei_plugin");
    public static final Identifier ALLOY_FORGE_CATEGORY_ID = new Identifier(AlloyForgery.MOD_ID, "alloy_forge");

    @Override
    public Identifier getPluginIdentifier() {
        return ID;
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {

        for (Item item : ItemRegistry.ITEMS.values()) {
            recipeHelper.registerWorkingStations(ALLOY_FORGE_CATEGORY_ID, EntryStack.create(new ItemStack(item)));
        }

        recipeHelper.registerLiveRecipeGenerator(new AlloyForgeRecipeGenerator());
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        recipeHelper.registerCategories(new AlloyForgeCategory());
    }
}
