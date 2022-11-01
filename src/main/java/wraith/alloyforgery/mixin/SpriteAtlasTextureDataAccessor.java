package wraith.alloyforgery.mixin;

import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteAtlasTexture.Data.class)
public interface SpriteAtlasTextureDataAccessor {
    @Accessor("width")
    int af$getWidth();

    @Accessor("height")
    int af$getHeight();
}
