package wraith.alloyforgery.mixin;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Inject(method = "getInputInventory", at = @At("RETURN"), cancellable = true)
    private static void injectPassthroughInventory(World world, Hopper hopper, CallbackInfoReturnable<Inventory> cir) {
        if(cir.getReturnValue() != null) return;

        final var pos = new BlockPos(hopper.getHopperX(), hopper.getHopperY() + 2, hopper.getHopperZ());
        if (!(world.getBlockEntity(pos) instanceof ForgeControllerBlockEntity)) return;
        cir.setReturnValue((Inventory) world.getBlockEntity(pos));
    }

}
