package wraith.alloyforgery.forges;

import io.wispforest.owo.moddata.ModDataConsumer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.ForgeControllerItem;
import wraith.alloyforgery.block.ForgeControllerBlock;

import java.util.*;

public class ForgeRegistry {

    public static final Gson GSON = new Gson();

    private static final Map<Identifier, ForgeDefinition> FORGE_DEFINITION_REGISTRY = new HashMap<>();
    private static final Map<Identifier, Block> CONTROLLER_BLOCK_REGISTRY = new HashMap<>();

    public static Optional<ForgeDefinition> getForgeDefinition(Identifier id) {
        return FORGE_DEFINITION_REGISTRY.containsKey(id) ? Optional.of(FORGE_DEFINITION_REGISTRY.get(id)) : Optional.empty();
    }

    public static Optional<Block> getControllerBlock(Identifier id) {
        return FORGE_DEFINITION_REGISTRY.containsKey(id) ? Optional.of(CONTROLLER_BLOCK_REGISTRY.get(id)) : Optional.empty();
    }

    public static Set<Identifier> getForgeIds() {
        return FORGE_DEFINITION_REGISTRY.keySet();
    }

    public static List<Block> getControllerBlocks() {
        return CONTROLLER_BLOCK_REGISTRY.values().stream().toList();
    }

    static void registerDefinition(Identifier forgeDefinitionId, ForgeDefinition definition) {
        final var controllerBlock = new ForgeControllerBlock(definition);
        final var controllerBlockRegistryId = AlloyForgery.id(Registry.BLOCK.getId(definition.material()).getPath() + "_forge_controller");

        Registry.register(Registry.BLOCK, controllerBlockRegistryId, controllerBlock);
        Registry.register(Registry.ITEM, controllerBlockRegistryId, new ForgeControllerItem(controllerBlock, new Item.Settings().group(AlloyForgery.ALLOY_FORGERY_GROUP)));

        store(forgeDefinitionId, definition, controllerBlock);
    }

    private static void store(Identifier id, ForgeDefinition definition, ForgeControllerBlock block) {
        FORGE_DEFINITION_REGISTRY.put(id, definition);
        CONTROLLER_BLOCK_REGISTRY.put(id, block);
    }

    public static final class Loader implements ModDataConsumer {

        @Override
        public String getDataSubdirectory() {
            return "alloy_forges";
        }

        @Override
        public void acceptParsedFile(Identifier id, JsonObject object) {
            ForgeDefinition.loadAndEnqueue(id, object);
        }
    }

}
