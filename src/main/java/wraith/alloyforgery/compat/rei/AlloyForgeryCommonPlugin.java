package wraith.alloyforgery.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;
import wraith.alloyforgery.AlloyForgeScreenHandler;
import wraith.alloyforgery.AlloyForgery;

public class AlloyForgeryCommonPlugin implements REIServerPlugin {

    public static final CategoryIdentifier<AlloyForgingDisplay> ID = CategoryIdentifier.of(AlloyForgery.id("forging"));

    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        registry.register(ID, AlloyForgeScreenHandler.class, SimpleMenuInfoProvider.of(AlloyForgeryMenuInfo::new));
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(ID, AlloyForgingDisplay.Serializer.INSTANCE);
    }
}
