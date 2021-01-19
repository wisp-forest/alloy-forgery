package wraith.alloy_forgery;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Forge {

    public static final HashMap<String, Forge> FORGES = new HashMap<>();
    public static final HashMap<String, HashMap<String, Integer>> MATERIAL_WORTH = new HashMap<>();
    public static final HashMap<HashMap<String, Integer>, RecipeOutput> FORGE_RECIPES = new HashMap<>();

    public HashSet<String> materials;
    public String controller;
    public float tier;
    public int maxHeat;

    public Forge(HashSet<String> materials, float tier, String controller, int maxHeat) {
        this.materials = materials;
        this.tier = tier;
        this.controller = controller;
        this.maxHeat = maxHeat;
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
            int maxHeat = controllerStats.get("max_heat").getAsInt();
            HashSet<String> materialsSet = new HashSet<>();
            JsonArray materials = controllerStats.getAsJsonArray("materials");
            for (JsonElement material : materials) {
                materialsSet.add(material.getAsString());
            }
            FORGES.put(controller, new Forge(materialsSet, tier, controller, maxHeat));
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
            FORGE_RECIPES.put(inputRecipes, new RecipeOutput(output.get("item").getAsString(), output.get("amount").getAsInt(), recipe.getAsJsonObject().get("heat_per_tick").getAsInt(), recipe.getAsJsonObject().get("required_tier").getAsInt()));
        }
    }

    public static void createAllConfigs() {
        createSmeltriesConfig();
        createMaterialConfig();
        createRecipeConfig();
    }

    public static void createSmeltriesConfig() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Forge> smeltry : FORGES.entrySet()) {
            JsonObject controllerJson = new JsonObject();
            JsonArray materialArray = new JsonArray();
            for (String material : smeltry.getValue().materials) {
                materialArray.add(material);
            }
            controllerJson.add("materials", materialArray);
            controllerJson.addProperty("tier", smeltry.getValue().tier);
            controllerJson.addProperty("tier", smeltry.getValue().maxHeat);
            json.add(smeltry.getKey(), controllerJson);
        }
    }

    public static void createMaterialConfig() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, HashMap<String, Integer>> material : MATERIAL_WORTH.entrySet()) {
            JsonObject materialJson = new JsonObject();
            for (Map.Entry<String, Integer> materialItem : material.getValue().entrySet()) {
                materialJson.addProperty(materialItem.getKey(), materialItem.getValue());
            }
            json.add(material.getKey(), materialJson);
        }
    }

    public static void createRecipeConfig() {
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        for (Map.Entry<HashMap<String, Integer>, RecipeOutput> recipes : FORGE_RECIPES.entrySet()) {
            JsonObject recipe = new JsonObject();
            HashMap<String, Integer> inputs = recipes.getKey();
            JsonObject inputJson = new JsonObject();
            for (Map.Entry<String, Integer> input : inputs.entrySet()) {
                inputJson.addProperty(input.getKey(), input.getValue());
            }
            recipe.add("input", inputJson);
            JsonObject outputJson = new JsonObject();
            outputJson.addProperty("item", recipes.getValue().outputItem);
            outputJson.addProperty("amount", recipes.getValue().outputAmount);
            recipe.add("output", outputJson);
            recipe.addProperty("heat_per_tick", recipes.getValue().heatAmount);
            recipe.addProperty("required_tier", recipes.getValue().requiredTier);
            array.add(recipe);
        }
        json.add("recipes", array);
    }

}
