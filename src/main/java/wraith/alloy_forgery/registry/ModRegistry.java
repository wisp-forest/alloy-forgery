package wraith.alloy_forgery.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.blocks.UniversalForgeControllerBlock;
import wraith.alloy_forgery.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModRegistry {
    public static final HashMap<String, Block> BLOCKS = new HashMap<>();
    public static final HashMap<String, Item> ITEMS = new HashMap<>();

    public static void loadBlocks() {
        BLOCKS.put("brick_forge_controller", new UniversalForgeControllerBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES).strength(2.5f, 7f).sounds(BlockSoundGroup.STONE)));
        BLOCKS.put("blackstone_forge_controller", new UniversalForgeControllerBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES).strength(3f, 8f).sounds(BlockSoundGroup.STONE)));
    }
    public static void registerBlocks() {
        for (Map.Entry<String, Block> entry : BLOCKS.entrySet()) {
            Registry.register(Registry.BLOCK, Utils.ID(entry.getKey()), entry.getValue());
        }
    }
    public static void loadItems() {
        ITEMS.put("brick_forge_controller", new Item(new Item.Settings().group(ItemGroup.DECORATIONS)));
        ITEMS.put("blackstone_forge_controller", new Item(new Item.Settings().group(ItemGroup.DECORATIONS)));

    }
    public static void registerItems() {
        ArrayList<String> IDs = new ArrayList<>(ITEMS.keySet());
        Collections.sort(IDs);
        for (String id : IDs) {
            Registry.register(Registry.ITEM, Utils.ID(id), ITEMS.get(id));
        }
    }

}
