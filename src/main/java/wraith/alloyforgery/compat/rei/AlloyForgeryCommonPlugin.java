package wraith.alloyforgery.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

public class AlloyForgeryCommonPlugin implements REICommonPlugin {

    public static final CategoryIdentifier<AlloyForgingDisplay> ID = CategoryIdentifier.of(AlloyForgery.id("forging"));

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {

        registry.beginRecipeFiller(AlloyForgeRecipe.class).fill(AlloyForgingDisplay::of);
    }

//    @Override
//    public void registerMenuInfo(MenuInfoRegistry registry) {
//        registry.register(ID, AlloyForgeScreenHandler.class, SimpleMenuInfoProvider.of(AlloyForgeryMenuInfo::new));
//    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(ID.getIdentifier(), AlloyForgingDisplay.SERIALIZER);
    }
}
