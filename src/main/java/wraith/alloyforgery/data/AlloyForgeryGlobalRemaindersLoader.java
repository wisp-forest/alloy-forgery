package wraith.alloyforgery.data;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.format.json.JsonDeserializer;
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

    private static final Endec<ItemStack> RECIPE_RESULT_ENDEC = Endec.ofCodec(ItemStack.RECIPE_RESULT_CODEC);

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public AlloyForgeryGlobalRemaindersLoader() {
        super(GSON, "forge_remainder");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        prepared.forEach((identifier, jsonElement) -> {
            try {
                if (jsonElement instanceof JsonObject jsonObject) {
                    var remainders = new HashMap<Item, ItemStack>();

                    for (var remainderEntry : JsonHelper.getObject(jsonObject, "remainders").entrySet()) {
                        var item = JsonHelper.asItem(new JsonPrimitive(remainderEntry.getKey()), remainderEntry.getKey()).value();

                        if (remainderEntry.getValue().isJsonObject()) {
                            var remainderStack = RECIPE_RESULT_ENDEC.decodeFully(JsonDeserializer::of, remainderEntry.getValue().getAsJsonObject());
                            remainders.put(item, remainderStack);
                        } else {
                            var remainderItem = JsonHelper.asItem(remainderEntry.getValue(), "item").value();
                            remainders.put(item, new ItemStack(remainderItem));
                        }
                    }

                    AlloyForgeRecipe.addRemainders(remainders);
                } else {
                    throw new JsonSyntaxException("Expected alloy forge remainders definition to be a json object");
                }
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error("[AlloyForgeRemainders]: Parsing error loading recipe {}", identifier, exception);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(AlloyForgery.MOD_ID, "forge_remainder");
    }
}
