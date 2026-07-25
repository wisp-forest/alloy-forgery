package io.wispforest.alloyforgery.client;

import io.wispforest.alloyforgery.AlloyForgeScreenHandler;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import io.wispforest.alloyforgery.networking.DisableSlotToggle;
import io.wispforest.alloyforgery.utils.ForgeInputSlot;
import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.wispforest.alloyforgery.client.ComponentUtils.*;
import static io.wispforest.owo.ui.component.UIComponents.*;
import static io.wispforest.owo.ui.container.UIContainers.horizontalFlow;
import static io.wispforest.owo.ui.container.UIContainers.verticalFlow;

public class AlloyForgeScreen extends BaseOwoContainerScreen<FlowLayout, AlloyForgeScreenHandler> {

    private static final TextureAtlasSprite LAVA_SPRITE = new TextureAtlasSprite(TextureAtlas.LOCATION_BLOCKS, Identifier.parse("block/lava_still"));

    private static final Component ENABLED_SLOT_TEXT = Component.translatable("tooltip.alloy-forgery.enabled_slot");
    private static final Component DISABLED_SLOT_TEXT = Component.translatable("tooltip.alloy-forgery.disabled_slot");

    private TextureComponent fuelGauge;
    private TextureComponent progressGauge;
    private TextureComponent invalidCross;
    private FlowLayout lavaBar;

    private boolean allowSlotToggling = false;

    public AlloyForgeScreen(AlloyForgeScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        this.imageWidth = 176;
        this.imageHeight = 189;

        this.titleLabelY = 69420;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

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
                    invalidCross = texture(textureID("cross.png"), 0, 0, 14, 14, 14, 14)
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
                    label(Component.translatable("title.alloy-forgery.forge_controller"))
                        .color(getThemedValue(Color.ofRgb(0x3f3f3f), Color.WHITE))
                        .positioning(Positioning.relative(50, 12))
                )
                .child(
                    horizontalFlow(Sizing.fixed(26), Sizing.content())
                        .child(
                            button(Component.empty(), btn -> {
                                this.allowSlotToggling = !allowSlotToggling;

                                btn.tooltip(Component.translatable("tooltip.alloy-forgery.slot_toggle_" + (this.allowSlotToggling ? "enable" : "disable")));
                            }).renderer((context, button, delta) -> {
                                    BUTTON_RENDERER.draw(context, button, delta);

                                    context.push()
                                        .translate(button.getX(), button.getY());

                                    context.blit(RenderPipelines.GUI_TEXTURED, textureID("slot_locks.png"),4, 3, this.allowSlotToggling ? 10 : 0, 0, 10, 12, 20, 12);

                                    context.pop();
                                })
                                .tooltip(Component.translatable("tooltip.alloy-forgery.slot_toggle_disable"))
                                .sizing(Sizing.fixed(18), Sizing.fixed(18))
                        ).horizontalAlignment(HorizontalAlignment.CENTER)
                        .positioning(Positioning.absolute(140, 75))
                )
                .child(
                    makeInputSlots(this.getMenu().getInputSlots(), 1, AlloyForgery.CONFIG::darkModeTheme, slot -> new SlotComponent(slot.index){
                        // TODO: REMOVE AS THIS IS Used to resolve clipping item text and other effects due to how rendering is more buffered and uses the wrong scissor
                        @Override
                        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
                            this.didDraw = true;
                        }
                    }, this.menu::isSlotDisabled)
                        .positioning(Positioning.absolute(42, 41))
                )
                .surface((context, component) -> {
                    var backgroundTexture = themedTextureID("forge_controller_base.png");

                    context.push().translate(component.x(), component.y());

                    context.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, 0, 0, 0, 0, 176, 189, 176, 189);

                    context.blit(RenderPipelines.GUI_TEXTURED, themedTextureID("fuel_meter.png"), 5, 22, 0, 0, 22, 48, 44, 48);

                    context.blit(RenderPipelines.GUI_TEXTURED, textureID("forging_status.png"), 143, 21, 0, 0, 20, 22, 40, 22);

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
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.fuelGauge.visibleArea(PositionedRectangle.of(0, this.fuelGauge.height() - this.menu.getFuelProgress(), this.fuelGauge.fullSize()));
        this.progressGauge.visibleArea(PositionedRectangle.of(0, 0, this.progressGauge.width(), this.menu.getSmeltProgress()));
        this.lavaBar.horizontalSizing(Sizing.fixed(this.menu.getLavaProgress()));

        int requiredTier = this.menu.getRequiredTierData();

        if (requiredTier <= -1) {
            this.invalidCross
                .visibleArea(PositionedRectangle.of(0, 0, 0, 0))
                .tooltip(List.<ClientTooltipComponent>of());
        } else {
            this.invalidCross
                .resetVisibleArea()
                .tooltip(Component.translatable("tooltip.alloy-forgery.invalid_tier", requiredTier));
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics context, int x, int y) {
        super.renderTooltip(context, x, y);

        if (this.allowSlotToggling
            && this.hoveredSlot instanceof ForgeInputSlot
            && this.menu.getCarried().isEmpty()
            && !this.hoveredSlot.hasItem()
            && !this.menu.player().isSpectator()) {

            if (this.menu.isSlotDisabled(this.hoveredSlot)) {
                context.setTooltipForNextFrame(this.font, DISABLED_SLOT_TEXT, x, y);
            } else {
                context.setTooltipForNextFrame(this.font, ENABLED_SLOT_TEXT, x, y);
            }
        }
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType) {
        var player = this.menu.player();

        if (allowSlotToggling && slot instanceof ForgeInputSlot && !slot.hasItem() && !player.isSpectator()) {
            if (actionType == net.minecraft.world.inventory.ClickType.PICKUP) {
                if (this.menu.isSlotDisabled(slot)) {
                    this.setSlotEnabled(slot, true);
                } else /*if (this.handler.getCursorStack().isEmpty())*/ {
                    this.setSlotEnabled(slot, false);
                }
            } else if(actionType == ClickType.SWAP) {
                var itemStack = player.getInventory().getItem(button);
                if (this.menu.isSlotDisabled(slot) && !itemStack.isEmpty()) {
                    this.setSlotEnabled(slot, true);
                }
            }
        }

        super.slotClicked(slot, slotId, button, actionType);
    }

    private void setSlotEnabled(Slot slot, boolean enabled) {
        AlloyForgeNetworking.CHANNEL.clientHandle().send(new DisableSlotToggle(this.menu.forge, slot.getContainerSlot(), !enabled));

        super.handleSlotStateChanged(slot.index, this.menu.containerId, enabled);
        float f = enabled ? 1.0F : 0.75F;
        this.menu.player().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, f);
    }

    public int rootX() {
        return this.leftPos;
    }

    public int rootY() {
        return this.topPos;
    }
}
