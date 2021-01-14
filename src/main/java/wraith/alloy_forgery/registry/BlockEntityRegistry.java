package wraith.alloy_forgery.registry;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.blocks.ForgeControllerBlock;
import wraith.alloy_forgery.blocks.ForgeControllerBlockEntity;
import wraith.alloy_forgery.utils.Utils;

import java.util.HashMap;

public class BlockEntityRegistry {

    public static HashMap<String, BlockEntityType<? extends BlockEntity>> BLOCK_ENTITIES = new HashMap<>();

    public static void loadBlockEntities() {
        BLOCK_ENTITIES.put("forge_controller", BlockEntityType.Builder.create(ForgeControllerBlockEntity::new,
            BlockRegistry.BLOCKS.get("brick_forge_controller"),
            BlockRegistry.BLOCKS.get("stone_brick_forge_controller"),
            BlockRegistry.BLOCKS.get("blackstone_forge_controller")
        ).build(null));
    }

    public static void registerBlockEntities() {
        for (String id : BLOCK_ENTITIES.keySet()) {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, Utils.ID(id), BLOCK_ENTITIES.get(id));
        }
    }

}
