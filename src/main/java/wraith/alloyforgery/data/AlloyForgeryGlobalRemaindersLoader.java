package wraith.alloyforgery.data;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

import java.util.HashMap;
import java.util.Map;

public class AlloyForgeryGlobalRemaindersLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public AlloyForgeryGlobalRemaindersLoader() {
        super(GSON, "forge_remainder");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        prepared.forEach((identifier, jsonElement) -> {
            try {
                if(jsonElement instanceof JsonObject jsonObject){
                    if(!jsonObject.has("remainders")){
                        throw new JsonSyntaxException("The Global Remainders file seems to be missing the needed remainders field.");
                    }

                    var remainders = new HashMap<Item, ItemStack>();

                    for (var remainderEntry : jsonObject.getAsJsonObject("remainders").entrySet()) {
                        var item = JsonHelper.asItem(new JsonPrimitive(remainderEntry.getKey()), remainderEntry.getKey());

                        if (remainderEntry.getValue().isJsonObject()) {
                            var remainderStack = ShapedRecipe.outputFromJson(remainderEntry.getValue().getAsJsonObject());
                            remainders.put(item, remainderStack);
                        } else {
                            var remainderItem = JsonHelper.asItem(remainderEntry.getValue(), "item");
                            remainders.put(item, new ItemStack(remainderItem));
                        }
                    }

                    AlloyForgeRecipe.addRemainders(remainders);
                } else {
                    throw new JsonSyntaxException("JsonElement wasn't a JsonObject meaning it is malformed");
                }
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error("[AlloyForgerRemainders]: Parsing error loading recipe {}", identifier, exception);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(AlloyForgery.MOD_ID, "forge_remainder");
    }
}
