package io.wispforest.alloyforgery.fabric.mixin;

import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.client.BlockEntityLocation;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ForgeControllerBlockEntity.class)
public abstract class ForgeControllerBlockEntityMixin implements ExtendedMenuProvider<BlockEntityLocation> {
    @Shadow(remap = false)
    public abstract BlockEntityLocation getExtraScreenData(ServerPlayer player);

    @Override
    public BlockEntityLocation getScreenOpeningData(ServerPlayer player) {
        return this.getExtraScreenData(player);
    }
}
