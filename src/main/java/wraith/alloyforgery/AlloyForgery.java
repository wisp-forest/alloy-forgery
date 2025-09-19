package wraith.alloyforgery;

import com.google.common.reflect.Reflection;
import com.mojang.datafixers.util.Either;
import io.wispforest.endec.Endec;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.OwoFreezer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;
import wraith.alloyforgery.client.BlockEntityLocation;
import wraith.alloyforgery.compat.AlloyForgeryConfig;
import wraith.alloyforgery.compat.CountedIngredientDisplay;
import wraith.alloyforgery.data.RecipeTagLoader;
import wraith.alloyforgery.forges.*;
import wraith.alloyforgery.networking.AlloyForgeNetworking;
import wraith.alloyforgery.recipe.*;
import wraith.alloyforgery.utils.RecipeInjector;
import wraith.alloyforgery.utils.data.EndecDataLoader;

import java.util.Map;

public class AlloyForgery implements ModInitializer {

    public static final String MOD_ID = "alloy_forgery";

    public static final AlloyForgeryConfig CONFIG = AlloyForgeryConfig.createAndLoad();

    public static BlockEntityType<ForgeControllerBlockEntity> FORGE_CONTROLLER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ForgeControllerBlockEntity::new).build();
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
        AlloyForgeNetworking.init();

        ALLOY_FORGE_SCREEN_HANDLER_TYPE = Registry.register(Registries.SCREEN_HANDLER, id("alloy_forge"), new ExtendedScreenHandlerType<>(
            (syncId, inventory, location) -> new AlloyForgeScreenHandler(syncId, inventory, location.get(inventory.player, FORGE_CONTROLLER_BLOCK_ENTITY)),
            CodecUtils.toPacketCodec(BlockEntityLocation.ENDEC)));

        Endec<Map<Item, ItemStack>> remaindersEndec = Endec.map(
                item -> Registries.ITEM.getId(item).toString(),
                id -> Registries.ITEM.get(Identifier.of(id)),
                CodecUtils.eitherEndec(CodecUtils.toEndec(ItemStack.VALIDATED_CODEC), MinecraftEndecs.ofRegistry(Registries.ITEM))
                        .xmap(either -> Either.unwrap(either.mapRight(Item::getDefaultStack)), Either::left)
        );

        new EndecDataLoader<>(
                Identifier.of(AlloyForgery.MOD_ID, "forge_remainder"),
                "forge_remainder",
                remaindersEndec,
                ResourceType.SERVER_DATA
        ) {
            @Override
            protected void apply(Map<Identifier, Map<Item, ItemStack>> prepared, ResourceManager manager, Profiler profiler) {
                prepared.values().forEach(AlloyForgeRecipe::addRemainders);
            }
        };

        Reflection.initialize(ForgeFuelDataLoader.class);

        ForgeTierDataLoader.initDataLoaders();

        var recipeTagLoader = new RecipeTagLoader();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(recipeTagLoader);

        recipeTagLoader.initEvents();

        RecipeInjector.initEvents();
        RecipeInjector.ADD_RECIPES.register(new BlastFurnaceRecipeAdapter());

        ForgeDefinition.initLoaders();

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

        Registry.register(Registries.SLOT_DISPLAY, id("counted_ingredient"), CountedIngredientDisplay.SERIALIZER);
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
