package wraith.alloyforgery.compat.emi;

import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.AlloyForgeScreenHandler;

import java.util.ArrayList;
import java.util.List;

public class AlloyForgeryEmiRecipeHandler implements StandardRecipeHandler<AlloyForgeScreenHandler> {
    @Override
    public List<Slot> getInputSources(AlloyForgeScreenHandler handler) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < handler.slots.size(); i++) {
            if (i < 2) continue;
            slots.add(handler.slots.get(i));
        }
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(AlloyForgeScreenHandler handler) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 2; i < 12; i++) {
            slots.add(handler.slots.get(i));
        }
        return slots;
    }

    @Override
    public @Nullable Slot getOutputSlot(AlloyForgeScreenHandler handler) {
        return StandardRecipeHandler.super.getOutputSlot(handler);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == AlloyForgeryEmiPlugin.FORGE_CATEGORY && recipe.supportsRecipeTree();
    }
}
