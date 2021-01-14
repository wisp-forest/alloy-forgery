package wraith.alloy_forgery.registry;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ItemRegistry {

    public static final HashMap<String, Item> ITEMS = new HashMap<>();

    public static void loadItems() {
        ITEMS.put("brick_forge_controller", new BlockItem(BlockRegistry.BLOCKS.get("brick_forge_controller"), new Item.Settings().group(ItemGroup.DECORATIONS)));
        ITEMS.put("stone_brick_forge_controller", new BlockItem(BlockRegistry.BLOCKS.get("stone_brick_forge_controller"), new Item.Settings().group(ItemGroup.DECORATIONS)));
        ITEMS.put("blackstone_forge_controller", new BlockItem(BlockRegistry.BLOCKS.get("blackstone_forge_controller"), new Item.Settings().group(ItemGroup.DECORATIONS)));

    }
    public static void registerItems() {
        ArrayList<String> IDs = new ArrayList<>(ITEMS.keySet());
        Collections.sort(IDs);
        for (String id : IDs) {
            Registry.register(Registry.ITEM, Utils.ID(id), ITEMS.get(id));
        }
    }

}
