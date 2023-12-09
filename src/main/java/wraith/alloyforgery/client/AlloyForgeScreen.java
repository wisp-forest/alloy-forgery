package wraith.alloyforgery.client;

import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import wraith.alloyforgery.AlloyForgeScreenHandler;
import wraith.alloyforgery.AlloyForgery;

import java.util.List;

public class AlloyForgeScreen extends BaseUIModelHandledScreen<FlowLayout, AlloyForgeScreenHandler> {

    private TextureComponent fuelGauge;
    private TextureComponent progressGauge;
    private TextureComponent invalidCross;
    private FlowLayout lavaBar;

    public AlloyForgeScreen(AlloyForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.asset(AlloyForgery.id("forge")));
        this.backgroundWidth = 176;
        this.backgroundHeight = 189;

        this.titleY = 69420;
        this.playerInventoryTitleY = this.backgroundHeight - 93;
    }

    @Override
    protected void build(FlowLayout layout) {
        this.fuelGauge = layout.childById(TextureComponent.class, "fuel-gauge");
        this.invalidCross = layout.childById(TextureComponent.class, "invalid-cross");
        this.progressGauge = layout.childById(TextureComponent.class, "progress-gauge");
        this.lavaBar = layout.childById(FlowLayout.class, "lava-bar");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.fuelGauge.visibleArea(PositionedRectangle.of(0, this.fuelGauge.height() - this.handler.getFuelProgress(), this.fuelGauge.fullSize()));
        this.progressGauge.visibleArea(PositionedRectangle.of(0, 0, this.progressGauge.width(), this.handler.getSmeltProgress()));
        this.lavaBar.horizontalSizing(Sizing.fixed(this.handler.getLavaProgress()));

        int requiredTier = this.handler.getRequiredTierData();

        if(requiredTier <= -1) {
            this.invalidCross
                    .visibleArea(PositionedRectangle.of(0, 0, 0, 0))
                    .tooltip(List.<TooltipComponent>of());
        } else{
            this.invalidCross
                    .resetVisibleArea()
                    .tooltip(Text.translatable("tooltip.alloy_forgery.invalid_tier", requiredTier));
        }
    }

    public int rootX() {
        return this.x;
    }

    public int rootY() {
        return this.y;
    }
}
