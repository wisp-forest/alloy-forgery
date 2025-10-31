package io.wispforest.alloyforgery.compat.rei;

import io.wispforest.alloyforgery.forges.ForgeTier;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.forges.ForgeRegistry;
import io.wispforest.alloyforgery.recipe.AlloyForgeRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.shedaniel.rei.api.client.gui.widgets.Widgets.*;

public class AlloyForgingCategory implements DisplayCategory<AlloyForgingDisplay> {

    final Identifier GUI_TEXTURE = AlloyForgery.id("textures/gui/forge_controller.png");
    final Identifier DARK_GUI_TEXTURE = AlloyForgery.id("textures/gui/forge_controller_dark.png");

    @Override
    public int getDisplayHeight() {
        return 88;
    }

    @Override
    public Renderer getIcon() {
        return ForgeRegistry.getControllerBlocks().isEmpty() ? EntryStack.empty() : EntryStacks.of(ForgeRegistry.getControllerBlocks().get(0));
    }

    @Override
    public Text getTitle() {
        return Text.translatable("container.alloy_forgery.rei.title");
    }

    @Override
    public List<Widget> setupDisplay(AlloyForgingDisplay display, Rectangle bounds) {
        final var origin = bounds.getLocation();
        final int x = origin.x, y = origin.y;

        final var widgets = new ArrayList<Widget>();
        final var texture = REIRuntime.getInstance().isDarkThemeEnabled() ? DARK_GUI_TEXTURE : GUI_TEXTURE;
        final var textColor = REIRuntime.getInstance().isDarkThemeEnabled() ? 0xafafaf : 0x3f3f3f;

        widgets.add(createRecipeBase(bounds));

        widgets.add(createTexturedWidget(texture, x + 10, y + 18, 42, 21, 124, 58));
        widgets.add(createTexturedWidget(texture, x + 115, y + 21, 176, 0, 15, 19));

        for (int i = 0; i < display.getInputEntries().size(); i++) {
            final var slotLocation = new Point(x + 12 + i % 5 * 18, y + 40 + (i > 4 ? 1 : 0) * 18);
            widgets.add(createSlot(slotLocation).entries(display.getInputEntries().get(i)).markInput().disableBackground());
            widgets.add(createTexturedWidget(texture, slotLocation.x - 1, slotLocation.y - 1, 208, 0, 18, 18));
        }

        final var resultSlot = createSlot(new Point(x + 113, y + 47));
        widgets.add(resultSlot.entries(display.getOutputEntries().get(0)).disableBackground().markOutput());

        final var minForgeTierName = ForgeTier.toName(true, display.minForgeTier());

        final var tierLabel = createLabel(new Point(x + 12, y + 11), Text.translatable("tooltip.alloy_forgery.recipe.min_tier", minForgeTierName));

        widgets.add(tierLabel.leftAligned().color(textColor).noShadow());
        widgets.add(createLabel(new Point(x + 12, y + 24), Text.translatable("tooltip.alloy_forgery.recipe.fuel_per_tick", display.fuelPerTick())).leftAligned().color(textColor).noShadow());

        final List<AlloyForgeRecipe.OverrideRange> overrides = new ArrayList<>(display.overrides().keySet());

        widgets.add(createButton(new Rectangle(x + 131, y + 6, 12, 12), Text.of("...")).onClick(new Consumer<>() {
            private int overrideIndex = 1;

            @Override
            public void accept(Button button) {
                var overrideTierName = overrideIndex == 0 ? minForgeTierName : overrides.get(overrideIndex - 1).toText(true);

                tierLabel.setMessage(Text.translatable("tooltip.alloy_forgery.recipe.min_tier", overrideTierName));

                resultSlot.clearEntries()
                    .entries(overrideIndex == 0 ? display.getOutputEntries().get(0) : EntryIngredients.of(display.overrides().get(overrides.get(overrideIndex - 1))));

                overrideIndex++;
                if (overrideIndex - 1 > overrides.size() - 1) overrideIndex = 0;
            }
        }).tooltipLine(Text.translatable("tooltip.alloy_forgery.recipe.button")).enabled(!overrides.isEmpty()));

        return widgets;
    }

    @Override
    public CategoryIdentifier<? extends AlloyForgingDisplay> getCategoryIdentifier() {
        return AlloyForgeryCommonPlugin.ID;
    }
}
