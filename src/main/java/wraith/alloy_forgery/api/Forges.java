package wraith.alloy_forgery.api;

import com.google.gson.*;
import wraith.alloy_forgery.Forge;
import wraith.alloy_forgery.utils.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Forges {

    private static final HashMap<String, Forge> FORGES = new HashMap<>();

    public static Forge getForge(String id) {
        return FORGES.getOrDefault(id, null);
    }

    public static void addForge(String id, Forge forge, boolean replace) {
        if (!FORGES.containsKey(id) || replace) {
            FORGES.put(id, forge);
            saveConfig(false);
        }
    }

    public static void addForges(HashMap<String, Forge> forges, boolean replace) {
        if (replace) {
            FORGES.putAll(forges);
        } else {
            for (Map.Entry<String, Forge> forgeEntry : forges.entrySet()) {
                if (!FORGES.containsKey(forgeEntry.getKey())) {
                    FORGES.put(forgeEntry.getKey(), forgeEntry.getValue());
                }
            }
        }
        saveConfig(false);
    }

    public static boolean hasForge(String id) {
        return FORGES.containsKey(id);
    }

    public static Set<Map.Entry<String, Forge>> getForges() {
        return FORGES.entrySet();
    }

    public static Set<String> getForgeNames() {
        return FORGES.keySet();
    }

    public static void readFromJson(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String controller = entry.getKey();
            JsonObject controllerStats = entry.getValue().getAsJsonObject();
            float tier = controllerStats.get("tier").getAsFloat();
            int maxHeat = controllerStats.get("max_heat").getAsInt();
            HashSet<String> materialsSet = new HashSet<>();
            JsonArray materials = controllerStats.getAsJsonArray("materials");
            for (JsonElement material : materials) {
                materialsSet.add(material.getAsString());
            }
            HashSet<String> recipeMaterialsSet = new HashSet<>();
            JsonArray recipeMaterials = controllerStats.getAsJsonArray("recipe_materials");
            for (JsonElement material : recipeMaterials) {
                recipeMaterialsSet.add(material.getAsString());
            }
            FORGES.put(controller, new Forge(materialsSet, recipeMaterialsSet, tier, controller, maxHeat));
        }
    }
    public static void saveConfig(boolean overwrite) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Forge> smeltry : Forges.getForges()) {
            JsonObject controllerJson = new JsonObject();
            JsonArray materialArray = new JsonArray();
            for (String material : smeltry.getValue().materials) {
                materialArray.add(material);
            }
            JsonArray recipeMaterialArray = new JsonArray();
            for (String material : smeltry.getValue().recipeMaterials) {
                recipeMaterialArray.add(material);
            }
            controllerJson.add("materials", materialArray);
            controllerJson.add("recipe_materials", recipeMaterialArray);
            controllerJson.addProperty("tier", smeltry.getValue().tier);
            controllerJson.addProperty("max_heat", smeltry.getValue().maxHeat);
            json.add(smeltry.getKey(), controllerJson);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Config.createFile("config/alloy_forgery/smelteries.json", gson.toJson(json), overwrite);
    }
}
