package wraith.alloyforgery;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;
import wraith.alloyforgery.forges.ForgeRegistry;

public class AlloyForgeryServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ForgeRegistry.loadFromJson();
        ForgeRegistry.registerBlocks();

        AlloyForgery.FORGE_CONTROLLER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ForgeControllerBlockEntity::new, ForgeRegistry.getControllerBlocks().toArray(new Block[0])).build();
        Registry.register(Registry.BLOCK_ENTITY_TYPE, AlloyForgery.id("forge_controller"), AlloyForgery.FORGE_CONTROLLER_BLOCK_ENTITY);
    }
}
