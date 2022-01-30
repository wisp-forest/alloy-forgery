package wraith.alloyforgery;

import io.wispforest.owo.moddata.ModDataLoader;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;
import wraith.alloyforgery.forges.ForgeRegistry;
import wraith.alloyforgery.forges.FuelDataLoader;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import wraith.alloyforgery.recipe.AlloyForgeRecipeSerializer;

public class AlloyForgery implements ModInitializer {

    public static final String MOD_ID = "alloy_forgery";

    public static ItemGroup ALLOY_FORGERY_GROUP;

    public static BlockEntityType<ForgeControllerBlockEntity> FORGE_CONTROLLER_BLOCK_ENTITY;
    public static ScreenHandlerType<AlloyForgeScreenHandler> ALLOY_FORGE_SCREEN_HANDLER_TYPE;

    private static final ParticleSystemController CONTROLLER = new ParticleSystemController(id("particles"));
    public static final ParticleSystem<Direction> FORGE_PARTICLES = CONTROLLER.register(Direction.class, (world, pos, facing) -> {
        final Vec3d particleSide = pos.add(0.5 + facing.getOffsetX() * 0.515, 0.25, 0.5 + facing.getOffsetZ() * 0.515);
        ClientParticles.spawnPrecise(ParticleTypes.FLAME, world, particleSide, facing.getOffsetZ() * 0.65, 0.175, facing.getOffsetX() * 0.65);
        ClientParticles.spawnPrecise(ParticleTypes.SMOKE, world, particleSide, facing.getOffsetZ() * 0.65, 0.175, facing.getOffsetX() * 0.65);
    });

    @Override
    public void onInitialize() {
        ALLOY_FORGERY_GROUP = FabricItemGroupBuilder.create(id("alloy_forgery")).icon(() -> new ItemStack(Items.BRICKS)).build();
        ALLOY_FORGE_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(id("alloy_forge"), AlloyForgeScreenHandler::new);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FuelDataLoader());

        ModDataLoader.load(new ForgeRegistry.Loader());
        FORGE_CONTROLLER_BLOCK_ENTITY = ForgeControllerBlockEntity.Type.INSTANCE;

        Registry.register(Registry.BLOCK_ENTITY_TYPE, id("forge_controller"), FORGE_CONTROLLER_BLOCK_ENTITY);

        Registry.register(Registry.RECIPE_TYPE, AlloyForgeRecipe.Type.ID, AlloyForgeRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AlloyForgeRecipe.Type.ID, AlloyForgeRecipeSerializer.INSTANCE);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
