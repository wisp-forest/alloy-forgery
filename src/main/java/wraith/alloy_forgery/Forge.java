package wraith.alloy_forgery;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Forge {

    public static final HashMap<String, Forge> FORGES = new HashMap<>();
    public static final HashMap<String, HashMap<String, Integer>> MATERIAL_WORTH = new HashMap<>();
    public static final HashMap<HashMap<String, Integer>, Pair<String, Integer>> FORGE_RECIPES = new HashMap<>();

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

    public static void readSmeltriesFromJson(JsonObject json) {
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

    public static void readMaterialsFromJson(JsonObject json) {
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

    public static void readRecipesFromJson(JsonObject json) {
        JsonArray recipes = json.getAsJsonArray("recipes");
        for (JsonElement recipe : recipes) {
            JsonObject inputs = recipe.getAsJsonObject().get("input").getAsJsonObject();
            JsonObject output = recipe.getAsJsonObject().get("output").getAsJsonObject();
            HashMap<String, Integer> inputRecipes = new HashMap<>();
            for (Map.Entry<String, JsonElement> input : inputs.entrySet()) {
                inputRecipes.put(input.getKey(), input.getValue().getAsInt());
            }
            FORGE_RECIPES.put(inputRecipes, new Pair<>(output.get("item").getAsString(), output.get("amount").getAsInt()));
        }
    }
}
