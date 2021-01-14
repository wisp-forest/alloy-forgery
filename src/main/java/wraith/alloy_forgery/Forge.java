package wraith.alloy_forgery;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Forge {

    public static final HashMap<String, Forge> FORGES = new HashMap<>();

    public HashSet<String> materials;
    public String controller;
    public float tier;

    public Forge(HashSet<String> materials, float tier, String controller) {
        this.materials = materials;
        this.tier = tier;
        this.controller = controller;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Forge forge = (Forge) o;
        return forge.tier == this.tier && forge.materials.equals(this.materials) && forge.controller.equals(this.controller);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Float.hashCode(tier);
        hash = 31 * hash + (materials == null ? 0 : materials.hashCode());
        hash = 31 * hash + (controller == null ? 0 : controller.hashCode());
        return hash;
    }

    public static void readFromJson(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String controller = entry.getKey();
            JsonObject controllerStats = entry.getValue().getAsJsonObject();
            float tier = controllerStats.get("tier").getAsFloat();
            HashSet<String> materialsSet = new HashSet<>();
            JsonArray materials = controllerStats.getAsJsonArray("materials");
            for (JsonElement material : materials) {
                materialsSet.add(material.getAsString());
            }
            FORGES.put(controller, new Forge(materialsSet, tier, controller));
        }
    }
}
