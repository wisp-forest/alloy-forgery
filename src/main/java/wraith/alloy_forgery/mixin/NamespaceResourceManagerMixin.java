package wraith.alloy_forgery.mixin;

import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Mixin(NamespaceResourceManager.class)
public abstract class NamespaceResourceManagerMixin {

    @Inject(method = "getAllResources", at = @At("HEAD"), cancellable = true)
    public void getAllResources(Identifier id, CallbackInfoReturnable<List<Resource>> cir) {
        if (!id.getNamespace().equals(AlloyForgery.MOD_ID) || !id.getPath().startsWith("blockstates/") || !id.getPath().endsWith(".json")) {
            return;
        }
        List<Resource> resources = new ArrayList<>();
        String json = Utils.createBlockStateJson(id.getPath().split("/")[1].split("\\.")[0]);
        resources.add(new ResourceImpl(AlloyForgery.MOD_ID, id, Utils.stringToInputStream(json), null));
        cir.setReturnValue(resources);
        cir.cancel();
    }

    @Inject(method = "getResource(Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/Resource;", at = @At("HEAD"), cancellable = true)
    public void getResource(Identifier id, CallbackInfoReturnable<Resource> cir) {
        String[] segments = id.getPath().split("/");
        String path = segments[segments.length - 1];

        if (!id.getNamespace().equals(AlloyForgery.MOD_ID) || !path.endsWith(".png")) {
            return;
        }

        File texture = new File("config/alloy_forgery/textures/" + path);
        File metadata = new File("config/alloy_forgery/textures/" + path + ".mcmeta");
        if (texture.exists()) {
            try {
                cir.setReturnValue(new ResourceImpl(AlloyForgery.MOD_ID, id, new FileInputStream(texture), metadata.exists() ? new FileInputStream(metadata) : null));
                cir.cancel();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
