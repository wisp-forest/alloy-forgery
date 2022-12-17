package wraith.alloyforgery.compat.emi;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.ButtonWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.BooleanSupplier;

public class CustomButtonWidget extends Widget {

    private final int x, y;
    private final BooleanSupplier isActive;
    private final ButtonWidget.ClickAction action;
    private final List<TooltipComponent> tooltipComponent = List.of(TooltipComponent.of(Text.translatable("container.alloy_forgery.rei.button").asOrderedText()));

    public CustomButtonWidget(int x, int y, BooleanSupplier isActive, ButtonWidget.ClickAction action) {
        this.x = x;
        this.y = y;
        this.isActive = isActive;
        this.action = action;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, 12, 12);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        EmiPort.setPositionTexShader();
        RenderSystem.setShaderTexture(0, AlloyForgeryEmiRecipe.GUI_TEXTURE);
        int v = 68;
        boolean active = this.isActive.getAsBoolean();
        if (!active) {
            v += 24;
        } else if (getBounds().contains(mouseX, mouseY)) {
            v += 12;
        }
        RenderSystem.enableDepthTest();
        DrawableHelper.drawTexture(matrices, this.x, this.y, 176, v, 12, 12, 256, 256);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!isActive.getAsBoolean()) return false;
        action.click(mouseX, mouseY, button);
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        return true;
    }

    @Override
    public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
        return tooltipComponent;
    }
}
