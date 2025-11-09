package io.wispforest.alloyforgery.fabric.utils;

import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import io.wispforest.alloyforgery.fabric.FluidHolderImpl;
import io.wispforest.alloyforgery.fabric.data.FabricResourceConditionHolder;
import io.wispforest.alloyforgery.forges.ForgeRegistry;
import io.wispforest.alloyforgery.utils.FluidStorage;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.alloyforgery.utils.data.EndecDataLoader;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public final class FabricGeneralPlatformUtils implements GeneralPlatformUtils {
    @Override
    public <T extends ScreenHandler, D> ScreenHandlerType<T> createScreenHandlerType(ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> packetCodec) {
        return new ExtendedScreenHandlerType<>(factory::create, packetCodec);
    }

    //--

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(Factory<T> factory) {
        return FabricBlockEntityTypeBuilder.create(factory::create).build();
    }

    @Override
    public void addToBlockEntity(BlockEntityType<ForgeControllerBlockEntity> type, Block... blocks) {
        for (var block : blocks) type.addSupportedBlock(block);
    }

    //--

    @Override
    public boolean interactWithFluidStorage(ForgeControllerBlockEntity controller, PlayerEntity player, Hand hand) {
        return FluidStorageUtil.interactWithFluidStorage(controller.<FluidHolderImpl>getFluidHolder(), player, hand);
    }

    @Override
    public FluidStorage createStorage(ForgeControllerBlockEntity controller) {
        return new FluidHolderImpl(controller::markDirty);
    }

    //--

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(ServerRecipeManager manager, RecipeType<T> type, I input, World world) {
        return manager.getAllMatches(type, input, world);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(ServerRecipeManager manager, RecipeType<T> type) {
        return manager.getAllOfType(type);
    }

    @Override
    public Ingredient createStackIngredient(ItemStack stack) {
        return new ComponentsIngredient(Ingredient.ofItem(stack.getItem()), stack.getComponentChanges())
            .toVanilla();
    }

    //--

    @Override
    public void registerLoader(Identifier id, ResourceType packType, EndecDataLoader<?> loader, boolean requiresRegistries) {
        if (requiresRegistries) {
            loader.setRegistryGetter(store -> store.getOrThrow(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY));
        }

        ResourceLoader.get(packType).registerReloader(id, loader);
    }

    //--

    @Override
    public ResourceConditionHolder createConditionsHolder() {
        return new FabricResourceConditionHolder(new ArrayList<>());
    }

    //--

    @Override
    public void handleDefinitionEntry(ForgeRegistry.EntryHolder holder) {
        holder.registerBlock();
        holder.registerItem();
    }
}
