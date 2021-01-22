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
import wraith.alloy_forgery.MaterialWorth;
import wraith.alloy_forgery.utils.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MaterialWorths {

    private static final HashMap<String, HashMap<String, MaterialWorth>> MATERIAL_WORTH = new HashMap<>();

    public static HashMap<String, MaterialWorth> getMaterialWorthMap(String material) {
        return MATERIAL_WORTH.getOrDefault(material, new HashMap<>());
    }
    public static void addMaterials(HashMap<String, HashMap<String, MaterialWorth>> materials, boolean replace) {
        if (replace) {
            MATERIAL_WORTH.putAll(materials);
        } else {
            for (Map.Entry<String, HashMap<String, MaterialWorth>> materialEntry : materials.entrySet()) {
                if (!MATERIAL_WORTH.containsKey(materialEntry.getKey())) {
                    MATERIAL_WORTH.put(materialEntry.getKey(), new HashMap<>());
                }
                for (Map.Entry<String, MaterialWorth> itemEntry : materialEntry.getValue().entrySet()) {
                    if (!MATERIAL_WORTH.get(materialEntry.getKey()).containsKey(itemEntry.getKey())) {
                        MATERIAL_WORTH.get(materialEntry.getKey()).put(itemEntry.getKey(), itemEntry.getValue());
                    }
                }
            }
        }
        saveConfig(true);
    }
    public static void addMaterial(String material, HashMap<String, MaterialWorth> worths, boolean replace) {
        if (replace) {
            MATERIAL_WORTH.put(material, worths);
        } else {
            if (!MATERIAL_WORTH.containsKey(material)) {
                MATERIAL_WORTH.put(material, new HashMap<>());
            }
            for (Map.Entry<String, MaterialWorth> itemEntry : worths.entrySet()) {
                if (!MATERIAL_WORTH.get(material).containsKey(itemEntry.getKey())) {
                    MATERIAL_WORTH.get(material).put(itemEntry.getKey(), itemEntry.getValue());
                }
            }
        }
        saveConfig(true);
    }
    public static void addMaterialWorthFromId(String material, String item, MaterialWorth worth, boolean replace) {
        if (hasMaterial(material) && (!MATERIAL_WORTH.get(material).containsKey(item) || replace)) {
            MATERIAL_WORTH.get(material).put(item, worth);
            saveConfig(true);
        }
    }
    public static Set<Map.Entry<String, MaterialWorth>> getMaterialWorthMapEntries(String material) {
        return getMaterialWorthMap(material).entrySet();
    }
    public static Set<Map.Entry<String, HashMap<String, MaterialWorth>>> getEntries() {
        return MATERIAL_WORTH.entrySet();
    }

    public static boolean hasMaterial(String material) {
        return MATERIAL_WORTH.containsKey(material);
    }

    public static boolean materialHasItem(String material, String itemId) {
        HashMap<String, MaterialWorth> map = MATERIAL_WORTH.getOrDefault(material, new HashMap<>());
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
        return map.entrySet().stream().anyMatch(entry -> entry.getKey().startsWith("#") && TagRegistry.item(new Identifier(entry.getKey().substring(1))).contains(item));
    }
    public static MaterialWorth getMaterialWorthFromId(String material, String id) {
        HashMap<String, MaterialWorth> map = MATERIAL_WORTH.getOrDefault(material, new HashMap<>());
        if (map.containsKey(id)) {
            return map.get(id);
        }
        if (id.startsWith("#")) {
            return null;
        }
        Item item = Registry.ITEM.get(new Identifier(id));
        if (item == Items.AIR) {
            return null;
        }
        for (Map.Entry<String, MaterialWorth> entry : map.entrySet()) {
            if (entry.getKey().startsWith("#") && TagRegistry.item(new Identifier(entry.getKey().substring(1))).contains(item)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static void readFromJson(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String material = entry.getKey();
            JsonObject itemEntries = entry.getValue().getAsJsonObject();
            HashMap<String, MaterialWorth> worth = new HashMap<>();
            for (Map.Entry<String, JsonElement> itemEntry : itemEntries.entrySet()) {
                JsonObject item = itemEntry.getValue().getAsJsonObject();
                worth.put(itemEntry.getKey(), new MaterialWorth(item.get("worth").getAsInt(), item.get("can_return").getAsBoolean()));
            }
            MATERIAL_WORTH.put(material, worth);
        }
    }

    public static void saveConfig(boolean overwrite) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, HashMap<String, MaterialWorth>> material : MATERIAL_WORTH.entrySet()) {
            JsonObject materialJson = new JsonObject();
            for (Map.Entry<String, MaterialWorth> materialItem : material.getValue().entrySet()) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("worth", materialItem.getValue().worth);
                itemJson.addProperty("can_return", materialItem.getValue().canReturn);
                materialJson.add(materialItem.getKey(), itemJson);
            }
            json.add(material.getKey(), materialJson);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Config.createFile("config/alloy_forgery/material_worth.json", gson.toJson(json), overwrite);
    }

}
