package io.wispforest.alloyforgery.utils;

import com.mojang.serialization.MapCodec;
import io.wispforest.endec.*;
import io.wispforest.endec.format.edm.EdmElement;
import io.wispforest.endec.format.edm.EdmEndec;
import io.wispforest.owo.serialization.format.edm.EdmOps;
import java.util.Map;

// TODO: migrate this to CodecUtils.toStructEndec when a new owo-lib version is released.
public record MapCodecStructEndec<T>(MapCodec<T> codec) implements StructEndec<T> {
    @Override
    public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, T value) {
        var ops = EdmOps.withContext(ctx);

        var builder = ops.mapBuilder();

        builder = codec.encode(value, ops, builder);

        EdmElement<?> map = builder.build(ops.empty()).getOrThrow(IllegalStateException::new);

        if (serializer instanceof SelfDescribedSerializer<?>) {
            for (var entry : map.<Map<String, EdmElement<?>>>cast().entrySet()) {
                struct.field(entry.getKey(), ctx, EdmEndec.INSTANCE, entry.getValue());
            }
        } else {
            struct.field("value", ctx, EdmEndec.INSTANCE, map);
        }
    }

    @Override
    public T decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
        EdmOps ops = EdmOps.withContext(ctx);
        EdmElement<?> value;

        if (deserializer instanceof SelfDescribedDeserializer<?>) {
            value = EdmEndec.INSTANCE.decode(ctx, deserializer);
        } else {
            value = struct.field("value", ctx, EdmEndec.INSTANCE, null);
        }

        return codec.decode(ops, ops.getMap(value).getOrThrow(IllegalStateException::new))
            .getOrThrow(IllegalStateException::new);
    }
}
