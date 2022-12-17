package wraith.alloyforgery.compat.emi;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

public class CustomTextWidget extends Widget {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private OrderedText text;
    private final int x, y;
    private final int color;
    private final boolean shadow;

    public CustomTextWidget(OrderedText text, int x, int y, int color, boolean shadow) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.shadow = shadow;
    }

    public void setText(OrderedText text){
        this.text = text;
    }

    @Override
    public Bounds getBounds() {
        int width = CLIENT.textRenderer.getWidth(text);
        int xOff = TextWidget.Alignment.START.offset(width);
        int yOff = TextWidget.Alignment.START.offset(CLIENT.textRenderer.fontHeight);
        return new Bounds(x + xOff, y + yOff, width, CLIENT.textRenderer.fontHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        matrices.push();
        int xOff = TextWidget.Alignment.START.offset(CLIENT.textRenderer.getWidth(text));
        int yOff = TextWidget.Alignment.START.offset(CLIENT.textRenderer.fontHeight);
        matrices.translate(xOff, yOff, 300);
        if (shadow) {
            CLIENT.textRenderer.drawWithShadow(matrices, text, x, y, color);
        } else {
            CLIENT.textRenderer.draw(matrices, text, x, y, color);
        }
        matrices.pop();
    }

}
