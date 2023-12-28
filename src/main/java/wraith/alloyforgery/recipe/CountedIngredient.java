package wraith.alloyforgery.recipe;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.format.json.JsonDeserializer;
import io.wispforest.owo.serialization.format.json.JsonEndec;
import io.wispforest.owo.serialization.format.json.JsonSerializer;
import net.minecraft.recipe.Ingredient;
import wraith.alloyforgery.utils.EndecUtils;

public record CountedIngredient(Ingredient ingredient, int count) {
    public static Endec<CountedIngredient> ENDEC = JsonEndec.INSTANCE.xmap(element -> {
        var object = element.getAsJsonObject();

        return new CountedIngredient(
                EndecUtils.INGREDIENT.decode(new JsonDeserializer(element)),
                object.keySet().contains("count") ? object.get("count").getAsInt() : 1
        );
    }, countedIngredient -> {
        var element = EndecUtils.INGREDIENT.encodeFully(JsonSerializer::of, countedIngredient.ingredient());

        var count = countedIngredient.count();

        if (count > 1) element.getAsJsonObject().addProperty("count", count);

        return element;
    });
}
