package io.wispforest.alloyforgery.utils.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.resources.ResourceLocation;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record EndecedHandler<T>(Endec<T> endec, BiConsumer<ResourceLocation, T> handler) {
    public void handle(ResourceLocation id, JsonElement jsonElement) {
        try {
            handler().accept(id, endec().decodeFully(GsonDeserializer::of, jsonElement));
        } catch (JsonSyntaxException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonSyntaxException(e);
        }
    }

    public static <T> EndecedHandler<Optional<T>> of(String fieldName, Endec<T> endec, BiConsumer<ResourceLocation, T> consumer, Consumer<ResourceLocation> onEmptyMsg) {
        return new EndecedHandler<>(
            StructEndecBuilder.of(endec.fieldOf(fieldName, Optional::get), Optional::of),
            (entryId, t) -> t.ifPresentOrElse(t1 -> consumer.accept(entryId, t1), () -> onEmptyMsg.accept(entryId))
        );
    }
}
