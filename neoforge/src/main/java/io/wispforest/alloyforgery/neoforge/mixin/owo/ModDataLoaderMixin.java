package io.wispforest.alloyforgery.neoforge.mixin.owo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.wispforest.owo.Owo;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.moddata.ModDataLoader;
import net.minecraft.util.Identifier;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ModDataLoader.class)
public class ModDataLoaderMixin {
    @Shadow
    @Final
    private static Gson GSON;

    @WrapMethod(method = "lambda$load$0")
    private static void adjustOutput(ModDataConsumer consumer, Map<Identifier, JsonObject> foundFiles, IModInfo container, Operation<Void> original) {
        var modids = new ArrayList<>(container.getConfig().<List<String>>getConfigElement("provides").orElse(List.of()));

        modids.add(container.getModId());

        for (var currentModid : modids) {
            var relativePath = String.format("data/%s/%s", currentModid, consumer.getDataSubdirectory());

            tryLoadJarContentsFrom(GSON, foundFiles, currentModid, container.getOwningFile().getFile().getContents(), relativePath);
        }
    }

    @Unique
    private static void tryLoadJarContentsFrom(Gson GSON, Map<Identifier, JsonObject> foundFiles, String namespace, JarContents jarContents, String relativePath) {
        try {
            jarContents.visitContent(relativePath, (path, resource) -> {
                if (!path.endsWith(".json")) return;

                try {
                    try (final var reader = resource.bufferedReader()) {
                        var idPath = path.replace(relativePath + "/",  "");

                        foundFiles.put(Identifier.of(namespace, FilenameUtils.removeExtension(idPath)), GSON.fromJson(reader, JsonObject.class));
                    }
                } catch (IOException e) {
                    Owo.LOGGER.warn("### Unable to open data file {} ++ Stacktrace below ###", path, e);
                }
            });
        } catch (Exception e) {
            Owo.LOGGER.error("### Unable to traverse data tree {}:{} ++ Stacktrace below ###", namespace, relativePath, e);
        }
    }
}
