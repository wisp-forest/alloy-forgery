package wraith.alloy_forgery.registry;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.api.Forges;
import wraith.alloy_forgery.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ItemRegistry {

    public static final HashMap<String, Item> ITEMS = new HashMap<>();

    public static void loadItems() {
        for (String forge : Forges.getForgeNames()) {
            ITEMS.put(forge, new BlockItem(BlockRegistry.BLOCKS.get(forge), new Item.Settings().group(ItemGroup.DECORATIONS)));
        }
    }

    public static void registerItems() {
        ArrayList<String> IDs = new ArrayList<>(ITEMS.keySet());
        Collections.sort(IDs);
        for (String id : IDs) {
            Registry.register(Registry.ITEM, Utils.ID(id), ITEMS.get(id));
        }
    }

}
