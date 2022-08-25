package wraith.alloyforgery.mixin;

import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HopperBlockEntity.class)
public interface HopperBlockEntityAccessor {

    @Invoker("setTransferCooldown")
    void alloyForge$setTransferCooldown(int transferCooldown);

    @Invoker("isDisabled")
    boolean alloyForge$isDisabled();
}
