package io.wispforest.alloyforgery.neoforge.mixin.neoforge;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockCapability.class)
public abstract class BlockCapabilityMixin<T, C extends @Nullable Object> {

    // TODO: MAYBE CONVINCE NEOFORGE TO HAVE FALLBACK OR FIND ALTERNATIVE FOR SUCH
    @ModifyReturnValue(method = "getCapability", at = @At("TAIL"))
    private T attemptFallbackForAlloyForge(@Nullable T original, @Local(argsOnly = true) Level world, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) C context) {
        if (original == null && ((BlockCapability) (Object) this) == Capabilities.Item.BLOCK) {
            if (context == Direction.DOWN && world.getBlockEntity(pos.above()) instanceof ForgeControllerBlockEntity froge){
                return (T) new WorldlyContainerWrapper(froge, Direction.DOWN);
            }
        }

        return null;
    }
}
