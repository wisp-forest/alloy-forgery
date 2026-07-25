package io.wispforest.alloyforgery;

import com.google.common.reflect.Reflection;
import com.mojang.datafixers.util.Either;
import io.wispforest.alloyforgery.client.BlockEntityLocation;
import io.wispforest.alloyforgery.compat.AlloyForgeryConfig;
import io.wispforest.alloyforgery.utils.DataPackEvents;
import io.wispforest.alloyforgery.utils.LoaderPlatformUtils;
import io.wispforest.endec.Endec;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.PackType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
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

    public static final String MOD_ID = "alloy-forgery";

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

    private static final ParticleSystemController CONTROLLER = new ParticleSystemController(id("particles"));
    public static final ParticleSystem<Direction> FORGE_PARTICLES = CONTROLLER.register(Direction.class, (world, pos, facing) -> {
        final Vec3 particleSide = pos.add(0.5 + facing.getStepX() * 0.515, 0.25, 0.5 + facing.getStepZ() * 0.515);
        ClientParticles.spawnPrecise(ParticleTypes.FLAME, world, particleSide,
            facing.getStepZ() * 0.65,
            0.175,
            facing.getStepX() * 0.65);

        ClientParticles.spawnPrecise(ParticleTypes.SMOKE, world, particleSide,
            facing.getStepZ() * 0.65,
            0.175,
            facing.getStepX() * 0.65);
    });

    @SuppressWarnings("UnstableApiUsage")
    public static void init() {
        AlloyForgeNetworking.init();

        Endec<Map<Item, ItemStack>> remaindersEndec = Endec.map(
                item -> BuiltInRegistries.ITEM.getKey(item).toString(),
                id -> BuiltInRegistries.ITEM.getValue(Identifier.parse(id)),
                CodecUtils.eitherEndec(CodecUtils.toEndec(ItemStack.STRICT_CODEC), MinecraftEndecs.ofRegistry(BuiltInRegistries.ITEM))
                        .xmap(either -> Either.unwrap(either.mapRight(Item::getDefaultInstance)), Either::left)
        );

        EndecDataLoader.builder("forge_remainder", remaindersEndec)
            .create(Identifier.fromNamespaceAndPath(MOD_ID, "forge_remainder"), PackType.SERVER_DATA, (data, manager, profiler) -> {
                data.values().forEach(AlloyForgeRecipe::addRemainders);
            });

        ForgeFuelDataLoader.init();
        ForgeTierDataLoader.init();

        Reflection.initialize(ForgeFuelDataLoader.class);

        RecipeInjector.ADD_RECIPES.register(new BlastFurnaceRecipeAdapter());
        DataPackEvents.BEFORE_SYNC.register(RecipeInjector::injectRecipes);

        ForgeDefinition.injectRecipeAdditions();
    }

    public static void registerScreenHandlerType() {
        AlloyForgeScreenHandler.ALLOY_FORGE_SCREEN_HANDLER_TYPE = Registry.register(
            BuiltInRegistries.MENU,
            id("alloy_forge"),
            INSTANCE.createScreenHandlerType(
                (syncId, inventory, location) -> new AlloyForgeScreenHandler(syncId, inventory, location.get(inventory.player, ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY)),
                CodecUtils.toPacketCodec(BlockEntityLocation.ENDEC))
        );
    }

    public static void registerBlockEntities() {
        ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("forge_controller"), INSTANCE.createBlockEntityType(ForgeControllerBlockEntity::new));
    }

    public static void registerRecipeTypes() {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, AlloyForgeRecipe.Type.ID, AlloyForgeRecipe.Type.INSTANCE);
    }

    public static void registerRecipeSerializers() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, AlloyForgeRecipe.Type.ID, AlloyForgeRecipeSerializer.INSTANCE);
    }

    public static void registerSlotDisplays() {
        Registry.register(BuiltInRegistries.SLOT_DISPLAY, id("counted_ingredient"), CountedIngredientDisplay.SERIALIZER);
    }

    public static void registerItemGroup() {
        AlloyForgeryItemGroup.GROUP.initialize();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String translationKey(String suffix) {
        return MOD_ID + "." + suffix;
    }

    public static MutableComponent translation(String prefix, String suffix, Object... args) {
        return Component.translatable(prefix + "." + MOD_ID + "." + suffix, args);
    }

    public static MutableComponent tooltipTranslation(String suffix, Object... args) {
        return translation("tooltip", suffix, args);
    }
}
