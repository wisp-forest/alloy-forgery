package wraith.alloy_forgery.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import wraith.alloy_forgery.ForgeFuel;
import wraith.alloy_forgery.utils.Config;

import java.util.HashMap;
import java.util.Map;

public class ForgeFuels {

    public static final HashMap<String, ForgeFuel> FUELS = new HashMap<>();

    public static void addFuel(String item, ForgeFuel fuel, boolean replace) {
        if (!FUELS.containsKey(item) || replace) {
            FUELS.put(item, fuel);
            saveConfig(true);
        }
    }

    public static void readFromJson(JsonObject json) {
        for (Map.Entry<String, JsonElement> fuel : json.entrySet()) {
            JsonObject fuelObject = fuel.getValue().getAsJsonObject();

            String item = fuel.getKey();
            int burnTime = fuelObject.get("burn_time").getAsInt();
            String returnable = null;
            if (fuelObject.has("returnable_item")) {
                returnable = fuelObject.get("returnable_item").getAsString();
            }
            boolean rightclickable = fuelObject.has("rightclickable") && fuelObject.get("rightclickable").getAsBoolean();
            FUELS.put(item, new ForgeFuel(item, burnTime, returnable, rightclickable));
        }
    }

    public static void saveConfig(boolean overwrite) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, ForgeFuel> fuels : FUELS.entrySet()) {
            ForgeFuel fuel = fuels.getValue();
            JsonObject fuelJson = new JsonObject();
            fuelJson.addProperty("burn_time", fuel.getCookTime());
            if (fuel.hasReturnableItem()) {
                fuelJson.addProperty("returnable_item", fuel.getReturnableItem());
            }
            fuelJson.addProperty("rightclickable", fuel.getReturnableItem());

            json.add(fuels.getKey(), fuelJson);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Config.createFile("config/alloy_forgery/fuels.json", gson.toJson(json), overwrite);
    }

}
