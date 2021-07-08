package wraith.alloyforgery.mixin;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wraith.alloyforgery.AlloyForgery;

@Mixin(DefaultedRegistry.class)
public class DefaultedRegistryMixin {

    // Removed identifiers from legacy
    Identifier alloyForgeryBlackstone = AlloyForgery.id("blackstone_forge_controller");
    Identifier alloyForgeryBrick = AlloyForgery.id("brick_forge_controller");
    Identifier alloyForgeryDeepslate = AlloyForgery.id("deepslate_forge_controller");
    Identifier alloyForgeryEndstone = AlloyForgery.id("end_stone_forge_controller");
    Identifier alloyForgeryStoneBricks = AlloyForgery.id("stone_brick_forge_controller");

    // New corresponding identifiers
    Identifier alloyFrogeryBlackstone = AlloyForgery.id("polished_blackstone_forge_controller");
    Identifier alloyFrogeryBricks = AlloyForgery.id("bricks_forge_controller");
    Identifier alloyFrogeryDeepslate = AlloyForgery.id("deepslate_bricks_forge_controller");
    Identifier alloyFrogeryEndstone = AlloyForgery.id("end_stone_bricks_forge_controller");
    Identifier alloyFrogeryStoneBricks = AlloyForgery.id("stone_bricks_forge_controller");

    @ModifyVariable(at = @At("HEAD"), method = "get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;", ordinal = 0)
    Identifier fixMissingFromRegistry(@Nullable Identifier id) {
        if (id != null) {
            if (id.equals(alloyForgeryBlackstone)) return alloyFrogeryBlackstone;
            if (id.equals(alloyForgeryBrick)) return alloyFrogeryBricks;
            if (id.equals(alloyForgeryDeepslate)) return alloyFrogeryDeepslate;
            if (id.equals(alloyForgeryEndstone)) return alloyFrogeryEndstone;
            if (id.equals(alloyForgeryStoneBricks)) return alloyFrogeryStoneBricks;
        }
        return id;
    }
}
