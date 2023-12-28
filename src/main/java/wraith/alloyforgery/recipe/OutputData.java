package wraith.alloyforgery.recipe;

import com.google.gson.JsonSyntaxException;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record OutputData(Integer count, @Nullable Item outputItem, @Nullable List<Identifier> items, @Nullable TagKey<Item> defaultTag) {
    public static Endec<OutputData> ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("count", OutputData::count),
            BuiltInEndecs.ofRegistry(Registries.ITEM).optionalFieldOf("id", OutputData::outputItem, () -> null),
            BuiltInEndecs.IDENTIFIER.listOf().optionalFieldOf("priority", OutputData::items, () -> null),
            BuiltInEndecs.unprefixedTagKey(RegistryKeys.ITEM).optionalFieldOf("default", OutputData::defaultTag, () -> null),
            OutputData::new
    );

    public OutputData {
        if (items != null && defaultTag == null) {
            throw new JsonSyntaxException("Priority-based recipes must declare a 'default' tag");
        }
    }

    public boolean prioritisedOutput() {
        return items != null;
    }
}
