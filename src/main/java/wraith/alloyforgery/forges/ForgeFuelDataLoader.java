package wraith.alloyforgery.forges;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.annotations.NullableComponent;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.utils.data.EndecDataLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgeFuelDataLoader {

    public static final StructEndec<Pair<Item, ForgeFuelDefinition>> FUEL_ENTRY = StructEndecBuilder.of(
        MinecraftEndecs.ofRegistry(Registries.ITEM).fieldOf("item", Pair::first),
        ForgeFuelDefinition.ENDEC.flatFieldOf(Pair::second),
        Pair::of
    );

    public static final EndecDataLoader<List<Pair<Item, ForgeFuelDefinition>>> FUEL_DATA_LOADER = new EndecDataLoader<>(
            AlloyForgery.id("forge_fuel_loader"),
            "alloy_forge_fuels",
            ForgeFuelDataLoader.FUEL_ENTRY.listOf().structOf("fuels"),
            ResourceType.SERVER_DATA
    ) {
        @Override
        protected void apply(Map<Identifier, List<Pair<Item, ForgeFuelDefinition>>> prepared, ResourceManager manager, Profiler profiler) {
            prepared.forEach((identifier, fuelEntries) -> {
                fuelEntries.forEach(fuelEntry -> ForgeFuelDataLoader.register(fuelEntry.first(), fuelEntry.second()));
            });
        }
    };

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
            MinecraftEndecs.ofRegistry(Registries.ITEM).optionalFieldOf("return_item", ForgeFuelDefinition::returnType, () -> null),
            ForgeFuelDefinition::new
        );

        public boolean hasReturnType() {
            return returnType != null;
        }
    }
}
