package io.wispforest.alloyforgery.compat;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay.Type;

import java.util.stream.Stream;

public record CountedIngredientDisplay(SlotDisplay ingredient, int amount) implements SlotDisplay {
    public static final StructEndec<CountedIngredientDisplay> ENDEC = StructEndecBuilder.of(
            CodecUtils.toEndecWithRegistries(SlotDisplay.CODEC, SlotDisplay.STREAM_CODEC).fieldOf("ingredient", CountedIngredientDisplay::ingredient),
            Endec.INT.fieldOf("amount", CountedIngredientDisplay::amount),
            CountedIngredientDisplay::new
    );

    public static final Type<CountedIngredientDisplay> SERIALIZER = new Type(CodecUtils.toMapCodec(ENDEC), CodecUtils.toPacketCodec(ENDEC));

    @Override
    public <T> Stream<T> resolve(ContextMap parameters, DisplayContentsFactory<T> factory) {
        if (factory instanceof SlotDisplay.ItemStackContentsFactory) {
            return (Stream<T>) ingredient
                    .resolve(parameters, ItemStackContentsFactory.INSTANCE)
                    .map(t -> {
                        var stack = t.copy();

                        stack.setCount(amount);

                        return stack;
                    });
        }

        return Stream.empty();
    }

    @Override
    public Type<CountedIngredientDisplay> type() {
        return SERIALIZER;
    }
}
