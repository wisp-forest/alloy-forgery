package wraith.alloyforgery.forges;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import wraith.alloyforgery.AlloyForgery;

import java.util.Map;

public class FuelDataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public FuelDataLoader() {
        super(new Gson(), "alloy_forge_fuels");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        ForgeFuelRegistry.clear();
        prepared.forEach((identifier, jsonElement) -> {
            for (var entry : jsonElement.getAsJsonObject().get("fuels").getAsJsonArray()) {
                ForgeFuelRegistry.register(JsonHelper.getItem(entry.getAsJsonObject(), "item"), ForgeFuelRegistry.ForgeFuelDefinition.fromJson(entry.getAsJsonObject()));
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return AlloyForgery.id("forge_fuel_loader");
    }
}
