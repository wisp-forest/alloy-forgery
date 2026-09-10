package io.wispforest.alloyforgery.recipe;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import io.wispforest.alloyforgery.utils.EndecUtils;

import java.util.List;

public record OutputData(Integer count, DataComponentPatch components, @Nullable Item outputItem, @Nullable List<Identifier> items, @Nullable TagKey<Item> defaultTag) {

    public OutputData(Integer count, @Nullable Item outputItem, @Nullable List<Identifier> items, @Nullable TagKey<Item> defaultTag) {
        this(count, DataComponentPatch.EMPTY, outputItem, items, defaultTag);
    }

    public static final Endec<OutputData> ENDEC = StructEndecBuilder.of(
        Endec.INT.fieldOf("count", OutputData::count),
        EndecUtils.optionalFieldOf("components", CodecUtils.toEndec(DataComponentPatch.CODEC), OutputData::components, () -> DataComponentPatch.EMPTY),
        MinecraftEndecs.ofRegistry(BuiltInRegistries.ITEM).optionalFieldOf("item", OutputData::outputItem, () -> null),
        MinecraftEndecs.IDENTIFIER.listOf().optionalFieldOf("priority", OutputData::items, () -> null),
        MinecraftEndecs.unprefixedTagKey(Registries.ITEM).optionalFieldOf("tag", OutputData::defaultTag, () -> null),
        OutputData::new
    );

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
