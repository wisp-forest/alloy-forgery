package wraith.alloyforgery.forges;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.ForgeControllerItem;
import wraith.alloyforgery.block.ForgeControllerBlock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;

public class ForgeRegistry {

    private static final Gson GSON = new Gson();

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

    public static void loadFromJson() {

        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            try {
                final var targetPath = modContainer.getRootPath().resolve(String.format("data/%s/alloy_forges", modContainer.getMetadata().getId()));

                if (!Files.exists(targetPath)) return;

                Files.walk(targetPath).forEach(path -> {

                    if (!path.toString().endsWith(".json")) return;

                    try {
                        final InputStreamReader forgeData = new InputStreamReader(Files.newInputStream(path));
                        FORGE_DEFINITION_REGISTRY.put(new Identifier(modContainer.getMetadata().getId(), path.getFileName().toString()), ForgeDefinition.fromJson(GSON.fromJson(forgeData, JsonObject.class)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        FORGE_DEFINITION_REGISTRY.forEach((identifier, forgeDefinition) -> System.out.println(identifier + "-->" + forgeDefinition));

    }

    public static void registerBlocks() {

        FORGE_DEFINITION_REGISTRY.forEach((identifier, forgeDefinition) -> {

            final var newController = new ForgeControllerBlock(forgeDefinition);
            final var id = AlloyForgery.id(Registry.BLOCK.getId(forgeDefinition.material()).getPath() + "_forge_controller");

            Registry.register(Registry.BLOCK, id, newController);
            Registry.register(Registry.ITEM, id, new ForgeControllerItem(newController, new Item.Settings()));

            CONTROLLER_BLOCK_REGISTRY.put(identifier, newController);
        });
    }

}
