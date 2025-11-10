package io.wispforest.alloyforgery.compat.rei;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.client.ComponentUtils;
import io.wispforest.alloyforgery.forges.ForgeTier;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import io.wispforest.alloyforgery.forges.ForgeRegistry;
import io.wispforest.alloyforgery.recipe.AlloyForgeRecipe;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.wispforest.owo.ui.container.Containers.*;
import static io.wispforest.owo.ui.component.Components.*;
import static io.wispforest.alloyforgery.client.ComponentUtils.*;

public class AlloyForgingCategory implements DisplayCategory<AlloyForgingDisplay> {

    @Override
    public int getDisplayHeight() {
        return 84;
    }

    @Override
    public int getDisplayWidth(AlloyForgingDisplay display) {
        return DisplayCategory.super.getDisplayWidth(display);
    }

    @Override
    public Renderer getIcon() {
        return ForgeRegistry.getControllerBlocks().isEmpty() ? EntryStack.empty() : EntryStacks.of(ForgeRegistry.getControllerBlocks().get(0));
    }

    @Override
    public Text getTitle() {
        return AlloyForgery.translation("title", "recipe");
    }

    @Override
    public List<Widget> setupDisplay(AlloyForgingDisplay display, Rectangle bounds) {
        final var rei = REIRuntime.getInstance();
        final var textColor = rei.isDarkThemeEnabled() ? 0xafafaf : 0x3f3f3f;
        final var adapter = new ReiUIAdapter<>(bounds, Containers::verticalFlow);

        final var list = new ArrayList<Component>();

        for (int i = 0; i < 10; i++) {
            var entry = (i < display.getInputEntries().size())
                ? display.getInputEntries().get(i)
                : null;

            var component = adapter.wrap(Widgets::createSlot, slot -> {
                slot.disableBackground();

                if (entry != null) slot.entries(entry).markInput();
            });

            list.add(component);
        }

        final var minForgeTierName = ForgeTier.toName(true, display.minForgeTier());
        final var slotEntries = new MutableObject<Slot>(null);

        adapter.rootComponent()
            .child(
                verticalFlow(Sizing.fill(100), Sizing.fill(100))
                    .child(
                        verticalFlow(Sizing.content(), Sizing.content())
                            .child(
                                label(AlloyForgery.tooltipTranslation("recipe.min_tier", minForgeTierName))
                                    .color(Color.ofRgb(textColor))
                                    .shadow(false)
                                    .margins(Insets.top(3))
                                    .id("tier-label")
                            )
                            .child(
                                label(AlloyForgery.tooltipTranslation("recipe.fuel_per_tick", display.fuelPerTick()))
                                    .color(Color.ofRgb(textColor))
                                    .shadow(false)
                                    .margins(Insets.top(3))
                            )
                            .child(
                                horizontalFlow(Sizing.fixed(124), Sizing.fixed(38))
                                    .child(
                                        makeInputSlots(list, 0, rei::isDarkThemeEnabled, Function.identity(), component -> false)
                                    )
                                    .child(
                                        verticalFlow(Sizing.content(), Sizing.content())
                                            .child(adapter.wrap(
                                                Widgets::createSlot,
                                                slot -> {
                                                    slotEntries.setValue(slot.entries(display.getOutputEntries().getFirst()).disableBackground().markOutput());
                                                }
                                            ))
                                            .padding(Insets.of(4))
                                            .surface((context, component) -> {
                                                var backgroundTexture = ComponentUtils.themedTextureID("forge_controller_base.png", rei::isDarkThemeEnabled);

                                                context.drawTexture(RenderLayer::getGuiTextured, backgroundTexture, component.x(), component.y(), 140, 45, 26, 26, 176, 189);
                                            })
                                            .margins(Insets.left(6))
                                    )
                                    .verticalAlignment(VerticalAlignment.CENTER)
                                    .margins(Insets.top(6))
                            ).margins(Insets.left(4))
                    )
                    .child(
                        button(Text.of("..."), buttonComponent -> {})
                            .<ButtonComponent>configure(btn -> {
                                final List<AlloyForgeRecipe.OverrideRange> overrides = new ArrayList<>(display.overrides().keySet());

                                btn.onPress(new Consumer<>() {
                                    private int overrideIndex = 1;

                                    @Override
                                    public void accept(ButtonComponent buttonComponent) {
                                        var overrideTierName = overrideIndex == 0 ? minForgeTierName : overrides.get(overrideIndex - 1).toText(true);

                                        adapter.rootComponent().childById(LabelComponent.class, "tier-label")
                                            .text(AlloyForgery.tooltipTranslation("recipe.min_tier", overrideTierName));

                                        slotEntries.getValue()
                                            .clearEntries()
                                            .entries(overrideIndex == 0 ? display.getOutputEntries().get(0) : EntryIngredients.of(display.overrides().get(overrides.get(overrideIndex - 1))));

                                        overrideIndex++;
                                        if (overrideIndex - 1 > overrides.size() - 1) overrideIndex = 0;
                                    }
                                }).active(!overrides.isEmpty())
                                    .renderer(createThemedButtonRenderer(rei::isDarkThemeEnabled))
                                    .tooltip(AlloyForgery.tooltipTranslation("recipe.button"));
                            })
                            .sizing(Sizing.fixed(14))
                            .margins(Insets.of(0, 0, 0, 0))
                            .positioning(Positioning.relative(100, 0))
                    ).surface((context, component) -> {
                        (REIRuntime.getInstance().isDarkThemeEnabled() ? Surface.DARK_PANEL : Surface.PANEL)
                            .draw(context, component);
                    }).padding(Insets.of(6))
            );

        adapter.prepare();

        return List.of(adapter);
    }

    @Override
    public CategoryIdentifier<? extends AlloyForgingDisplay> getCategoryIdentifier() {
        return AlloyForgeryCommonPlugin.ID;
    }
}
