package wraith.alloyforgery.utils;

import com.mojang.serialization.MapCodec;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructField;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.recipe.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EndecUtils {
    public static StructEndec<Ingredient> STRUCT_INGREDIENT = new MapCodecStructEndec<>(MapCodec.assumeMapUnsafe(Ingredient.CODEC));
    public static Endec<Ingredient> INGREDIENT = CodecUtils.toEndec(Ingredient.CODEC);
    public static Endec<FluidVariant> FLUID_VARIANT = CodecUtils.toEndec(FluidVariant.CODEC).catchErrors((ctx, deserializer, e) -> FluidVariant.blank());

    public static <S, T> StructField<S, T> optionalFieldOf(String name, Endec<T> endec, Function<S, T> getter, Supplier<@Nullable T> defaultValue) {
        return optionalFieldOf(name, endec, getter, defaultValue, t -> Objects.equals(t, defaultValue.get()));
    }

    public static <S, T> StructField<S, T> optionalFieldOf(String name, Endec<T> endec, Function<S, T> getter, Supplier<@Nullable T> defaultValue, Predicate<T> isEmpty) {
        Objects.requireNonNull(defaultValue, "Optional default value is null for field: " + name);

        return new StructField<>(
            name,
            endec.optionalOf()
                .xmap(
                    optional -> optional.orElseGet(defaultValue),
                    t -> {
                        if (isEmpty.test(t)) return Optional.empty();

                        return Optional.ofNullable(t);
                    }
                ),
            getter,
            defaultValue
        );
    }

}
