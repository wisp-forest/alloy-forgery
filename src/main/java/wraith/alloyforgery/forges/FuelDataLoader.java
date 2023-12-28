package wraith.alloyforgery.forges;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import wraith.alloyforgery.AlloyForgery;
import java.util.Map;

public class FuelDataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final FuelDataLoader INSTANCE = new FuelDataLoader();

    private FuelDataLoader() {
        super(new Gson(), "alloy_forge_fuels");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        ForgeFuelRegistry.clear();

        prepared.forEach((identifier, jsonElement) -> {
            try {
                for (var entry : jsonElement.getAsJsonObject().get("fuels").getAsJsonArray()) {
                    ForgeFuelRegistry.register(JsonHelper.getItem(entry.getAsJsonObject(), "item").value(), ForgeFuelRegistry.ForgeFuelDefinition.fromJson(entry.getAsJsonObject()));
                }
            } catch (JsonSyntaxException e){
                LOGGER.error("An error has occurred during FuelDataLoader stage:", e);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return AlloyForgery.id("forge_fuel_loader");
    }
}
