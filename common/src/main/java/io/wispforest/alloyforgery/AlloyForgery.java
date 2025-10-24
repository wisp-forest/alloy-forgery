package io.wispforest.alloyforgery;

import com.google.common.reflect.Reflection;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.wispforest.alloyforgery.client.BlockEntityLocation;
import io.wispforest.alloyforgery.compat.AlloyForgeryConfig;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.alloyforgery.utils.LoaderPlatformUtils;
import io.wispforest.endec.Endec;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.wispforest.alloyforgery.compat.CountedIngredientDisplay;
import io.wispforest.alloyforgery.forges.ForgeDefinition;
import io.wispforest.alloyforgery.forges.ForgeFuelDataLoader;
import io.wispforest.alloyforgery.forges.ForgeTierDataLoader;
import io.wispforest.alloyforgery.networking.AlloyForgeNetworking;
import io.wispforest.alloyforgery.recipe.AlloyForgeRecipe;
import io.wispforest.alloyforgery.recipe.AlloyForgeRecipeSerializer;
import io.wispforest.alloyforgery.recipe.BlastFurnaceRecipeAdapter;
import io.wispforest.alloyforgery.utils.RecipeInjector;
import io.wispforest.alloyforgery.utils.data.EndecDataLoader;

import java.util.Map;

import static io.wispforest.alloyforgery.utils.GeneralPlatformUtils.INSTANCE;

public class AlloyForgery {

    public static final String MOD_ID = "alloy_forgery";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Whether the given mods debug is enabled, this defaults to {@code true} in a development environment.
     */
    public static final boolean DEBUG;

    static {
        boolean debug = LoaderPlatformUtils.INSTANCE.isDevelopmentEnvironment();
        if (System.getProperty(MOD_ID + ".debug") != null) debug = Boolean.getBoolean(MOD_ID + ".debug");

        DEBUG = debug;
    }

    public static final AlloyForgeryConfig CONFIG = AlloyForgeryConfig.createAndLoad();

    public static BlockEntityType<ForgeControllerBlockEntity> FORGE_CONTROLLER_BLOCK_ENTITY = INSTANCE.createBlockEntityType(ForgeControllerBlockEntity::new);
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
    public static void init() {
        AlloyForgeNetworking.init();

        Endec<Map<Item, ItemStack>> remaindersEndec = Endec.map(
                item -> Registries.ITEM.getId(item).toString(),
                id -> Registries.ITEM.get(Identifier.of(id)),
                CodecUtils.eitherEndec(CodecUtils.toEndec(ItemStack.VALIDATED_CODEC), MinecraftEndecs.ofRegistry(Registries.ITEM))
                        .xmap(either -> Either.unwrap(either.mapRight(Item::getDefaultStack)), Either::left)
        );

        EndecDataLoader.builder("forge_remainder", remaindersEndec)
            .create(Identifier.of(MOD_ID, "forge_remainder"), ResourceType.SERVER_DATA, (data, manager, profiler) -> {
                data.values().forEach(AlloyForgeRecipe::addRemainders);
            });

        ForgeFuelDataLoader.init();
        ForgeTierDataLoader.init();

        Reflection.initialize(ForgeFuelDataLoader.class);

        RecipeInjector.ADD_RECIPES.register(new BlastFurnaceRecipeAdapter());

        ForgeDefinition.initLoaders();

        AlloyForgeryItemGroup.GROUP.initialize();
    }

    public static void registerScreenHandlerType() {
        ALLOY_FORGE_SCREEN_HANDLER_TYPE = Registry.register(
            Registries.SCREEN_HANDLER,
            id("alloy_forge"),
            INSTANCE.createScreenHandlerType(
                (syncId, inventory, location) -> new AlloyForgeScreenHandler(syncId, inventory, location.get(inventory.player, FORGE_CONTROLLER_BLOCK_ENTITY)),
                CodecUtils.toPacketCodec(BlockEntityLocation.ENDEC))
        );
    }

    public static void registerBlockEntities() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("forge_controller"), FORGE_CONTROLLER_BLOCK_ENTITY);
    }

    public static void registerRecipeTypes() {
        Registry.register(Registries.RECIPE_TYPE, AlloyForgeRecipe.Type.ID, AlloyForgeRecipe.Type.INSTANCE);
    }

    public static void registerRecipeSerializers() {
        Registry.register(Registries.RECIPE_SERIALIZER, AlloyForgeRecipe.Type.ID, AlloyForgeRecipeSerializer.INSTANCE);
    }

    public static void registerSlotDisplays() {
        Registry.register(Registries.SLOT_DISPLAY, id("counted_ingredient"), CountedIngredientDisplay.SERIALIZER);
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
