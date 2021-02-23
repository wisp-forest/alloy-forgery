package wraith.alloy_forgery.registry;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.Forge;
import wraith.alloy_forgery.api.Forges;
import wraith.alloy_forgery.blocks.ForgeControllerBlockEntity;
import wraith.alloy_forgery.utils.Utils;

import java.util.Map;
import java.util.Set;

public class BlockEntityRegistry {

    public static BlockEntityType<ForgeControllerBlockEntity> FORGE_CONTROLLER;

    public static void loadBlockEntities() {
        Set<Map.Entry<String, Forge>> forgeBlocks = Forges.getForges();
        int i = 0;
        Block[] blocks = new Block[forgeBlocks.size()];
        for (Map.Entry<String, Forge> eachForge : forgeBlocks) {
            blocks [i++] = BlockRegistry.BLOCKS.get(eachForge.getKey());

        }
            FORGE_CONTROLLER = BlockEntityType.Builder.create(ForgeControllerBlockEntity::new,
                   blocks
            ).build(null);
    }

    public static void registerBlockEntities() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Utils.ID("forge_controller"), FORGE_CONTROLLER);
    }

}
