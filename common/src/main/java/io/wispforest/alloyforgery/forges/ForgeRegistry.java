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
import net.minecraft.resources.ResourceLocation;
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
    private static final ResourceLocation MINEABLE_PICKAXE = ResourceLocation.parse("mineable/pickaxe");

    private static final Map<ResourceLocation, ForgeDefinition> ID_TO_FORGE_DEFINITION = new LinkedHashMap<>();
    private static final Map<ForgeDefinition, ResourceLocation> FORGE_DEFINITION_TO_ID = new LinkedHashMap<>();

    private static final Map<ResourceLocation, Block> CONTROLLER_BLOCK_REGISTRY = new LinkedHashMap<>();

    public static final class EntryHolder {
        public final ResourceLocation controllerId;
        public final ResourceLocation forgeDefinitionId;
        private final Supplier<ForgeControllerBlock> controllerBlock;

        private EntryHolder(ResourceLocation forgeDefinitionId, ResourceLocation controllerId, BiFunction<ResourceLocation, ResourceLocation, ForgeControllerBlock> controllerBlock) {
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

    static void registerDefinition(ResourceLocation forgeDefinitionId, ForgeDefinition definition) {
        final var controllerId = AlloyForgery.id(BuiltInRegistries.BLOCK.getKey(definition.material()).getPath() + "_forge_controller");

        GeneralPlatformUtils.INSTANCE.handleDefinitionEntry(new EntryHolder(forgeDefinitionId, controllerId, ForgeControllerBlock::of));

        store(forgeDefinitionId, definition);
    }

    public static Optional<ForgeDefinition> getForgeDefinition(ResourceLocation id) {
        return ID_TO_FORGE_DEFINITION.containsKey(id) ? Optional.of(ID_TO_FORGE_DEFINITION.get(id)) : Optional.empty();
    }

    public static Optional<Block> getControllerBlock(ResourceLocation id) {
        return ID_TO_FORGE_DEFINITION.containsKey(id) ? Optional.of(CONTROLLER_BLOCK_REGISTRY.get(id)) : Optional.empty();
    }

    public static Optional<ResourceLocation> getId(ForgeDefinition definition) {
        return FORGE_DEFINITION_TO_ID.containsKey(definition) ? Optional.of(FORGE_DEFINITION_TO_ID.get(definition)) : Optional.empty();
    }

    public static Set<Map.Entry<ResourceLocation, ForgeDefinition>> getForgeEntries() {
        return ID_TO_FORGE_DEFINITION.entrySet();
    }

    public static Set<ResourceLocation> getForgeIds() {
        return ID_TO_FORGE_DEFINITION.keySet();
    }

    public static List<Block> getControllerBlocks() {
        return CONTROLLER_BLOCK_REGISTRY.values().stream().toList();
    }

    private static void store(ResourceLocation id, ForgeDefinition definition) {
        FORGE_DEFINITION_TO_ID.put(definition, id);
        ID_TO_FORGE_DEFINITION.put(id, definition);
    }
}
