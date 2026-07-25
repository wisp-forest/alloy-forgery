package io.wispforest.alloyforgery.utils;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import io.wispforest.alloyforgery.forges.ForgeDefinition;
import io.wispforest.alloyforgery.forges.ForgeRegistry;
import io.wispforest.alloyforgery.utils.data.EndecDataLoader;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.recipe.*;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.OptionalInt;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface GeneralPlatformUtils {
    GeneralPlatformUtils INSTANCE = load(GeneralPlatformUtils.class);

    //--

    <T extends AbstractContainerMenu, D> MenuType<T> createScreenHandlerType(ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> packetCodec);

    interface ExtendedFactory<T extends AbstractContainerMenu, D> {
        T create(int syncId, Inventory inventory, D data);
    }

    //--

    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(Factory<T> factory);

    void addToBlockEntity(BlockEntityType<ForgeControllerBlockEntity> type, Block...blocks);

    @FunctionalInterface
    public interface Factory<T extends BlockEntity> {
        T create(BlockPos blockPos, BlockState blockState);
    }

    //--

    boolean interactWithFluidStorage(ForgeControllerBlockEntity controller, Player player, InteractionHand hand);

    FluidStorage createStorage(ForgeControllerBlockEntity controller);

    //--

    <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeManager manager, RecipeType<T> type, I input, Level world);

    <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeManager manager, RecipeType<T> type);

    Ingredient createStackIngredient(ItemStack stack);

    //--

    void registerLoader(ResourceLocation id, PackType packType, EndecDataLoader<?> loader, boolean requiresRegistries);

    //--

    ResourceConditionHolder createConditionsHolder();

    //--

    default OptionalInt openHandledScreen(Player player, ForgeControllerBlockEntity blockEntity, @Nullable MenuProvider factory) {
        return player.openMenu(factory);
    }

    //--

    private static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));

        AlloyForgery.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);

        return loadedService;
    }

    //--

    void handleDefinitionEntry(ForgeRegistry.EntryHolder holder);
}
