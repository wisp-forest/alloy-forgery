package wraith.alloyforgery.mixin;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.alloyforgery.client.AlloyForgeScreen;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {

    @Shadow
    @Final
    private Identifier id;

    @Inject(method = "upload", at = @At("HEAD"))
    private void captureSize(SpriteAtlasTexture.Data data, CallbackInfo ci) {
        if (!this.id.equals(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)) return;

        AlloyForgeScreen.blockAtlasWidth = ((SpriteAtlasTextureDataAccessor) data).af$getWidth();
        AlloyForgeScreen.blockAtlasHeight = ((SpriteAtlasTextureDataAccessor) data).af$getHeight();
    }

}
