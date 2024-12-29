package wraith.alloyforgery.client;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.utils.EndecUtils;

public record BlockEntityLocation(BlockPos blockPos, RegistryKey<World> worldKey) {
    public static final StructEndec<BlockEntityLocation> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.BLOCK_POS.fieldOf("blockPos", BlockEntityLocation::blockPos),
            MinecraftEndecs.IDENTIFIER.xmap(id -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue).fieldOf("dimensionType", BlockEntityLocation::worldKey),
            BlockEntityLocation::new
    );

    public static BlockEntityLocation of(BlockEntity blockEntity) {
        var world = blockEntity.getWorld();

        if (world == null) {
            throw new IllegalStateException("Unable to create the needed BlockEntityLocation fro the given BlockEntity due to it missing its World!");
        }

        return new BlockEntityLocation(blockEntity.getPos(), world.getRegistryKey());
    }

    public <T extends BlockEntity> T get(PlayerEntity player, BlockEntityType<T> blockEntityType) {
        var originWorld = player.getWorld();

        if (!originWorld.getRegistryKey().equals(worldKey())) {
            var server = originWorld.getServer();

            if (server == null) throw new IllegalStateException("Unable to get the given block entity due to a inability to get the needed server instance!");

            originWorld = server.getWorld(this.worldKey());
        }

        if(originWorld == null) throw new IllegalStateException("Unable to get the given block entity due to a inability to get the needed origin world! [World: " + this.worldKey().getValue() + "]");

        return originWorld.getBlockEntity(this.blockPos(), blockEntityType)
                .orElseThrow(() -> new IllegalStateException("Unable to get the given block entity due not finding any block entity at the given location! [World: " + this.worldKey().getValue() + ", Pos: " + this.blockPos() + "]"));
    }
}
