package wraith.alloyforgery.recipe;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import io.wispforest.owo.serialization.util.EndecRecipeSerializer;
import wraith.alloyforgery.utils.EndecUtils;

public class AlloyForgeRecipeSerializer extends EndecRecipeSerializer<AlloyForgeRecipe> {

    public static final StructEndec<AlloyForgeRecipe> RECIPE_ENDEC = new StructEndec<>() {
        @Override
        public void encodeStruct(Serializer.Struct struct, AlloyForgeRecipe recipe) {
            var rawData = recipe.rawRecipeData.orElseThrow(() -> new IllegalStateException("Unable to serialize Recipe due to not having the required RawRecipeData!"));

            RawAlloyForgeRecipe.ENDEC.encodeStruct(struct, rawData);
        }

        @Override
        public AlloyForgeRecipe decodeStruct(Deserializer.Struct struct) {
            var rawData = RawAlloyForgeRecipe.ENDEC.decodeStruct(struct);

            return rawData.generateRecipe();
        }
    };

    public static final Endec<AlloyForgeRecipe> ENDEC = Endec.ifAttr(SerializationAttribute.HUMAN_READABLE, RECIPE_ENDEC)
            .orElse(
                    StructEndecBuilder.of(
                            Endec.map(EndecUtils.INGREDIENT, Endec.INT).fieldOf("inputs", AlloyForgeRecipe::getIngredientsMap),
                            BuiltInEndecs.ITEM_STACK.fieldOf("result", AlloyForgeRecipe::getBaseResult),
                            Endec.INT.fieldOf("min_forge_tier", AlloyForgeRecipe::getMinForgeTier),
                            Endec.INT.fieldOf("fuel_per_tick", AlloyForgeRecipe::getFuelPerTick),
                            Endec.map(AlloyForgeRecipe.OverrideRange.OVERRIDE_RANGE, BuiltInEndecs.ITEM_STACK).fieldOf("overrides", AlloyForgeRecipe::getTierOverrides),
                            BuiltInEndecs.IDENTIFIER.optionalOf().fieldOf("secondary_id", AlloyForgeRecipe::secondaryID),
                            AlloyForgeRecipe::new
                    )
            );

    public static final AlloyForgeRecipeSerializer INSTANCE = new AlloyForgeRecipeSerializer(RECIPE_ENDEC, ENDEC);

    protected AlloyForgeRecipeSerializer(StructEndec<AlloyForgeRecipe> endec, Endec<AlloyForgeRecipe> networkEndec) {
        super(endec, networkEndec);
    }
}
