package wraith.alloyforgery.compat.rei;

import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.clean.InputCleanHandler;
import me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import net.minecraft.inventory.Inventory;
import wraith.alloyforgery.AlloyForgeScreenHandler;
import java.util.ArrayList;
import java.util.List;

public record AlloyForgeryMenuInfo(AlloyForgingDisplay display) implements SimplePlayerInventoryMenuInfo<AlloyForgeScreenHandler, AlloyForgingDisplay> {

    @Override
    public Iterable<SlotAccessor> getInputSlots(MenuInfoContext<AlloyForgeScreenHandler, ?, AlloyForgingDisplay> context) {
        Inventory inventory = context.getMenu().getControllerInventory();

        List<SlotAccessor> list = new ArrayList<>(inventory.size() - 2);
        for (int i = 0; i < inventory.size() - 2; i++) {
            list.add(SlotAccessor.fromContainer(inventory, i));
        }

        return list;
    }

    @Override
    public AlloyForgingDisplay getDisplay() {
        return this.display;
    }

    @Override
    public InputCleanHandler<AlloyForgeScreenHandler, AlloyForgingDisplay> getInputCleanHandler() {
        return context -> {
            for (SlotAccessor gridStack : getInputSlots(context)) {
                InputCleanHandler.returnSlotsToPlayerInventory(context, getDumpHandler(), gridStack);
            }

            clearInputSlots(context.getMenu());
        };
    }
}
