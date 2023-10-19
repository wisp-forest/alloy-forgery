package wraith.alloyforgery.pond;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface RecipeTagHelper {

    default boolean isIn(Identifier tag){
        throw new UnsupportedOperationException("RecipeTagHelper 'isIn' method not implememnted!");
    }
}
