package io.wispforest.alloyforgery.client;

import io.wispforest.owo.ui.component.SpriteComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

public class FixedSpriteComponent extends SpriteComponent {
    protected FixedSpriteComponent(Sprite sprite) {
        super(sprite);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        context.draw();
    }

    public static FixedSpriteComponent fixedSprite(SpriteIdentifier spriteId) {
        return new FixedSpriteComponent(
            spriteId.getAtlasId().equals(Identifier.of("textures/atlas/gui.png"))
                ? MinecraftClient.getInstance().getGuiAtlasManager().getSprite(spriteId.getTextureId())
                : spriteId.getSprite()
        );
    }
}
