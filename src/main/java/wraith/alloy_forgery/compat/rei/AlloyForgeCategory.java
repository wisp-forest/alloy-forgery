package wraith.alloy_forgery.compat.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Label;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public class AlloyForgeCategory implements RecipeCategory<AlloyForgeDisplay> {

    public static TranslatableText NAME = new TranslatableText("container.alloy_forgery.rei.title");

    @Override
    public @NotNull Identifier getIdentifier() {
        return AlloyForgeryREIPlugin.ALLOY_FORGE_CATEGORY_ID;
    }

    @Override
    public @NotNull String getCategoryName() {
        return NAME.getString();
    }

    @Override
    public @NotNull EntryStack getLogo() {
        return EntryStack.create(new ItemStack(ItemRegistry.ITEMS.values().iterator().next()));
    }

    @Override
    public int getDisplayHeight() {
        return 88;
    }

    @Override
    public @NotNull List<Widget> setupDisplay(AlloyForgeDisplay recipeDisplay, Rectangle bounds) {
        Point origin = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        List<List<EntryStack>> inputs = recipeDisplay.getInputEntries();
        List<Slot> slots = new ArrayList<>();

        widgets.add(Widgets.createTexturedWidget(new Identifier(AlloyForgery.MOD_ID, "textures/gui/forge_controller.png"), origin.x - 6, origin.y - 6, 42, 25, 92, 38));

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 5; x++) {
                slots.add(Widgets.createSlot(new Point(origin.x - 4 + x * 18, origin.y - 4 + y * 18)).markInput().disableBackground());
            }
        }

        for (int i = 0; i < inputs.size(); i++) {
            if (!inputs.get(i).isEmpty()) {
                slots.get(i).entries(inputs.get(i));
            }
        }

        widgets.addAll(slots);
        widgets.add(Widgets.createResultSlotBackground(new Point(origin.x + 100, origin.y + 4)));
        widgets.add(Widgets.createSlot(new Point(origin.x + 100, origin.y + 4)).entries(recipeDisplay.getResultingEntries().get(0)).disableBackground().markOutput());

        Label tierWidget = Widgets.createLabel(new Point(origin.x - 4, origin.y + 38), new TranslatableText("container.alloy_forgery.rei.min_tier", recipeDisplay.getOutput().requiredTier)).color(0x3F3F3F).shadow(false);
        tierWidget.leftAligned();
        widgets.add(tierWidget);

        Label heatWidget = Widgets.createLabel(new Point(origin.x - 4, origin.y + 50), new TranslatableText("container.alloy_forgery.rei.heat", recipeDisplay.getOutput().heatAmount * 200)).color(0x3F3F3F).shadow(false);
        heatWidget.leftAligned();
        widgets.add(heatWidget);

        return widgets;
    }
}
