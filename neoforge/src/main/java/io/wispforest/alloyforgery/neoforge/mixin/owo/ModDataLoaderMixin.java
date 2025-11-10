package io.wispforest.alloyforgery.neoforge.mixin.owo;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.neoforge.utils.CursedLoaderHacks;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.moddata.ModDataLoader;
import net.minecraft.util.Identifier;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(value = ModDataLoader.class, remap = false)
public abstract class ModDataLoaderMixin {
    @WrapOperation(method = "lambda$load$0", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforgespi/locating/IModFile;getFilePath()Ljava/nio/file/Path;"))
    private static Path adjustReturnedPath(IModFile instance, Operation<Path> original, @Local(argsOnly = true) IModInfo container) {
        return CursedLoaderHacks.getPath(container);
    }

    @WrapOperation(method = "lambda$load$0", at = @At(value = "INVOKE", target = "Lio/wispforest/owo/moddata/ModDataLoader;tryLoadFilesFrom(Ljava/util/Map;Ljava/lang/String;Ljava/nio/file/Path;)V"))
    private static void attemptOtherModIds(Map<Identifier, JsonObject> foundFiles, String namespace, Path targetPath, Operation<Void> original, @Local(argsOnly = true) ModDataConsumer consumer, @Local(argsOnly = true) IModInfo container) {
        original.call(foundFiles, namespace, targetPath);

        try {
            var otherIds = new ArrayList<>(container.getConfig().<List<String>>getConfigElement("provides").orElse(List.of()));

            var rootPath = CursedLoaderHacks.getPath(container);

            for (var otherId : otherIds) {
                targetPath = rootPath.resolve(String.format("data/%s/%s", otherId, consumer.getDataSubdirectory()));

                original.call(foundFiles, otherId, targetPath);
            }
        } catch (Exception e) {
            AlloyForgery.LOGGER.warn("Unable to get additional provided entry due to an exception: ", e);
        }
    }
}
