package wraith.alloyforgery;

import io.wispforest.owo.moddata.ModDataLoader;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.util.OwoFreezer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;
import wraith.alloyforgery.data.AlloyForgeryGlobalRemaindersLoader;
import wraith.alloyforgery.data.RecipeTagLoader;
import wraith.alloyforgery.forges.ForgeRegistry;
import wraith.alloyforgery.forges.FuelDataLoader;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import wraith.alloyforgery.recipe.AlloyForgeRecipeSerializer;
import wraith.alloyforgery.recipe.BlastFurnaceRecipeAdapter;
import wraith.alloyforgery.utils.RecipeInjector;

public class AlloyForgery implements ModInitializer {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(id("main"));

    public static final String MOD_ID = "alloy_forgery";

    public static BlockEntityType<ForgeControllerBlockEntity> FORGE_CONTROLLER_BLOCK_ENTITY = ForgeControllerBlockEntity.Type.INSTANCE;
    public static ScreenHandlerType<AlloyForgeScreenHandler> ALLOY_FORGE_SCREEN_HANDLER_TYPE;

    private static final ParticleSystemController CONTROLLER = new ParticleSystemController(id("particles"));
    public static final ParticleSystem<Direction> FORGE_PARTICLES = CONTROLLER.register(Direction.class, (world, pos, facing) -> {
        final Vec3d particleSide = pos.add(0.5 + facing.getOffsetX() * 0.515, 0.25, 0.5 + facing.getOffsetZ() * 0.515);
        ClientParticles.spawnPrecise(ParticleTypes.FLAME, world, particleSide,
                facing.getOffsetZ() * 0.65,
                0.175,
                facing.getOffsetX() * 0.65);

        ClientParticles.spawnPrecise(ParticleTypes.SMOKE, world, particleSide,
                facing.getOffsetZ() * 0.65,
                0.175,
                facing.getOffsetX() * 0.65);
    });

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onInitialize() {
        ALLOY_FORGE_SCREEN_HANDLER_TYPE = Registry.register(Registries.SCREEN_HANDLER, id("alloy_forge"), new ScreenHandlerType<>(AlloyForgeScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES));

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AlloyForgeryGlobalRemaindersLoader());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(FuelDataLoader.INSTANCE);

        var recipeTagLoader = new RecipeTagLoader();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(recipeTagLoader);

        recipeTagLoader.initEvents();

        CHANNEL.registerClientboundDeferred(RecipeTagLoader.TagPacket.class);

        RecipeInjector.initEvents();
        RecipeInjector.ADD_RECIPES.register(new BlastFurnaceRecipeAdapter());

        ModDataLoader.load(ForgeRegistry.Loader.INSTANCE);

        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("forge_controller"), FORGE_CONTROLLER_BLOCK_ENTITY);

        Registry.register(Registries.RECIPE_TYPE, AlloyForgeRecipe.Type.ID, AlloyForgeRecipe.Type.INSTANCE);
        Registry.register(Registries.RECIPE_SERIALIZER, AlloyForgeRecipe.Type.ID, AlloyForgeRecipeSerializer.INSTANCE);

        AlloyForgeryItemGroup.GROUP.initialize();

        ItemStorage.SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
            if (context == Direction.DOWN && world.getBlockEntity(pos.up()) instanceof ForgeControllerBlockEntity froge)
                return InventoryStorage.of(froge, Direction.DOWN);

            return null;
        });

        OwoFreezer.registerFreezeCallback(() -> FluidStorage.SIDED.registerSelf(AlloyForgery.FORGE_CONTROLLER_BLOCK_ENTITY));
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
