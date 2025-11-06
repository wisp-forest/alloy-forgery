package io.wispforest.alloyforgery.client;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.wispforest.owo.ui.container.Containers.horizontalFlow;
import static io.wispforest.owo.ui.container.Containers.verticalFlow;

public class ComponentUtils {
    public static final ButtonComponent.Renderer BUTTON_RENDERER = createThemedButtonRenderer(AlloyForgery.CONFIG::darkModeTheme);

    public static Identifier themedTextureID(String suffix) {
        return themedTextureID(suffix, AlloyForgery.CONFIG::darkModeTheme);
    }

    public static Identifier themedTextureID(String suffix, Supplier<Boolean> isDarkMode) {
        return AlloyForgery.id("textures/gui/theme/" + (isDarkMode.get() ? "dark" : "light") + "/" + suffix);
    }

    public static Identifier textureID(String suffix) {
        return AlloyForgery.id("textures/gui/" + suffix);
    }

    public static <T> T getThemedValue(T light, T dark) {
        return AlloyForgery.CONFIG.darkModeTheme() ? dark : light;
    }

    public static ButtonComponent.Renderer createThemedButtonRenderer(Supplier<Boolean> isDarkMode) {
        return (context, button, delta) -> {
            NinePatchTexture.draw(getBtnTexture(button, isDarkMode), context, button.getX(), button.getY(), button.width(), button.height());
        };
    }

    private static Identifier getBtnTexture(ButtonComponent btn, Supplier<Boolean> isDarkMode) {
        var btnType = (btn.visible ? (btn.isHovered() ? "hovered" : "active") : "disabled");
        var themeType = isDarkMode.get() ? "dark" : "light";

        return AlloyForgery.id("theme/" + themeType + "/button/" + btnType);
    }

    public static <T> Component makeInputSlots(List<T> entries, int paddingInset, Supplier<Boolean> isDarkMode, Function<T, Component> slotBuilder, Predicate<T> isSlotDisabled) {
        return verticalFlow(Sizing.content(), Sizing.content())
            .<FlowLayout>configure(layout -> {
                var inputSlots = entries.stream()
                    .map(t -> verticalFlow(Sizing.content(), Sizing.content())
                        .child(slotBuilder.apply(t))
                        .padding(Insets.of(paddingInset))
                        .surface((context, component) -> {
                            var slotTexture = themedTextureID("input_slot_background.png", isDarkMode);

                            context.drawTexture(RenderLayer::getGuiTextured, slotTexture, component.x(), component.y(), isSlotDisabled.test(t) ? 18 : 0, 0,18, 18, 36, 18);
                        }))
                    .toList();

                var middleIndex = (int) Math.floor(inputSlots.size() / 2f);

                boolean onlySingleRow = inputSlots.size() <= 2;

                if (onlySingleRow) middleIndex = inputSlots.size();

                var topSlots = inputSlots.subList(0, middleIndex);

                // 42, 41
                layout.child(horizontalFlow(Sizing.content(), Sizing.content()).children(topSlots));

                if (!onlySingleRow) {
                    var bottomSlots = inputSlots.subList(middleIndex, inputSlots.size());

                    layout.child(horizontalFlow(Sizing.content(), Sizing.content()).children(bottomSlots));
                }

                layout.surface(
                    (context, component) -> context.drawRectOutline(component.x(), component.y(), component.width(), component.height(), getThemedValue(0xFF373737, 0xFF0B0B0B))
                );
            })
            .padding(Insets.of(1))
            .horizontalAlignment(HorizontalAlignment.CENTER);
    }
}
