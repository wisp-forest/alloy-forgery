package io.wispforest.alloyforgery.forges;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.TagInjector;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.ForgeControllerItem;
import io.wispforest.alloyforgery.block.ForgeControllerBlock;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeRegistry {

    public static Endec<ForgeDefinition> FORGE_DEFINITION = MinecraftEndecs.IDENTIFIER.xmap(
        identifier -> {
            return getForgeDefinition(identifier)
                .orElseThrow(() -> new IllegalStateException("Unable to locate ForgerDefinition with Identifier: [ID: " + identifier + "]"));
        }, forgeDefinition -> {
            for (var entry : getForgeEntries()) {
                if (entry.getValue() == forgeDefinition) return entry.getKey();
            }

            throw new IllegalStateException();
        }
    );

    public static final Gson GSON = new Gson();
    private static final Identifier MINEABLE_PICKAXE = Identifier.parse("mineable/pickaxe");

    private static final Map<Identifier, ForgeDefinition> ID_TO_FORGE_DEFINITION = new LinkedHashMap<>();
    private static final Map<ForgeDefinition, Identifier> FORGE_DEFINITION_TO_ID = new LinkedHashMap<>();

    private static final Map<Identifier, Block> CONTROLLER_BLOCK_REGISTRY = new LinkedHashMap<>();

    public static final class EntryHolder {
        public final Identifier controllerId;
        public final Identifier forgeDefinitionId;
        private final Supplier<ForgeControllerBlock> controllerBlock;

        private EntryHolder(Identifier forgeDefinitionId, Identifier controllerId, BiFunction<Identifier, Identifier, ForgeControllerBlock> controllerBlock) {
            this.forgeDefinitionId = forgeDefinitionId;
            this.controllerId = controllerId;
            this.controllerBlock = Suppliers.memoize(() -> controllerBlock.apply(forgeDefinitionId, controllerId));
        }

        private Item createItem() {
            return new ForgeControllerItem(
                controllerBlock(),
                new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, controllerId))
                    .useBlockDescriptionPrefix()
            );
        }

        public void registerItem() {
            Registry.register(BuiltInRegistries.ITEM, controllerId, createItem());
        }

        public void registerBlock() {
            var controllerBlock = controllerBlock();

            Registry.register(BuiltInRegistries.BLOCK, controllerId, controllerBlock);

            TagInjector.inject(BuiltInRegistries.BLOCK, MINEABLE_PICKAXE, controllerBlock);

            CONTROLLER_BLOCK_REGISTRY.put(forgeDefinitionId, controllerBlock);
            GeneralPlatformUtils.INSTANCE.addToBlockEntity(ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY, controllerBlock);
        }

        public ForgeControllerBlock controllerBlock() {
            return controllerBlock.get();
        }
    }

    static void registerDefinition(Identifier forgeDefinitionId, ForgeDefinition definition) {
        final var controllerId = AlloyForgery.id(BuiltInRegistries.BLOCK.getKey(definition.material()).getPath() + "_forge_controller");

        GeneralPlatformUtils.INSTANCE.handleDefinitionEntry(new EntryHolder(forgeDefinitionId, controllerId, ForgeControllerBlock::of));

        store(forgeDefinitionId, definition);
    }

    public static Optional<ForgeDefinition> getForgeDefinition(Identifier id) {
        return ID_TO_FORGE_DEFINITION.containsKey(id) ? Optional.of(ID_TO_FORGE_DEFINITION.get(id)) : Optional.empty();
    }

    public static Optional<Block> getControllerBlock(Identifier id) {
        return ID_TO_FORGE_DEFINITION.containsKey(id) ? Optional.of(CONTROLLER_BLOCK_REGISTRY.get(id)) : Optional.empty();
    }

    public static Optional<Identifier> getId(ForgeDefinition definition) {
        return FORGE_DEFINITION_TO_ID.containsKey(definition) ? Optional.of(FORGE_DEFINITION_TO_ID.get(definition)) : Optional.empty();
    }

    public static Set<Map.Entry<Identifier, ForgeDefinition>> getForgeEntries() {
        return ID_TO_FORGE_DEFINITION.entrySet();
    }

    public static Set<Identifier> getForgeIds() {
        return ID_TO_FORGE_DEFINITION.keySet();
    }

    public static List<Block> getControllerBlocks() {
        return CONTROLLER_BLOCK_REGISTRY.values().stream().toList();
    }

    private static void store(Identifier id, ForgeDefinition definition) {
        FORGE_DEFINITION_TO_ID.put(definition, id);
        ID_TO_FORGE_DEFINITION.put(id, definition);
    }
}
