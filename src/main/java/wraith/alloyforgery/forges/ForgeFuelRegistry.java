package wraith.alloyforgery.forges;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.util.JsonHelper;
import java.util.HashMap;
import java.util.Map;

public class ForgeFuelRegistry {

    private static final Map<Item, ForgeFuelDefinition> REGISTRY = new HashMap<>();

    public static void clear() {
        REGISTRY.clear();
    }

    public static ForgeFuelDefinition getFuelForItem(Item item) {
        return REGISTRY.getOrDefault(item, ForgeFuelDefinition.EMPTY);
    }

    public static boolean hasFuel(Item item) {
        return REGISTRY.containsKey(item);
    }

    public static void register(Item item, ForgeFuelDefinition fuel) {
        REGISTRY.put(item, fuel);
    }

    public record ForgeFuelDefinition(int fuel, Item returnType) {

        public static final ForgeFuelDefinition EMPTY = new ForgeFuelDefinition(0, null);

        public boolean hasReturnType() {
            return returnType != null;
        }

        public static ForgeFuelDefinition fromJson(JsonObject json) {
            final int fuel = json.get("fuel").getAsInt();
            final Item returnType = JsonHelper.getItem(json, "return_item", null);

            return new ForgeFuelDefinition(fuel, returnType);
        }

    }

}
