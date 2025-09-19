package wraith.alloyforgery.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import io.wispforest.owo.Owo;
import io.wispforest.owo.util.StackTraceSupplier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

@Mixin(value = DataResult.class, remap = false)
public interface DataResultMixin {
    @Mixin(value = DataResult.Error.class, remap = false)
    abstract class DataResultErrorMixin<R> {

        @Shadow
        @Final
        private Supplier<String> messageSupplier;

        @WrapOperation(method ={
                "toString"
        }, at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DataResult$Error;message()Ljava/lang/String;"))
        private <T> String printStackTrace(DataResult.Error instance, Operation<String> original) {
            var str = original.call(instance);

            if (Owo.DEBUG && this.messageSupplier instanceof StackTraceSupplier supplier) {
                StringWriter sw = new StringWriter();

                supplier.throwable().printStackTrace(new PrintWriter(sw));

                var extraErrorData = sw.toString();

                return "\n[oωo]: An error has occurred within DFU = " + extraErrorData;
            }

            return str;
        }
    }
}
