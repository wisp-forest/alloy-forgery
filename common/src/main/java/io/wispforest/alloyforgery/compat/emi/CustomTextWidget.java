package io.wispforest.alloyforgery.compat.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import dev.emi.emi.api.widget.*;

public class CustomTextWidget extends Widget {
    private static final Minecraft CLIENT = Minecraft.getInstance();
    private FormattedCharSequence text;
    private final int x, y;
    private final int color;
    private final boolean shadow;

    public CustomTextWidget(FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.shadow = shadow;
    }

    public void setText(FormattedCharSequence text) {
        this.text = text;
    }

    @Override
    public Bounds getBounds() {
        int width = CLIENT.font.width(text);
        int xOff = TextWidget.Alignment.START.offset(width);
        int yOff = TextWidget.Alignment.START.offset(CLIENT.font.lineHeight);
        return new Bounds(x + xOff, y + yOff, width, CLIENT.font.lineHeight);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        var matrices = context.pose();
        matrices.pushMatrix();
        int xOff = TextWidget.Alignment.START.offset(CLIENT.font.width(text));
        int yOff = TextWidget.Alignment.START.offset(CLIENT.font.lineHeight);
        matrices.translate(xOff, yOff);
        context.drawString(CLIENT.font, text, x, y, color, shadow);
        matrices.popMatrix();
    }

}
