package wraith.alloyforgery.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgeScreenHandler;
import wraith.alloyforgery.AlloyForgery;

public class AlloyForgeScreen extends HandledScreen<AlloyForgeScreenHandler> {

    private static final Identifier TEXTURE = AlloyForgery.id("textures/gui/forge_controller.png");

    public AlloyForgeScreen(AlloyForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 173;
        this.playerInventoryTitleY = this.backgroundHeight - 93;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        renderBackground(matrices);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        drawTexture(matrices, x + 147, y + 8, 176, 0, 15, handler.getSmeltProgress());
        drawTexture(matrices, x + 5, y + 54 - handler.getFuelProgress(), 176, 68 - handler.getFuelProgress(), 22, handler.getFuelProgress());

        for (int i = 2; i < 12; i++) {
            final var slot = handler.slots.get(i);
            if (!slot.hasStack()) continue;
            drawTexture(matrices, x + slot.x - 1, y + slot.y - 1, 208, 0, 18, 18);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = backgroundWidth / 2 - textRenderer.getWidth(title) / 2;
    }
}
