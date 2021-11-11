package wraith.alloyforgery.mixin;

import me.jellysquid.mods.lithium.common.hopper.HopperHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;

@Pseudo
@Mixin(HopperHelper.class)
public class HopperHelperMixin {

    @Inject(method = "vanillaGetBlockInventory", at = @At("RETURN"), cancellable = true)
    private static void injectPassthroughInventory(World world, BlockPos pos, CallbackInfoReturnable<Inventory> cir) {
        if (cir.getReturnValue() != null) return;

        if (!(world.getBlockEntity(pos.up()) instanceof ForgeControllerBlockEntity forge)) return;
        cir.setReturnValue(forge);
    }

}
