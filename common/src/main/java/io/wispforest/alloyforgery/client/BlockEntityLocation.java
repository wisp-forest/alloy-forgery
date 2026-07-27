package io.wispforest.alloyforgery.client;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record BlockEntityLocation(BlockPos blockPos, ResourceKey<Level> worldKey) {
    public static final StructEndec<BlockEntityLocation> ENDEC = StructEndecBuilder.of(
        MinecraftEndecs.BLOCK_POS.fieldOf("blockPos", BlockEntityLocation::blockPos),
        MinecraftEndecs.IDENTIFIER.xmap(id -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::identifier).fieldOf("dimensionType", BlockEntityLocation::worldKey),
        BlockEntityLocation::new
    );

    public static BlockEntityLocation of(BlockEntity blockEntity) {
        var world = blockEntity.getLevel();

        if (world == null) {
            throw new IllegalStateException("Unable to create the needed BlockEntityLocation fro the given BlockEntity due to it missing its World!");
        }

        return new BlockEntityLocation(blockEntity.getBlockPos(), world.dimension());
    }

    public <T extends BlockEntity> T get(Player player, BlockEntityType<T> blockEntityType) {
        return get(player.level(), blockEntityType);
    }

    public <T extends BlockEntity> T get(Level world, BlockEntityType<T> blockEntityType) {
        if (!world.dimension().equals(worldKey())) {
            var server = world.getServer();

            if (server == null) {
                throw new IllegalStateException("Unable to get the given block entity due to a inability to get the needed server instance!");
            }

            world = server.getLevel(this.worldKey());
        }

        if (world == null) {
            throw new IllegalStateException("Unable to get the given block entity due to a inability to get the needed origin world! [World: " + this.worldKey().identifier() + "]");
        }

        return world.getBlockEntity(this.blockPos(), blockEntityType)
            .orElseThrow(() -> new IllegalStateException("Unable to get the given block entity due not finding any block entity at the given location! [World: " + this.worldKey().identifier() + ", Pos: " + this.blockPos() + "]"));
    }
}
