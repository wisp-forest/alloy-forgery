package io.wispforest.alloyforgery.forges;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.annotations.NullableComponent;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.ResourceLocation;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.utils.data.EndecDataLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ForgeFuelDataLoader {

    public static final StructEndec<Pair<Item, ForgeFuelDefinition>> FUEL_ENTRY = StructEndecBuilder.of(
        MinecraftEndecs.ofRegistry(BuiltInRegistries.ITEM).fieldOf("item", Pair::first),
        ForgeFuelDefinition.ENDEC.flatFieldOf(Pair::second),
        Pair::of
    );

    public static final ResourceLocation LOADER_ID = AlloyForgery.id("forge_fuel_loader");

    public static void init() {
        EndecDataLoader.builder("alloy_forge_fuels", FUEL_ENTRY.listOf().structOf("fuels"))
            .create(LOADER_ID, PackType.SERVER_DATA, (data, manager, profiler) -> {
                data.values().stream()
                    .flatMap(Collection::stream)
                    .forEach(entry -> ForgeFuelDataLoader.register(entry.first(), entry.second()));
            });
    }

    private static final Map<Item, ForgeFuelDefinition> REGISTRY = new HashMap<>();

    public static void clear() {
        REGISTRY.clear();
    }

    public static ForgeFuelDefinition getFuelForItem(Item item) {
        return REGISTRY.getOrDefault(item, ForgeFuelDefinition.EMPTY);
    }

    public static boolean hasFuel(Item item) {
        return REGISTRY.containsKey(item);
    }

    public static void register(Item item, ForgeFuelDefinition fuel) {
        REGISTRY.put(item, fuel);
    }

    public record ForgeFuelDefinition(int fuel, @NullableComponent Item returnType) {

        public static final ForgeFuelDefinition EMPTY = new ForgeFuelDefinition(0, null);

        public static StructEndec<ForgeFuelDefinition> ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("fuel", ForgeFuelDefinition::fuel),
            MinecraftEndecs.ofRegistry(BuiltInRegistries.ITEM).optionalFieldOf("return_item", ForgeFuelDefinition::returnType, () -> null),
            ForgeFuelDefinition::new
        );

        public boolean hasReturnType() {
            return returnType != null;
        }
    }
}
