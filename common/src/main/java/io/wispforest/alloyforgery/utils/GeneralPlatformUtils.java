package io.wispforest.alloyforgery.utils;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import io.wispforest.alloyforgery.utils.data.EndecDataLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface GeneralPlatformUtils {
    GeneralPlatformUtils INSTANCE = load(GeneralPlatformUtils.class);

    //--

    <T extends ScreenHandler, D> ScreenHandlerType<T> createScreenHandlerType(ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> packetCodec);

    interface ExtendedFactory<T extends ScreenHandler, D> {
        T create(int syncId, PlayerInventory inventory, D data);
    }

    //--

    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(Factory<T> factory);

    void addToBlockEntity(BlockEntityType<?> type, Block...blocks);

    @FunctionalInterface
    public interface Factory<T extends BlockEntity> {
        T create(BlockPos blockPos, BlockState blockState);
    }

    //--

    boolean interactWithFluidStorage(ForgeControllerBlockEntity controller, PlayerEntity player, Hand hand);

    FluidStorage createStorage(ForgeControllerBlockEntity controller);

    //--

    <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(ServerRecipeManager manager, RecipeType<T> type, I input, World world);

    <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(ServerRecipeManager manager, RecipeType<T> type);

    Ingredient createStackIngredient(ItemStack stack);

    //--

    void registerLoader(Identifier id, ResourceType packType, EndecDataLoader<?> loader, boolean requiresRegistries);

    //--

    ResourceConditionHolder createConditionsHolder();

    //--

    private static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));

        AlloyForgery.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);

        return loadedService;
    }

    //--


}
