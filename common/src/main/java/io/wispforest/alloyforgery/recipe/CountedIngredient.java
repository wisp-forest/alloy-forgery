package io.wispforest.alloyforgery.recipe;


import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.world.item.crafting.Ingredient;

public record CountedIngredient(Ingredient ingredient, int count) {
    public static final Endec<CountedIngredient> ENDEC = StructEndecBuilder.of(
        CodecUtils.toEndec(Ingredient.CODEC).fieldOf("ingredient", CountedIngredient::ingredient),
        Endec.INT.optionalFieldOf("count", CountedIngredient::count, 1),
        CountedIngredient::new
    );
}
