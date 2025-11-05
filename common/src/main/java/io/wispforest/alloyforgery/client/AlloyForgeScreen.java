package io.wispforest.alloyforgery.client;

import io.wispforest.alloyforgery.AlloyForgeScreenHandler;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import io.wispforest.alloyforgery.networking.DisableSlotToggle;
import io.wispforest.alloyforgery.utils.ForgeInputSlot;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.wispforest.owo.ui.container.Containers.*;
import static io.wispforest.owo.ui.component.Components.*;

public class AlloyForgeScreen extends BaseOwoHandledScreen<FlowLayout, AlloyForgeScreenHandler> {

    private static final SpriteIdentifier LAVA_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.of("block/lava_still"));

    private static final Text ENABLED_SLOT_TEXT = Text.translatable("tooltip.alloy_forgery.enabled_slot");
    private static final Text DISABLED_SLOT_TEXT = Text.translatable("tooltip.alloy_forgery.disabled_slot");

    private TextureComponent fuelGauge;
    private TextureComponent progressGauge;
    private TextureComponent invalidCross;
    private FlowLayout lavaBar;

    private boolean allowSlotToggling = false;

    public AlloyForgeScreen(AlloyForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 176;
        this.backgroundHeight = 189;

        this.titleY = 69420;
        this.playerInventoryTitleY = this.backgroundHeight - 93;
    }

    private Identifier themedTextureID(String suffix) {
        return AlloyForgery.id("textures/gui/" + (AlloyForgery.CONFIG.darkModeTheme() ? "dark" : "light") + "/" + suffix);
    }

    private Identifier textureID(String suffix) {
        return AlloyForgery.id("textures/gui/" + suffix);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    public <T> T getThemedValue(T light, T dark) {
        return AlloyForgery.CONFIG.darkModeTheme() ? dark : light;
    }

//    @Override
//    protected int getLayerZOffset(HandledScreenLayer layer) {
//        return switch (layer) {
//            case SLOT -> 400;
//            case CURSOR_ITEM -> 500;
//            case ITEM_TOOLTIP -> 550;
//        };
//    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(
            verticalFlow(Sizing.fixed(176), Sizing.fixed(189))
                .child(
                    // fuel Gauge
                    fuelGauge = texture(themedTextureID("fuel_meter.png"), 22, 0, 22, 48, 44, 48)
                        .visibleArea(PositionedRectangle.of(0, 0, 22, 0))
                        .configure(textureComponent -> {
                            textureComponent.positioning(Positioning.absolute(5, 22))
                                .id("fuel-gauge");
                        })
                )
                .child(
                    // progress Gauge
                    progressGauge = texture(textureID("forging_status.png"), 24, 3, 15, 19, 40, 22)
                        .visibleArea(PositionedRectangle.of(0, 0, 15, 0))
                        .configure(textureComponent -> {
                            textureComponent.positioning(Positioning.absolute(147, 24))
                                .id("progress-gauge");
                        })
                )
                .child(
                    // Invalid Cross
                    invalidCross = texture(textureID("forge_controller.png"), 241, 1, 14, 14)
                        .visibleArea(PositionedRectangle.of(0, 0, 14, 0))
                        .configure(textureComponent -> {
                            textureComponent.positioning(Positioning.absolute(147, 25))
                                .id("invalid-cross");
                        })
                )
                .child(
                    lavaBar = horizontalFlow(Sizing.fixed(0), Sizing.fixed(10))
                        .child(sprite(LAVA_SPRITE))
                        .child(sprite(LAVA_SPRITE))
                        .child(sprite(LAVA_SPRITE))
                        .child(sprite(LAVA_SPRITE))
                        .configure(layout -> {
                            layout.positioning(Positioning.absolute(63, 4))
                                .id("lava-bar");
                        })
                )
                .child(
                    label(Text.translatable("container.alloy_forgery.forge_controller"))
                        .color(getThemedValue(Color.ofRgb(0x3f3f3f), Color.WHITE))
                        .positioning(Positioning.relative(50, 12))
                )
                .child(
                    horizontalFlow(Sizing.fixed(26), Sizing.content())
                        .child(
                            button(Text.empty(), btn -> {
                                this.allowSlotToggling = !allowSlotToggling;

                                btn.tooltip(Text.translatable("tooltip.alloy_forgery.slot_toggle_" + (this.allowSlotToggling ? "enable" : "disable")));
                            }).tooltip(Text.translatable("tooltip.alloy_forgery.slot_toggle_disable"))
                                .sizing(Sizing.fixed(14), Sizing.fixed(14))
                        ).horizontalAlignment(HorizontalAlignment.CENTER)
                        .positioning(Positioning.absolute(140, 75))
                )
                .child(
                    Util.make(
                        verticalFlow(Sizing.content(), Sizing.content()),
                        layout -> {
                            var inputSlots = this.getScreenHandler().getInputSlots().stream()
                                .map(slot ->
                                    verticalFlow(Sizing.content(), Sizing.content())
                                        .child(slotAsComponent(slot.id))
                                        .padding(Insets.of(1))
                                        .surface((context, component) -> {
                                            var slotTexture = themedTextureID("input_slot_background.png");

                                            context.drawTexture(RenderLayer::getGuiTextured, slotTexture, component.x(), component.y() , this.handler.isSlotDisabled(slot) ? 18 : 0, 0,18, 18, 36, 18);
                                        })
                                )
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
                        }
                    ).padding(Insets.of(1))
                        //.horizontalAlignment(HorizontalAlignment.CENTER)
                        .positioning(Positioning.absolute(42, 41))
                )
                .surface((context, component) -> {
                    var backgroundTexture = themedTextureID("forge_controller_base.png");

                    context.push().translate(component.x(), component.y(), 0);

                    context.drawTexture(RenderLayer::getGuiTextured, backgroundTexture, 0, 0, 0, 0, 176, 189, 176, 189);

                    context.drawTexture(RenderLayer::getGuiTextured, themedTextureID("fuel_meter.png"), 5, 22, 0, 0, 22, 48, 44, 48);

                    context.drawTexture(RenderLayer::getGuiTextured, textureID("forging_status.png"), 143, 21, 0, 0, 20, 22, 40, 22);

                    context.pop();
                })
                .id("main-layout")
        );

        rootComponent
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .surface(Surface.VANILLA_TRANSLUCENT);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.fuelGauge.visibleArea(PositionedRectangle.of(0, this.fuelGauge.height() - this.handler.getFuelProgress(), this.fuelGauge.fullSize()));
        this.progressGauge.visibleArea(PositionedRectangle.of(0, 0, this.progressGauge.width(), this.handler.getSmeltProgress()));
        this.lavaBar.horizontalSizing(Sizing.fixed(this.handler.getLavaProgress()));

        int requiredTier = this.handler.getRequiredTierData();

        if (requiredTier <= -1) {
            this.invalidCross
                .visibleArea(PositionedRectangle.of(0, 0, 0, 0))
                .tooltip(List.<TooltipComponent>of());
        } else {
            this.invalidCross
                .resetVisibleArea()
                .tooltip(Text.translatable("tooltip.alloy_forgery.invalid_tier", requiredTier));
        }

        if (this.allowSlotToggling
            && this.focusedSlot instanceof ForgeInputSlot
            && this.handler.getCursorStack().isEmpty()
            && !this.focusedSlot.hasStack()
            && !this.handler.player().isSpectator()) {

            if (this.handler.isSlotDisabled(this.focusedSlot)) {
                context.drawTooltip(this.textRenderer, DISABLED_SLOT_TEXT, mouseX, mouseY);
            } else {
                context.drawTooltip(this.textRenderer, ENABLED_SLOT_TEXT, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        var player = this.handler.player();

        if (allowSlotToggling && slot instanceof ForgeInputSlot && !slot.hasStack() && !player.isSpectator()) {
            if (actionType == SlotActionType.PICKUP) {
                if (this.handler.isSlotDisabled(slot)) {
                    this.enableInputSlot(slot);
                } else /*if (this.handler.getCursorStack().isEmpty())*/ {
                    this.disableInputSlot(slot);
                }
            } else if(actionType == SlotActionType.SWAP) {
                var itemStack = player.getInventory().getStack(button);
                if (this.handler.isSlotDisabled(slot) && !itemStack.isEmpty()) {
                    this.enableInputSlot(slot);
                }
            }
        }

        super.onMouseClick(slot, slotId, button, actionType);
    }

    private void enableInputSlot(Slot slot) {
        this.setSlotEnabled(slot, true);
    }

    private void disableInputSlot(Slot slot) {
        this.setSlotEnabled(slot, false);
    }

    private void setSlotEnabled(Slot slot, boolean enabled) {
        AlloyForgeNetworking.CHANNEL.clientHandle().send(new DisableSlotToggle(this.handler.forge, slot.getIndex(), !enabled));

        super.onSlotChangedState(slot.id, this.handler.syncId, enabled);
        float f = enabled ? 1.0F : 0.75F;
        this.handler.player().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, f);
    }

    public int rootX() {
        return this.x;
    }

    public int rootY() {
        return this.y;
    }
}
