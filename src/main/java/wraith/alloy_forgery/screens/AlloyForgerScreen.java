package wraith.alloy_forgery.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.alloy_forgery.utils.Utils;

public class AlloyForgerScreen extends HandledScreen<ScreenHandler> {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/forge_controller.png");
    private final AlloyForgerScreenHandler handler;

    public AlloyForgerScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = (AlloyForgerScreenHandler) handler;
        this.backgroundWidth = 176;
        this.backgroundHeight = 173;
        this.titleX += 34;
        this.playerInventoryTitleY = this.backgroundHeight - 93;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        //Fuel
        if (this.handler.slots.get(0).getStack().isEmpty()) {
            this.drawTexture(matrices, x + 8, y + 58, 192, 0, 16, 16);
        }
        if ((this.handler.isHeating())) {
            int progress = (this.handler.getHeatProgress());
            this.drawTexture(matrices, x + 5, y + 5 + 48 - progress, this.backgroundWidth, 67 - progress, 22, progress);
        }
        if ((this.handler.isSmelting())) {
            int progress = (this.handler.getSmeltingProgress());
            this.drawTexture(matrices, x + 147, y + 8, this.backgroundWidth, 0, 16, progress);
        }
        for (int y = 0; y < 2; ++y) {
            for (int x = 0; x < 5; ++x) {
                if(!this.handler.slots.get(2 + y*5 + x).getStack().isEmpty()) {
                    this.drawTexture(matrices, this.x + 43 + x * 18, this.y + 26 + y * 18, 208, 0, 18, 18);
                }
            }
        }
        if (Utils.isMouseInside(mouseX, mouseY, this.x + 3, this.y + 3, this.x + 29, this.y + 56)) {
            String time = this.handler.getHeat() + "/" + this.handler.getMaxHeat();
            renderTooltip(matrices, new LiteralText(time), mouseX, mouseY);
        }
        else if (Utils.isMouseInside(mouseX, mouseY, this.x + 143, this.y + 5, this.x + 161, this.y + 26)) {
            String time = this.handler.getSmeltingPercent() + "%";
            renderTooltip(matrices, new LiteralText(time), mouseX, mouseY);
        }
    }

}
