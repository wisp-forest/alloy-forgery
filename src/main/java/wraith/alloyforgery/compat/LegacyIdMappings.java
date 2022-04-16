package wraith.alloyforgery.compat;

import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgery;

import java.util.HashMap;
import java.util.Map;

public class LegacyIdMappings {

    private static final Map<Identifier, Identifier> MAPPINGS = new HashMap<>();

    static {
        MAPPINGS.put(id("blackstone_forge_controller"), id("polished_blackstone_forge_controller"));
        MAPPINGS.put(id("brick_forge_controller"), id("bricks_forge_controller"));
        MAPPINGS.put(id("deepslate_forge_controller"), id("deepslate_bricks_forge_controller"));
        MAPPINGS.put(id("end_stone_forge_controller"), id("end_stone_bricks_forge_controller"));
        MAPPINGS.put(id("stone_brick_forge_controller"), id("stone_bricks_forge_controller"));
    }

    public static Identifier remap(Identifier original) {
        return MAPPINGS.getOrDefault(original, original);
    }

    private static Identifier id(String path) {
        return new Identifier(AlloyForgery.MOD_ID, path);
    }
}
