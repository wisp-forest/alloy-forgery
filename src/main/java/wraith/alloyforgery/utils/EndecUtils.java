package wraith.alloyforgery.utils;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.format.json.JsonDeserializer;
import io.wispforest.owo.serialization.format.json.JsonEndec;
import io.wispforest.owo.serialization.format.json.JsonSerializer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Pair;

public class EndecUtils {

    public static Endec<Ingredient> INGREDIENT = Endec.ofCodec(Ingredient.ALLOW_EMPTY_CODEC);


}
