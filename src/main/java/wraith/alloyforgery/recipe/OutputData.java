package wraith.alloyforgery.recipe;

import com.mojang.logging.LogUtils;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.List;

public record OutputData(Integer count, ComponentChanges components, @Nullable Item outputItem, @Nullable List<Identifier> items, @Nullable TagKey<Item> defaultTag) {

    public OutputData(Integer count, @Nullable Item outputItem, @Nullable List<Identifier> items, @Nullable TagKey<Item> defaultTag) {
        this(count, ComponentChanges.EMPTY, outputItem, items, defaultTag);
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    @Deprecated(forRemoval = true)
    private static final Endec<OutputData> OLD_FORMAT_ENDEC = StructEndecBuilder.of(
        Endec.INT.fieldOf("count", OutputData::count),
        MinecraftEndecs.ofRegistry(Registries.ITEM).optionalFieldOf("id", OutputData::outputItem, () -> null),
        MinecraftEndecs.IDENTIFIER.listOf().optionalFieldOf("priority", OutputData::items, () -> null),
        MinecraftEndecs.unprefixedTagKey(RegistryKeys.ITEM).optionalFieldOf("default", OutputData::defaultTag, () -> null),
        OutputData::new
    );

    private static final Endec<OutputData> NEW_FORMAT_ENDEC = StructEndecBuilder.of(
        Endec.INT.fieldOf("count", OutputData::count),
        CodecUtils.toEndec(ComponentChanges.CODEC).optionalFieldOf("components", OutputData::components, ComponentChanges.EMPTY),
        MinecraftEndecs.ofRegistry(Registries.ITEM).optionalFieldOf("item", OutputData::outputItem, () -> null),
        MinecraftEndecs.IDENTIFIER.listOf().optionalFieldOf("priority", OutputData::items, () -> null),
        MinecraftEndecs.unprefixedTagKey(RegistryKeys.ITEM).optionalFieldOf("tag", OutputData::defaultTag, () -> null),
        OutputData::new
    );

    public static final Endec<OutputData> ENDEC = NEW_FORMAT_ENDEC.catchErrors((ctx, deserializer, e) -> {
        if (!(e instanceof InvalidOutputDataException)) throw new RuntimeException(e);

        var data = OLD_FORMAT_ENDEC.decode(ctx, deserializer);

        //LOGGER.warn("Deprecated Alloy Forgery Recipe keys were used when decoding the recipe! Please change all 'id' -> 'item' and 'default' -> 'tag'. ");

        return data;
    });

    public OutputData {
        if (items != null && defaultTag == null) {
            throw new InvalidOutputDataException("Priority-based recipes must declare a 'default' tag");
        } else if (outputItem == null && defaultTag == null) {
            throw new InvalidOutputDataException("No output for the given recipe was found!");
        }
    }

    public boolean prioritisedOutput() {
        return items != null;
    }

    public static class InvalidOutputDataException extends IllegalArgumentException {
        public InvalidOutputDataException(String s) {
            super(s);
        }
    }
}
