package wraith.alloyforgery.compat.rei;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;
import wraith.alloyforgery.AlloyForgeScreenHandler;

public class AlloyForgeryCommonPlugin implements REIServerPlugin {

    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        registry.register(AlloyForgingCategory.ID, AlloyForgeScreenHandler.class, SimpleMenuInfoProvider.of(AlloyForgeryMenuInfo::new));
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(AlloyForgingCategory.ID, AlloyForgingDisplay.Serializer.INSTANCE);
    }
}
