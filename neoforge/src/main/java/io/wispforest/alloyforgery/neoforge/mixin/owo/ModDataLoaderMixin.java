package io.wispforest.alloyforgery.neoforge.mixin.owo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.alloyforgery.neoforge.utils.CursedLoaderHacks;
import io.wispforest.owo.moddata.ModDataLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;

@Mixin(value = ModDataLoader.class, remap = false)
public abstract class ModDataLoaderMixin {
    @WrapOperation(method = "lambda$load$0", at = @At(value = "INVOKE", target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;"))
    private static Path adjustReturnedPath(Path instance, String other, Operation<Path> original, @Local(argsOnly = true) IModInfo container) {
        return original.call(CursedLoaderHacks.getPath(container), other);
    }
}
