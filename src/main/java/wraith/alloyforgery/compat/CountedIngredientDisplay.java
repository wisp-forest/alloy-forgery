package wraith.alloyforgery.compat;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.recipe.display.DisplayedItemFactory;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.util.context.ContextParameterMap;

import java.util.stream.Stream;

public record CountedIngredientDisplay(SlotDisplay ingredient, int amount) implements SlotDisplay {
    public static final StructEndec<CountedIngredientDisplay> ENDEC = StructEndecBuilder.of(
            CodecUtils.toEndecWithRegistries(SlotDisplay.CODEC, SlotDisplay.PACKET_CODEC).fieldOf("ingredient", CountedIngredientDisplay::ingredient),
            Endec.INT.fieldOf("amount", CountedIngredientDisplay::amount),
            CountedIngredientDisplay::new
    );

    public static final Serializer<CountedIngredientDisplay> SERIALIZER = new Serializer(CodecUtils.toMapCodec(ENDEC), CodecUtils.toPacketCodec(ENDEC));

    @Override
    public <T> Stream<T> appendStacks(ContextParameterMap parameters, DisplayedItemFactory<T> factory) {
        if (factory instanceof SlotDisplay.NoopDisplayedItemFactory) {
            return (Stream<T>) ingredient
                    .appendStacks(parameters, NoopDisplayedItemFactory.INSTANCE)
                    .map(t -> {
                        var stack = t.copy();

                        stack.setCount(amount);

                        return stack;
                    });
        }

        return Stream.empty();
    }

    @Override
    public Serializer<CountedIngredientDisplay> serializer() {
        return SERIALIZER;
    }
}
