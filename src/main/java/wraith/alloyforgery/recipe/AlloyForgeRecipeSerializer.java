package wraith.alloyforgery.recipe;

import io.wispforest.endec.*;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.EndecRecipeSerializer;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import wraith.alloyforgery.utils.EndecUtils;

public class AlloyForgeRecipeSerializer extends EndecRecipeSerializer<AlloyForgeRecipe> {

    public static final StructEndec<AlloyForgeRecipe> RECIPE_ENDEC = new StructEndec<>() {
        @Override
        public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, AlloyForgeRecipe recipe) {
            var rawData = recipe.rawRecipeData.orElseThrow(() -> new IllegalStateException("Unable to serialize Recipe due to not having the required RawRecipeData!"));

            RawAlloyForgeRecipe.ENDEC.encodeStruct(ctx, serializer, struct, rawData);
        }

        @Override
        public AlloyForgeRecipe decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
            var rawData = RawAlloyForgeRecipe.ENDEC.decodeStruct(ctx, deserializer, struct);

            return rawData.generateRecipe();
        }
    };

    public static final Endec<AlloyForgeRecipe> ENDEC = Endec.ifAttr(SerializationAttributes.HUMAN_READABLE, RECIPE_ENDEC)
        .orElse(
            StructEndecBuilder.of(
                Endec.map(EndecUtils.INGREDIENT, Endec.INT).fieldOf("inputs", AlloyForgeRecipe::getIngredientsMap),
                MinecraftEndecs.ITEM_STACK.fieldOf("result", AlloyForgeRecipe::getBaseResult),
                Endec.INT.fieldOf("min_forge_tier", AlloyForgeRecipe::getMinForgeTier),
                Endec.INT.fieldOf("fuel_per_tick", AlloyForgeRecipe::getFuelPerTick),
                Endec.map(AlloyForgeRecipe.OverrideRange.OVERRIDE_RANGE, MinecraftEndecs.ITEM_STACK).fieldOf("overrides", AlloyForgeRecipe::getTierOverrides),
                MinecraftEndecs.IDENTIFIER.optionalOf().fieldOf("secondary_id", AlloyForgeRecipe::secondaryID),
                AlloyForgeRecipe::new
            )
        );

    public static final AlloyForgeRecipeSerializer INSTANCE = new AlloyForgeRecipeSerializer(RECIPE_ENDEC, ENDEC);

    protected AlloyForgeRecipeSerializer(StructEndec<AlloyForgeRecipe> endec, Endec<AlloyForgeRecipe> networkEndec) {
        super(endec, networkEndec);
    }
}
