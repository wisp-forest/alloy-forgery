package wraith.alloyforgery.compat;

import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgery;

import java.util.HashMap;
import java.util.Map;

public class LegacyIdMappings {

    private static final Map<Identifier, Identifier> MAPPINGS = new HashMap<>();

    static {
        MAPPINGS.put(AlloyForgery.id("blackstone_forge_controller"), AlloyForgery.id("polished_blackstone_forge_controller"));
        MAPPINGS.put(AlloyForgery.id("brick_forge_controller"), AlloyForgery.id("bricks_forge_controller"));
        MAPPINGS.put(AlloyForgery.id("deepslate_forge_controller"), AlloyForgery.id("deepslate_bricks_forge_controller"));
        MAPPINGS.put(AlloyForgery.id("end_stone_forge_controller"), AlloyForgery.id("end_stone_bricks_forge_controller"));
        MAPPINGS.put(AlloyForgery.id("stone_brick_forge_controller"), AlloyForgery.id("stone_bricks_forge_controller"));
    }

    public static Identifier remap(Identifier original) {
        return MAPPINGS.getOrDefault(original, original);
    }
}
