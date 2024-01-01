package wraith.alloyforgery.recipe;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.format.edm.EdmDeserializer;
import io.wispforest.owo.serialization.format.edm.EdmElement;
import io.wispforest.owo.serialization.format.edm.EdmEndec;
import io.wispforest.owo.serialization.format.edm.EdmSerializer;
import net.minecraft.recipe.Ingredient;
import wraith.alloyforgery.utils.EndecUtils;

import java.util.Map;

public record CountedIngredient(Ingredient ingredient, int count) {
    public static Endec<CountedIngredient> ENDEC = EdmEndec.INSTANCE.xmap(element -> {
        var object = element.<Map<String, EdmElement<?>>>cast();

        return new CountedIngredient(
                EndecUtils.INGREDIENT.decode(EdmDeserializer.of(element)),
                object.containsKey("count") ? object.get("count").<Number>cast().intValue() : 1
        );
    }, countedIngredient -> {
        var element = (EdmElement<Map<String, EdmElement<?>>>) EndecUtils.INGREDIENT.encodeFully(EdmSerializer::of, countedIngredient.ingredient());

        var count = countedIngredient.count();

        if (count > 1) element.value().put("count", EdmElement.wrapInt(count));

        return element;
    });
}
