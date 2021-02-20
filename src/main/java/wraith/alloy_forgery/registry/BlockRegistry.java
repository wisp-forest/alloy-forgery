package wraith.alloy_forgery.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.api.Forges;
import wraith.alloy_forgery.blocks.ForgeControllerBlock;
import wraith.alloy_forgery.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    public static final HashMap<String, Block> BLOCKS = new HashMap<>();

    public static void loadBlocks() {
        for (String forge : Forges.getForgeNames()) {
            BLOCKS.put(forge, new ForgeControllerBlock(FabricBlockSettings.of(Material.STONE).breakByTool(FabricToolTags.PICKAXES, 1).requiresTool().strength(3f, 8f).sounds(BlockSoundGroup.STONE)));
        }
    }

    public static void registerBlocks() {
        for (Map.Entry<String, Block> entry : BLOCKS.entrySet()) {
            Registry.register(Registry.BLOCK, Utils.ID(entry.getKey()), entry.getValue());
        }
    }

}
