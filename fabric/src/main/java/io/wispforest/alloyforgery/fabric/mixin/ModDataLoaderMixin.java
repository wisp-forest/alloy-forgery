package io.wispforest.alloyforgery.fabric.mixin;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.alloyforgery.utils.LoaderPlatformUtils;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.moddata.ModDataLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.Map;

@Mixin(value = ModDataLoader.class, remap = false)
public abstract class ModDataLoaderMixin {

    // TODO: Fix issues with ModDataLoader but use this as patch
    @WrapOperation(method = "lambda$load$0", at = @At(value = "INVOKE", target = "Lio/wispforest/owo/moddata/ModDataLoader;tryLoadFilesFrom(Ljava/util/Map;Ljava/lang/String;Ljava/nio/file/Path;)V"), remap = false)
    private static void loadFromOtherLocations(Map<Identifier, JsonObject> foundFiles, String namespace, Path targetPath, Operation<Void> original, @Local(argsOnly = true) ModDataConsumer consumer, @Local(argsOnly = true) ModContainer container) {
        original.call(foundFiles, namespace, targetPath);

        if (LoaderPlatformUtils.INSTANCE.isDevelopmentEnvironment()) {
            var paths = container.getRootPaths();

            if (paths.size() <= 1) return;

            for (var path : paths) {
                var newPath = path.resolve(String.format("data/%s/%s", namespace, consumer.getDataSubdirectory()));

                if (targetPath.equals(newPath)) continue;

                original.call(foundFiles, namespace, newPath);
            }
        }
    }
}
