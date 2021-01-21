package wraith.alloy_forgery.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.utils.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MaterialWorths {

    private static final HashMap<String, HashMap<String, Integer>> MATERIAL_WORTH = new HashMap<>();

    public static HashMap<String, Integer> getMaterialWorthMap(String material) {
        return MATERIAL_WORTH.getOrDefault(material, new HashMap<>());
    }
    public static void addMaterials(HashMap<String, HashMap<String, Integer>> materials, boolean replace) {
        if (replace) {
            MATERIAL_WORTH.putAll(materials);
        } else {
            for (Map.Entry<String, HashMap<String, Integer>> materialEntry : materials.entrySet()) {
                if (!MATERIAL_WORTH.containsKey(materialEntry.getKey())) {
                    MATERIAL_WORTH.put(materialEntry.getKey(), new HashMap<>());
                }
                for (Map.Entry<String, Integer> itemEntry : materialEntry.getValue().entrySet()) {
                    if (!MATERIAL_WORTH.get(materialEntry.getKey()).containsKey(itemEntry.getKey())) {
                        MATERIAL_WORTH.get(materialEntry.getKey()).put(itemEntry.getKey(), itemEntry.getValue());
                    }
                }
            }
        }
        saveConfig(false);
    }
    public static void addMaterial(String material, HashMap<String, Integer> worths, boolean replace) {
        if (replace) {
            MATERIAL_WORTH.put(material, worths);
        } else {
            if (!MATERIAL_WORTH.containsKey(material)) {
                MATERIAL_WORTH.put(material, new HashMap<>());
            }
            for (Map.Entry<String, Integer> itemEntry : worths.entrySet()) {
                if (!MATERIAL_WORTH.get(material).containsKey(itemEntry.getKey())) {
                    MATERIAL_WORTH.get(material).put(itemEntry.getKey(), itemEntry.getValue());
                }
            }
        }
        saveConfig(false);
    }
    public static void addMaterialWorthFromId(String material, String item, int worth, boolean replace) {
        if (hasMaterial(material) && (!MATERIAL_WORTH.get(material).containsKey(item) || replace)) {
            MATERIAL_WORTH.get(material).put(item, worth);
            saveConfig(false);
        }
    }
    public static Set<Map.Entry<String, Integer>> getMaterialWorthMapEntries(String material) {
        return getMaterialWorthMap(material).entrySet();
    }
    public static Set<Map.Entry<String, HashMap<String, Integer>>> getEntries() {
        return MATERIAL_WORTH.entrySet();
    }

    public static boolean hasMaterial(String material) {
        return MATERIAL_WORTH.containsKey(material);
    }
    public static boolean materialHasItem(String material, String itemId) {
        HashMap<String, Integer> map = MATERIAL_WORTH.getOrDefault(material, new HashMap<>());
        if (map.containsKey(itemId)) {
            return true;
        }
        if (itemId.startsWith("#")) {
            return false;
        }
        Item item = Registry.ITEM.get(new Identifier(itemId));
        if (item == Items.AIR) {
            return false;
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getKey().startsWith("#") && TagRegistry.item(new Identifier(entry.getKey().substring(1))).contains(item)) {
                return true;
            }
        }
        return false;
    }
    public static int getMaterialWorthFromId(String material, String id) {
        HashMap<String, Integer> map = MATERIAL_WORTH.getOrDefault(material, new HashMap<>());
        if (map.containsKey(id)) {
            return map.get(id);
        }
        if (id.startsWith("#")) {
            return 0;
        }
        Item item = Registry.ITEM.get(new Identifier(id));
        if (item == Items.AIR) {
            return 0;
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getKey().startsWith("#") && TagRegistry.item(new Identifier(entry.getKey().substring(1))).contains(item)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public static void readFromJson(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String material = entry.getKey();
            JsonObject itemEntries = entry.getValue().getAsJsonObject();
            HashMap<String, Integer> worth = new HashMap<>();
            for (Map.Entry<String, JsonElement> itemEntry : itemEntries.entrySet()) {
                worth.put(itemEntry.getKey(), itemEntry.getValue().getAsInt());
            }
            MATERIAL_WORTH.put(material, worth);
        }
    }

    public static void saveConfig(boolean overwrite) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, HashMap<String, Integer>> material : MATERIAL_WORTH.entrySet()) {
            JsonObject materialJson = new JsonObject();
            for (Map.Entry<String, Integer> materialItem : material.getValue().entrySet()) {
                materialJson.addProperty(materialItem.getKey(), materialItem.getValue());
            }
            json.add(material.getKey(), materialJson);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Config.createFile("config/alloy_forgery/material_worth.json", gson.toJson(json), overwrite);
    }

}
