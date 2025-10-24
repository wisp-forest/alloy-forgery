package io.wispforest.alloyforgery.fabric.mixin;

import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.client.BlockEntityLocation;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ForgeControllerBlockEntity.class)
public abstract class ForgeControllerBlockEntityMixin implements ExtendedScreenHandlerFactory<BlockEntityLocation> {
    @Shadow(remap = false)
    public abstract BlockEntityLocation getExtraScreenData(ServerPlayerEntity player);

    @Override
    public BlockEntityLocation getScreenOpeningData(ServerPlayerEntity player) {
        return this.getExtraScreenData(player);
    }
}
