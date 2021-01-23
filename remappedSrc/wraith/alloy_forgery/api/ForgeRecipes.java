package wraith.alloy_forgery.api;

import com.google.gson.*;
import wraith.alloy_forgery.RecipeOutput;
import wraith.alloy_forgery.utils.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ForgeRecipes {

    private static final HashMap<HashMap<String, Integer>, RecipeOutput> FORGE_RECIPES = new HashMap<>();

    public static void addRecipe(HashMap<String, Integer> input, RecipeOutput output, boolean replace) {
        if (!FORGE_RECIPES.containsKey(input) || replace) {
            FORGE_RECIPES.put(input, output);
            saveConfig(true);
        }
    }
    public static boolean containsRecipe(HashMap<String, Integer> recipe) {
        return FORGE_RECIPES.containsKey(recipe);
    }

    public static RecipeOutput getOutput(HashMap<String, Integer> input) {
        return FORGE_RECIPES.getOrDefault(input, null);
    }
    public static void addRecipes(HashMap<HashMap<String, Integer>, RecipeOutput> recipes, boolean overwrite) {
        if (overwrite) {
            FORGE_RECIPES.putAll(recipes);
        } else {
            for (Map.Entry<HashMap<String, Integer>, RecipeOutput> recipeEntry : recipes.entrySet()) {
                if (!FORGE_RECIPES.containsKey(recipeEntry.getKey())) {
                    FORGE_RECIPES.put(recipeEntry.getKey(), recipeEntry.getValue());
                }
            }
        }
        saveConfig(true);
    }
    public static Set<Map.Entry<HashMap<String, Integer>, RecipeOutput>> getRecipes() {
        return FORGE_RECIPES.entrySet();
    }

    public static void readFromJson(JsonObject json) {
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

    public static void saveConfig(boolean overwrite) {
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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Config.createFile("config/alloy_forgery/recipes.json", gson.toJson(json), overwrite);
    }
}
