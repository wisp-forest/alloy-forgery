package wraith.alloyforgery.recipe;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public record OutputData(Integer count, @Nullable Item outputItem, @Nullable List<Identifier> items, @Nullable TagKey<Item> defaultTag) {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Endec<OutputData> OLD_FORMAT_ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("count", OutputData::count),
            BuiltInEndecs.ofRegistry(Registries.ITEM).optionalFieldOf("id", OutputData::outputItem, () -> null),
            BuiltInEndecs.IDENTIFIER.listOf().optionalFieldOf("priority", OutputData::items, () -> null),
            BuiltInEndecs.unprefixedTagKey(RegistryKeys.ITEM).optionalFieldOf("default", OutputData::defaultTag, () -> null),
            OutputData::new
    );

    private static final Endec<OutputData> NEW_FORMAT_ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("count", OutputData::count),
            BuiltInEndecs.ofRegistry(Registries.ITEM).optionalFieldOf("item", OutputData::outputItem, () -> null),
            BuiltInEndecs.IDENTIFIER.listOf().optionalFieldOf("priority", OutputData::items, () -> null),
            BuiltInEndecs.unprefixedTagKey(RegistryKeys.ITEM).optionalFieldOf("tag", OutputData::defaultTag, () -> null),
            OutputData::new
    );

    public static final Endec<OutputData> ENDEC = NEW_FORMAT_ENDEC.catchErrors((deserializer, e) -> {
        if(!(e instanceof InvalidOutputDataException)) throw new RuntimeException(e);

        var data = OLD_FORMAT_ENDEC.decode(deserializer);

        //LOGGER.warn("Deprecated Alloy Forgery Recipe keys were used when decoding the recipe! Please change all 'id' -> 'item' and 'default' -> 'tag'. ");

        return data;
    });

    public OutputData {
        if (items != null && defaultTag == null) {
            throw new InvalidOutputDataException("Priority-based recipes must declare a 'default' tag");
        } else if(outputItem == null && defaultTag == null){
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
