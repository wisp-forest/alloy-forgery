package wraith.alloyforgery.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgeScreenHandler;
import wraith.alloyforgery.AlloyForgery;

public class AlloyForgeScreen extends HandledScreen<AlloyForgeScreenHandler> {

    private static final Identifier TEXTURE = AlloyForgery.id("textures/gui/forge_controller.png");
    private final SpriteIdentifier lavaSpriteId = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("block/lava_still"));

    public AlloyForgeScreen(AlloyForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 189;
        this.playerInventoryTitleY = this.backgroundHeight - 93;
        this.titleY = this.titleY + 16;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        renderBackground(matrices);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matrices, this.x, this.y, 0, 0, backgroundWidth, backgroundHeight);

        drawTexture(matrices, this.x + 147, this.y + 24, 176, 0, 15, handler.getSmeltProgress());
        drawTexture(matrices, this.x + 5, this.y + 70 - handler.getFuelProgress(), 176, 68 - handler.getFuelProgress(), 22, handler.getFuelProgress());

        for (int i = 2; i < 12; i++) {
            final var slot = handler.slots.get(i);
            if (!slot.hasStack()) continue;
            drawTexture(matrices, this.x + slot.x - 1, this.y + slot.y - 1, 208, 0, 18, 18);
        }

        var lavaSprite = lavaSpriteId.getSprite();
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);

        final var fullFrames = this.handler.getLavaProgress() / 16;
        for (int i = 0; i < fullFrames; i++) {
            drawTexture(matrices, this.x + 63 + i * 16, this.y + 4, lavaSprite.getX(), lavaSprite.getY() + 2,
                    16, 10, 1024, 1024);
        }

        drawTexture(matrices, this.x + 63 + fullFrames * 16, this.y + 4, lavaSprite.getX(), lavaSprite.getY() + 2,
                (this.handler.getLavaProgress() - fullFrames * 16), 10, 1024, 1024);
    }

    public int rootX() {
        return this.x;
    }

    public int rootY() {
        return this.y;
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
