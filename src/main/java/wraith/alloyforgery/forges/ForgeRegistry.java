package wraith.alloyforgery.forges;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.FilenameUtils;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.ForgeControllerItem;
import wraith.alloyforgery.block.ForgeControllerBlock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static void readJsonAndEnqueueRegistration() {

        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            try {
                final var targetPath = modContainer.getRootPath().resolve(String.format("data/%s/alloy_forges", modContainer.getMetadata().getId()));

                if (!Files.exists(targetPath)) return;
                Files.walk(targetPath).forEach(path -> {
                    tryReadFromPath(modContainer.getMetadata().getId(), path);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        final var forgePath = FabricLoader.getInstance().getGameDir().resolve("alloy_forges");
        if (!Files.exists(forgePath)) return;

        try {
            Files.walk(forgePath).forEach(path -> {
                tryReadFromPath(AlloyForgery.MOD_ID, path);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void tryReadFromPath(String namespace, Path path) {
        if (!path.toString().endsWith(".json")) return;
        try {
            final InputStreamReader forgeData = new InputStreamReader(Files.newInputStream(path));
            ForgeDefinition.loadAndEnqueue(new Identifier(namespace, FilenameUtils.removeExtension(path.getFileName().toString())), GSON.fromJson(forgeData, JsonObject.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
