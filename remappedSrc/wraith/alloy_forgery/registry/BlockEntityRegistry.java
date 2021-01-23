package wraith.alloy_forgery.registry;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.blocks.ForgeControllerBlockEntity;
import wraith.alloy_forgery.utils.Utils;

public class BlockEntityRegistry {

    public static BlockEntityType<ForgeControllerBlockEntity> FORGE_CONTROLLER;

    public static void loadBlockEntities() {
        FORGE_CONTROLLER = BlockEntityType.Builder.create(ForgeControllerBlockEntity::new,
            BlockRegistry.BLOCKS.get("brick_forge_controller"),
            BlockRegistry.BLOCKS.get("stone_brick_forge_controller"),
            BlockRegistry.BLOCKS.get("blackstone_forge_controller")
        ).build(null);
    }

    public static void registerBlockEntities() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Utils.ID("forge_controller"), FORGE_CONTROLLER);
    }

}
